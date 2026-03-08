package com.distributed2pc.participant.feature.election.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.distributed2pc.common.dto.PeerLogEntryDto;
import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.participant.feature.election.infrastructure.PeerConsultationClient;
import com.distributed2pc.participant.feature.log.domain.LogEntry;
import com.distributed2pc.participant.feature.log.infrastructure.InMemoryTransactionLog;
import com.distributed2pc.participant.feature.simulation.application.SimulationModeService;

import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link ElectionService}.
 */
@ExtendWith(MockitoExtension.class)
class ElectionServiceTest {

    @Mock
    InMemoryTransactionLog transactionLog;

    @Mock
    PeerConsultationClient peerClient;

    @Mock
    SimulationModeService simulationModeService;

    ElectionService service;

    private static final String PEER_URL = "https://peer-1:9001";

    @BeforeEach
    void setUp() {
        when(simulationModeService.isRedundancyEnabled()).thenReturn(true);
        service = new ElectionService(transactionLog, peerClient, simulationModeService, List.of(PEER_URL));
    }

    @ParameterizedTest
    @MethodSource("decidedStatuses")
    void elect_givenLocalLogIsDecided_returnsLocalDecision(TransactionStatus decided) {
        UUID txId = UUID.randomUUID();
        LogEntry localEntry = new LogEntry(txId, decided, "v", Instant.now());
        when(transactionLog.read(txId)).thenReturn(Optional.of(localEntry));

        TransactionStatus result = service.elect(txId);

        assertThat(result).isEqualTo(decided);
    }

    static Stream<TransactionStatus> decidedStatuses() {
        return Stream.of(TransactionStatus.COMMITTED, TransactionStatus.ABORTED);
    }

    @Test
    void elect_givenLocalLogIsPreparing_consultsPeers() {
        UUID txId = UUID.randomUUID();
        LogEntry preparingEntry = new LogEntry(txId, TransactionStatus.PREPARING, "v", Instant.now());
        when(transactionLog.read(txId)).thenReturn(Optional.of(preparingEntry));
        PeerLogEntryDto peerEntry = new PeerLogEntryDto(txId, TransactionStatus.COMMITTED, "peer-1", Instant.now());
        when(peerClient.fetchLogEntry(PEER_URL, txId)).thenReturn(Mono.just(peerEntry));

        TransactionStatus result = service.elect(txId);

        assertThat(result).isEqualTo(TransactionStatus.COMMITTED);
    }

    @Test
    void elect_givenNoLocalLog_consultsPeers() {
        UUID txId = UUID.randomUUID();
        when(transactionLog.read(txId)).thenReturn(Optional.empty());
        PeerLogEntryDto peerEntry = new PeerLogEntryDto(txId, TransactionStatus.ABORTED, "peer-1", Instant.now());
        when(peerClient.fetchLogEntry(PEER_URL, txId)).thenReturn(Mono.just(peerEntry));

        TransactionStatus result = service.elect(txId);

        assertThat(result).isEqualTo(TransactionStatus.ABORTED);
    }

    @Test
    void elect_givenAllPeersUncertain_abortsAsSafeDefault() {
        UUID txId = UUID.randomUUID();
        when(transactionLog.read(txId)).thenReturn(Optional.empty());
        PeerLogEntryDto uncertainEntry = new PeerLogEntryDto(txId, TransactionStatus.PREPARING, "peer-1",
                Instant.now());
        when(peerClient.fetchLogEntry(any(), eq(txId))).thenReturn(Mono.just(uncertainEntry));

        TransactionStatus result = service.elect(txId);

        assertThat(result).isEqualTo(TransactionStatus.ABORTED);
    }

    @Test
    void elect_givenNoPeers_abortsAsSafeDefault() {
        service = new ElectionService(transactionLog, peerClient, simulationModeService, List.of());
        UUID txId = UUID.randomUUID();
        when(transactionLog.read(txId)).thenReturn(Optional.empty());

        TransactionStatus result = service.elect(txId);

        assertThat(result).isEqualTo(TransactionStatus.ABORTED);
    }

    @Test
    void elect_givenRedundancyDisabled_returnsUncertainWithoutConsultingPeers() {
        when(simulationModeService.isRedundancyEnabled()).thenReturn(false);
        UUID txId = UUID.randomUUID();

        TransactionStatus result = service.elect(txId);

        assertThat(result).isEqualTo(TransactionStatus.UNCERTAIN);
    }
}
