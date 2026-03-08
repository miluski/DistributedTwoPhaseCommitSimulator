package com.distributed2pc.common.dto;

import com.distributed2pc.common.enums.TransactionStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Participant's local log entry for a transaction.
 * Sent to peers during coordinator-failure election to determine the consensus
 * decision.
 *
 * @param transactionId Identifies the transaction.
 * @param phase         The phase recorded in this participant's log (PREPARED,
 *                      COMMITTED, ABORTED).
 * @param serverId      The participant that owns this log entry.
 * @param timestamp     When this entry was written.
 */
public record PeerLogEntryDto(
        UUID transactionId,
        TransactionStatus phase,
        String serverId,
        Instant timestamp) {
}
