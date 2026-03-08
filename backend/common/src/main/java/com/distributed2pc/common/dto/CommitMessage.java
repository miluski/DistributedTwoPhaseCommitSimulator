package com.distributed2pc.common.dto;

import java.util.UUID;

/**
 * Message sent by the coordinator to participants during Phase 2 to commit a
 * transaction.
 *
 * @param transactionId Unique identifier of the transaction to commit.
 */
public record CommitMessage(UUID transactionId) {
}
