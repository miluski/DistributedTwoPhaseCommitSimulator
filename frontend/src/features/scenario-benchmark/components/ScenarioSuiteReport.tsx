import { SUITE_ROW_VERDICT, VERDICT_STYLES } from '@features/scenario-benchmark/model';
import type { ScenarioSuiteReportProps } from '@features/scenario-benchmark/types';
import { formatSuiteAsText } from '@features/scenario-benchmark/utils';

/**
 * Displays the aggregated result of running a suite of fault scenarios,
 * including per-scenario verdict, summary, and overall pass/fail statistics.
 *
 * @param report the aggregated suite run report.
 */
export default function ScenarioSuiteReport({ report }: ScenarioSuiteReportProps) {
  function handleCopy() {
    navigator.clipboard.writeText(formatSuiteAsText(report)).catch(() => {});
  }

  return (
    <div
      data-testid="suite-report"
      className="bg-slate-800/50 border border-slate-700 rounded-xl p-4 mt-3 text-sm"
    >
      <div className="flex flex-wrap items-center gap-3 mb-3">
        <h3 className="font-semibold text-slate-200">Raport serii</h3>
        <div className="flex gap-3 text-xs ml-auto">
          <span className="text-emerald-400">✓ {report.passed} Zal.</span>
          <span className="text-rose-400">✗ {report.failed} Niezal.</span>
          <span className="text-amber-400">⚠ {report.degraded} Częściowo</span>
          <span className="text-slate-500 tabular-nums">{report.totalDurationMs} ms</span>
        </div>
        <button
          data-testid="copy-suite-btn"
          onClick={handleCopy}
          className="text-xs text-cyan-400 hover:text-cyan-300 transition-colors"
        >
          Kopiuj raport
        </button>
      </div>

      <table className="w-full text-xs border-collapse">
        <thead>
          <tr className="border-b border-slate-700 text-slate-500">
            <th className="text-left py-1.5 px-2">Werdykt</th>
            <th className="text-left py-1.5 px-2">Scenariusz</th>
            <th className="text-left py-1.5 px-2">Podsumowanie</th>
            <th className="text-right py-1.5 px-2">ms</th>
          </tr>
        </thead>
        <tbody>
          {report.results.map((r) => (
            <tr
              key={r.scenarioId}
              data-testid={`suite-row-${r.scenarioId}`}
              className="border-b border-slate-800"
            >
              <td className={`py-1.5 px-2 font-bold ${SUITE_ROW_VERDICT[r.result.verdict]}`}>
                {VERDICT_STYLES[r.result.verdict].label}
              </td>
              <td className="py-1.5 px-2 text-slate-300 max-w-[160px] truncate">{r.label}</td>
              <td className="py-1.5 px-2 text-slate-500 text-xs leading-tight">
                {r.result.summary}
              </td>
              <td className="py-1.5 px-2 text-right text-slate-600 tabular-nums">
                {r.result.durationMs}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
