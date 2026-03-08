package com.distributed2pc.participant.feature.log.domain;

import com.distributed2pc.common.enums.TransactionStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable log entry written by a participant before responding to any 2PC message.
 *
 * <p>The log is the source of truth during coordinator-failure recovery.
 * Each entry captures the last known phase for a transaction.
 *
 * @param transactionId The transaction this entry belongs to.
 * @param phase         The last recorded phase (PREPARING → COMMITTED or ABORTED).
 * @param value         The payload associated with the transaction.
 * @param timestamp     When this entry was written.
 */
public record LogEntry(UUID transactionId, TransactionStatus phase, String value, Instant timestamp) {

    /**
     * Returns a copy of this entry with the phase updated to reflect a decision.
     *
     * @param decision {@link TransactionStatus#COMMITTED} or {@link TransactionStatus#ABORTED}.
     * @return updated log entry.
     */
    public LogEntry withPhase(TransactionStatus decision) {
        return new LogEntry(transactionId, decision, value, Instant.now());
    }
}
