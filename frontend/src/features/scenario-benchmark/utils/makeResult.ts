import type { MetricsSnapshot } from '@common/types';
import type { ScenarioResult, ScenarioVerdict } from '@features/scenario-benchmark/types';

/**
 * Constructs a {@link ScenarioResult} from the collected run data.
 *
 * @param verdict  overall pass/fail/degraded outcome.
 * @param summary  human-readable one-line outcome description.
 * @param steps    ordered list of executed step descriptions.
 * @param before   metrics snapshot taken before the scenario.
 * @param after    metrics snapshot taken after the scenario.
 * @param startMs  epoch milliseconds when the scenario started.
 * @returns completed scenario result.
 */
export function makeResult(
  verdict: ScenarioVerdict,
  summary: string,
  steps: string[],
  before: MetricsSnapshot,
  after: MetricsSnapshot,
  startMs: number
): ScenarioResult {
  return {
    verdict,
    summary,
    steps,
    durationMs: Date.now() - startMs,
    metricsBefore: before,
    metricsAfter: after,
  };
}
