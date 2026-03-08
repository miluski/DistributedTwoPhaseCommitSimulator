import type { SuiteReport } from '@features/scenario-benchmark/types';

/**
 * Builds a {@link SuiteReport} from accumulated per-scenario results.
 *
 * @param results   array of scenario run entries.
 * @param totalMs   total wall-clock time for the whole suite in milliseconds.
 * @returns aggregated suite report.
 */
export function buildReport(results: SuiteReport['results'], totalMs: number): SuiteReport {
  return {
    results,
    passed: results.filter((r) => r.result.verdict === 'PASS').length,
    failed: results.filter((r) => r.result.verdict === 'FAIL').length,
    degraded: results.filter((r) => r.result.verdict === 'DEGRADED').length,
    totalDurationMs: totalMs,
  };
}
