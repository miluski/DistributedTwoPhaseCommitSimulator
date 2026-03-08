import type { Scenario } from './Scenario';
import type { ScenarioResult } from './ScenarioResult';

/** Pair of a scenario definition and its most recent run result. */
export interface ScenarioRunResult {
  scenario: Scenario;
  result: ScenarioResult;
}
