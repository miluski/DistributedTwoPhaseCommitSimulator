package com.distributed2pc.common.dto;

import java.util.UUID;

/**
 * Message sent by the coordinator to participants during Phase 2 to abort a
 * transaction.
 *
 * @param transactionId Unique identifier of the transaction to abort.
 */
public record AbortMessage(UUID transactionId) {
}
