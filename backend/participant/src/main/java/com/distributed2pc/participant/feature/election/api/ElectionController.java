package com.distributed2pc.participant.feature.election.api;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.distributed2pc.common.dto.PeerLogEntryDto;
import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.participant.feature.election.application.ElectionService;
import com.distributed2pc.participant.feature.log.infrastructure.InMemoryTransactionLog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST endpoints used during coordinator-failure election.
 *
 * <ul>
 * <li>{@code GET /api/peers/log/{txId}} — return this node's persisted log
 * entry so
 * other participants can determine whether a decision was already reached.</li>
 * <li>{@code POST /api/peers/elect/{txId}} — trigger the termination protocol
 * on
 * this node and return the resolved {@link TransactionStatus}.</li>
 * </ul>
 */
@Tag(name = "Election", description = "Termination protocol during coordinator failure")
@RestController
@RequestMapping("/api/peers")
public class ElectionController {

    private final InMemoryTransactionLog transactionLog;
    private final ElectionService electionService;
    private final String serverId;

    /**
     * @param transactionLog  log store to read this node's persisted entries.
     * @param electionService service that runs the termination protocol.
     * @param serverId        identifier of this participant.
     */
    public ElectionController(InMemoryTransactionLog transactionLog,
            ElectionService electionService,
            @Value("${participant.server-id}") String serverId) {
        this.transactionLog = transactionLog;
        this.electionService = electionService;
        this.serverId = serverId;
    }

    /**
     * Returns this node's log entry for the given transaction.
     *
     * @param txId the transaction to query.
     * @return 200 with {@link PeerLogEntryDto}, or 404 if not in log.
     */
    @Operation(summary = "Returns this node's log entry for the given transaction")
    @ApiResponse(responseCode = "200", description = "Log entry")
    @ApiResponse(responseCode = "400", description = "Invalid transaction ID format")
    @ApiResponse(responseCode = "404", description = "No log entry for transaction")
    @GetMapping("/log/{txId}")
    public ResponseEntity<PeerLogEntryDto> getLogEntry(@PathVariable UUID txId) {
        return transactionLog.read(txId)
                .map(entry -> new PeerLogEntryDto(entry.transactionId(), entry.phase(),
                        serverId, entry.timestamp()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Triggers the election/termination protocol for a transaction.
     * Called by a peer or by the recovery scheduler to resolve an uncertain
     * outcome.
     *
     * @param txId the uncertain transaction.
     * @return 200 with the resolved {@link TransactionStatus}.
     */
    @Operation(summary = "Triggers the election protocol for an uncertain transaction")
    @ApiResponse(responseCode = "200", description = "Election protocol result")
    @ApiResponse(responseCode = "400", description = "Invalid transaction ID format")
    @PostMapping("/elect/{txId}")
    public ResponseEntity<PeerLogEntryDto> runElection(@PathVariable UUID txId) {
        TransactionStatus resolved = electionService.elect(txId);
        PeerLogEntryDto result = new PeerLogEntryDto(txId, resolved, serverId, Instant.now());
        return ResponseEntity.ok(result);
    }
}
