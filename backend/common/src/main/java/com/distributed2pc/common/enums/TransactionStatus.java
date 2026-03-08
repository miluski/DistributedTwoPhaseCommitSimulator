package com.distributed2pc.common.enums;

/**
 * Lifecycle states of a 2PC transaction, as seen by the coordinator.
 */
public enum TransactionStatus {
    /** Transaction object created; preparation not yet started. */
    INITIATED,
    /** PREPARE sent to all participants; waiting for votes. */
    PREPARING,
    /** All votes received as YES; COMMIT broadcast in progress or complete. */
    COMMITTED,
    /** At least one NO vote or timeout; ABORT broadcast in progress or complete. */
    ABORTED,
    /**
     * Coordinator crashed after Phase 1; outcome determined by participant
     * election.
     * This state is only persisted if the coordinator restarts and cannot determine
     * the final outcome from participant logs.
     */
    UNCERTAIN
}
