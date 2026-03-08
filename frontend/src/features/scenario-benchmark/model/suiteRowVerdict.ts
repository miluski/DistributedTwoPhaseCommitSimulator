import type { ScenarioVerdict } from '@features/scenario-benchmark/types';

/** Tailwind text colour class for each verdict in a suite results row. */
export const SUITE_ROW_VERDICT: Record<ScenarioVerdict, string> = {
  PASS: 'text-green-400',
  FAIL: 'text-red-400',
  DEGRADED: 'text-yellow-400',
};
