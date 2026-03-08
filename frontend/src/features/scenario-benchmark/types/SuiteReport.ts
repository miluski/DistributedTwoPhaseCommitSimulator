import type { ScenarioResult } from './ScenarioResult';

/** Aggregate report produced by running a suite of scenarios. */
export interface SuiteReport {
  results: Array<{ scenarioId: string; label: string; result: ScenarioResult }>;
  passed: number;
  failed: number;
  degraded: number;
  totalDurationMs: number;
}
