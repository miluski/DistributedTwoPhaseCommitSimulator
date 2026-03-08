package com.distributed2pc.common.dto;

/**
 * Response DTO used by the coordinator's metrics endpoint to expose transaction
 * outcome statistics and the current simulation configuration to the UI.
 *
 * @param total             total number of transactions initiated.
 * @param committed         number of successfully committed transactions.
 * @param aborted           number of aborted transactions (any-NO or
 *                          coordinator-forced).
 * @param uncertain         number of transactions left in an unresolved state.
 * @param inProgress        number of transactions still in INITIATED or
 *                          PREPARING state.
 * @param avgDecisionMs     average elapsed time (ms) from INITIATED to
 *                          COMMIT/ABORT decision;
 *                          {@code 0} when no decided transaction exists yet.
 * @param redundancyEnabled whether the election-based recovery protocol is
 *                          currently active.
 */
public record MetricsResponse(
        long total,
        long committed,
        long aborted,
        long uncertain,
        long inProgress,
        long avgDecisionMs,
        boolean redundancyEnabled) {
}
