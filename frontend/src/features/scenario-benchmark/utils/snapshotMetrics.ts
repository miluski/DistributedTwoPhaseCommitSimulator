import { fetchMetrics } from '@common/api';
import type { MetricsSnapshot } from '@common/types';

/**
 * Fetches the current coordinator metrics and maps them to a lightweight snapshot.
 *
 * @returns current metrics snapshot.
 */
export async function snapshotMetrics(): Promise<MetricsSnapshot> {
  const m = await fetchMetrics();
  return {
    committed: m.committed,
    aborted: m.aborted,
    uncertain: m.uncertain,
    avgDecisionMs: m.avgDecisionMs,
  };
}
