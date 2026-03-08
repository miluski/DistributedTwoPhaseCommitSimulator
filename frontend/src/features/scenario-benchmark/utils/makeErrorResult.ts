import type { MetricsSnapshot } from '@common/types';
import type { ScenarioResult } from '@features/scenario-benchmark/types';
import { makeResult } from './makeResult';

/**
 * Constructs a FAIL result from an unexpected error thrown during a scenario.
 *
 * @param error  the caught error value.
 * @param before metrics snapshot taken before the scenario aborted.
 * @returns failed scenario result.
 */
export function makeErrorResult(error: unknown, before: MetricsSnapshot): ScenarioResult {
  const msg = error instanceof Error ? error.message : String(error);
  return makeResult(
    'FAIL',
    `Scenario error: ${msg}`,
    [`Error: ${msg}`],
    before,
    { ...before },
    Date.now()
  );
}
