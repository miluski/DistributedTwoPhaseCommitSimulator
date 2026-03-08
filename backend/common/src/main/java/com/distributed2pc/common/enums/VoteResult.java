package com.distributed2pc.common.enums;

/**
 * Possible outcomes of a participant vote in Phase 1 of the 2PC protocol.
 */
public enum VoteResult {
    /** Participant is ready and willing to commit the transaction. */
    YES,
    /** Participant cannot commit (resource unavailable, fault injected, etc.). */
    NO
}
