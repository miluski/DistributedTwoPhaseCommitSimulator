package com.distributed2pc.common.dto;

import java.util.UUID;

/**
 * Message sent by the coordinator to each participant during Phase 1 of the 2PC
 * protocol.
 *
 * @param transactionId Unique identifier of the transaction.
 * @param value         Payload that the participant must be prepared to commit.
 */
public record PrepareMessage(UUID transactionId, String value) {
}
