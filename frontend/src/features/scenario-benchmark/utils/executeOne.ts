import type { ParticipantInfo } from '@common/types';
import type { Scenario, ScenarioRunResult } from '@features/scenario-benchmark/types';
import { makeErrorResult } from './makeErrorResult';
import { snapshotMetrics } from './snapshotMetrics';

/**
 * Executes a single scenario and wraps the result in a {@link ScenarioRunResult}.
 *
 * @param scenario     the scenario to execute.
 * @param participants current list of registered participant nodes.
 * @returns pair of the scenario and its run result.
 */
export async function executeOne(
  scenario: Scenario,
  participants: readonly ParticipantInfo[]
): Promise<ScenarioRunResult> {
  const before = await snapshotMetrics().catch(() => ({
    committed: 0,
    aborted: 0,
    uncertain: 0,
    avgDecisionMs: 0,
  }));
  const result = await scenario.run(participants).catch((e) => makeErrorResult(e, before));
  return { scenario, result };
}
