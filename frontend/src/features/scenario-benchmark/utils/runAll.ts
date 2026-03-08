import type { ParticipantInfo } from '@common/types';
import type { Scenario, SuiteReport } from '@features/scenario-benchmark/types';
import { buildReport } from './buildReport';
import { executeOne } from './executeOne';

/**
 * Runs every scenario in {@code toRun} sequentially, reporting progress after each step.
 *
 * @param toRun        ordered list of scenarios to execute.
 * @param participants list of registered participant nodes.
 * @param onProgress   callback invoked with {@code (current, total)} after each scenario.
 * @returns aggregated suite report.
 */
export async function runAll(
  toRun: Scenario[],
  participants: readonly ParticipantInfo[],
  onProgress: (current: number, total: number) => void
): Promise<SuiteReport> {
  const results: SuiteReport['results'] = [];
  const suiteStart = Date.now();
  for (let i = 0; i < toRun.length; i++) {
    onProgress(i + 1, toRun.length);
    const { scenario, result } = await executeOne(toRun[i], participants);
    results.push({ scenarioId: scenario.id, label: scenario.label, result });
  }
  return buildReport(results, Date.now() - suiteStart);
}
