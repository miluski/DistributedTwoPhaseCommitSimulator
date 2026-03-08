import type { SuiteReport } from '@features/scenario-benchmark/types';

/**
 * Serialises an entire suite report to a plain-text summary string.
 *
 * @param report the aggregated suite run report.
 * @returns multi-line text suitable for clipboard copy.
 */
export function formatSuiteAsText(report: SuiteReport): string {
  const header = [
    '=== FAULT TOLERANCE BENCHMARK SUITE REPORT ===',
    `Total scenarios : ${report.results.length}`,
    `PASS            : ${report.passed}`,
    `FAIL            : ${report.failed}`,
    `DEGRADED        : ${report.degraded}`,
    `Total duration  : ${report.totalDurationMs} ms`,
    '',
    'Results:',
  ].join('\n');
  const rows = report.results
    .map((r) => `  [${r.result.verdict.padEnd(8)}] ${r.label}\n             ${r.result.summary}`)
    .join('\n');
  return `${header}\n${rows}\n`;
}
