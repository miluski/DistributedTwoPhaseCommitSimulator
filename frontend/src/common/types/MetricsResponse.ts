/** Aggregate transaction outcome statistics returned by GET /api/metrics. */
export interface MetricsResponse {
  total: number;
  committed: number;
  aborted: number;
  uncertain: number;
  inProgress: number;
  avgDecisionMs: number;
  redundancyEnabled: boolean;
}
