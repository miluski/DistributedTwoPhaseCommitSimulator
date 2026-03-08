import { buildMetricRows, formatDelta } from '@common/utils';
import type { ScenarioResult } from '@features/scenario-benchmark/types';

/**
 * Serialises a single scenario result to a plain-text report string.
 *
 * @param label human-readable scenario label.
 * @param result the structured scenario run result.
 * @returns multi-line text suitable for clipboard copy.
 */
export function formatAsText(label: string, result: ScenarioResult): string {
  const rows = buildMetricRows(result.metricsBefore, result.metricsAfter);
  const stepLines = result.steps.map((s, i) => `  ${i + 1}. ${s}`).join('\n');
  const metricsLines = rows
    .map(
      (r) =>
        `  ${r.label.padEnd(14)}: ${r.before} → ${r.after} (${formatDelta(r.after - r.before, r.unit)})`
    )
    .join('\n');
  return [
    `=== SCENARIO: ${label} ===`,
    `Verdict  : ${result.verdict}`,
    `Summary  : ${result.summary}`,
    `Duration : ${result.durationMs} ms`,
    `Steps:`,
    stepLines,
    `Metrics Delta:`,
    metricsLines,
    '',
  ].join('\n');
}
