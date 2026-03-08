/** Snapshot of key metrics at a point in time, used for before/after scenario comparisons. */
export interface MetricsSnapshot {
  committed: number;
  aborted: number;
  uncertain: number;
  avgDecisionMs: number;
}
