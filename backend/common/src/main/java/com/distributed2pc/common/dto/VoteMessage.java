package com.distributed2pc.common.dto;

import com.distributed2pc.common.enums.VoteResult;
import java.util.UUID;

/**
 * Participant response to a PREPARE message (Phase 1 of 2PC).
 *
 * @param transactionId Unique identifier of the transaction being voted on.
 * @param vote          YES if the participant is ready to commit, NO otherwise.
 * @param serverId      Identifier of the participant returning this vote.
 */
public record VoteMessage(UUID transactionId, VoteResult vote, String serverId) {
}
