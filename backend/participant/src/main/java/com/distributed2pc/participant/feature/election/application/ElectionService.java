package com.distributed2pc.participant.feature.election.application;

import com.distributed2pc.common.dto.PeerLogEntryDto;
import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.participant.feature.election.infrastructure.PeerConsultationClient;
import com.distributed2pc.participant.feature.log.domain.LogEntry;
import com.distributed2pc.participant.feature.log.infrastructure.InMemoryTransactionLog;
import com.distributed2pc.participant.feature.simulation.application.SimulationModeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements the participant-side termination protocol that runs when the
 * coordinator does not respond.
 *
 * <p>Algorithm:
 * <ol>
 *   <li>If this node's log shows COMMITTED or ABORTED → return that decision.</li>
 *   <li>Otherwise consult all peers concurrently.</li>
 *   <li>If any peer has a decision → adopt it.</li>
 *   <li>If all peers are still PREPARING (uncertain) → safe to ABORT.</li>
 * </ol>
 */
@Slf4j
@Service
public class ElectionService {

    private final InMemoryTransactionLog transactionLog;
    private final PeerConsultationClient peerClient;
    private final SimulationModeService simulationModeService;
    private final List<String> peerUrls;

    /**
     * @param transactionLog       persisted log store for this participant.
     * @param peerClient           HTTP client for consulting peers.
     * @param simulationModeService controls whether the recovery protocol is active.
     * @param peerUrls             list of peer base URLs from configuration.
     */
    public ElectionService(InMemoryTransactionLog transactionLog,
                           PeerConsultationClient peerClient,
                           SimulationModeService simulationModeService,
                           @Value("${participant.peers}") List<String> peerUrls) {
        this.transactionLog = transactionLog;
        this.peerClient = peerClient;
        this.simulationModeService = simulationModeService;
        this.peerUrls = List.copyOf(peerUrls);
    }

    /**
     * Runs the termination protocol for {@code transactionId}.
     *
     * @param transactionId the uncertain transaction to resolve.
     * @return COMMITTED, ABORTED, or UNCERTAIN if consensus could not be reached.
     */
    public TransactionStatus elect(UUID transactionId) {
        if (!simulationModeService.isRedundancyEnabled()) {
            log.info("Redundancy disabled — leaving tx {} UNCERTAIN without peer consultation",
                    transactionId);
            return TransactionStatus.UNCERTAIN;
        }
        Optional<LogEntry> localEntry = transactionLog.read(transactionId);
        if (localEntry.isPresent() && isDecided(localEntry.get().phase())) {
            return localEntry.get().phase();
        }
        return consultPeers(transactionId);
    }

    private boolean isDecided(TransactionStatus phase) {
        return phase == TransactionStatus.COMMITTED || phase == TransactionStatus.ABORTED;
    }

    private TransactionStatus consultPeers(UUID transactionId) {
        List<PeerLogEntryDto> peerEntries = Flux.fromIterable(peerUrls)
                .flatMap(url -> peerClient.fetchLogEntry(url, transactionId))
                .collectList()
                .block();

        if (peerEntries == null) return TransactionStatus.UNCERTAIN;

        Optional<TransactionStatus> decided = peerEntries.stream()
                .map(PeerLogEntryDto::phase)
                .filter(this::isDecided)
                .findFirst();

        if (decided.isPresent()) {
            log.info("Election for {} resolved via peer: {}", transactionId, decided.get());
            return decided.get();
        }
        log.info("All peers uncertain for {} — aborting as safe default", transactionId);
        return TransactionStatus.ABORTED;
    }
}
