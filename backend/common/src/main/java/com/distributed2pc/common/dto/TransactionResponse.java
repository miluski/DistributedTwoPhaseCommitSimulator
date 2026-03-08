package com.distributed2pc.common.dto;

import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.common.enums.VoteResult;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response returned by the coordinator after a transaction has been initiated
 * or completed.
 *
 * @param transactionId Unique identifier of the transaction.
 * @param status        Current lifecycle status.
 * @param value         Payload that was (or was being) committed.
 * @param initiatedAt   Timestamp when the transaction was created.
 * @param decidedAt     Timestamp when the COMMIT/ABORT decision was made (null
 *                      if pending).
 * @param votes         Map of serverId → vote, populated after Phase 1
 *                      completes.
 */
public record TransactionResponse(
        UUID transactionId,
        TransactionStatus status,
        String value,
        Instant initiatedAt,
        Instant decidedAt,
        Map<String, VoteResult> votes) {
}
