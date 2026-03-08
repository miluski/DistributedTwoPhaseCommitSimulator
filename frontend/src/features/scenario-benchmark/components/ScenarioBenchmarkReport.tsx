import { VERDICT_STYLES } from '@features/scenario-benchmark/model';
import type { ScenarioBenchmarkReportProps } from '@features/scenario-benchmark/types';
import { formatAsText } from '@features/scenario-benchmark/utils';
import MetricsTable from './MetricsTable';

/**
 * Displays a single scenario run result with verdict badge, step log,
 * metrics delta table, duration, and a copy-to-clipboard button.
 *
 * @param result the structured scenario run result.
 * @param label  the scenario display label shown in the header.
 */
export default function ScenarioBenchmarkReport({ result, label }: Readonly<ScenarioBenchmarkReportProps>) {
  const style = VERDICT_STYLES[result.verdict];

  function handleCopy() {
    navigator.clipboard.writeText(formatAsText(label, result)).catch(() => {});
  }

  return (
    <div
      data-testid="benchmark-report"
      className="bg-slate-800/50 border border-slate-700 rounded-xl p-4 mt-3 text-sm"
    >
      <p className="text-xs text-slate-500 mb-1 font-semibold">{label}</p>
      <div className="flex items-center gap-3 mb-2">
        <span
          data-testid="verdict-badge"
          className={`text-xs font-bold px-2.5 py-0.5 rounded-full border ${style.badge}`}
        >
          {style.icon}
        </span>
        <span className="text-xs text-slate-500 tabular-nums">{result.durationMs} ms</span>
        <button
          data-testid="copy-report-btn"
          onClick={handleCopy}
          className="text-xs text-cyan-400 hover:text-cyan-300 transition-colors ml-auto"
        >
          Kopiuj raport
        </button>
      </div>

      <p
        data-testid="result-summary"
        className="text-slate-300 font-medium mb-2 leading-snug text-xs"
      >
        {result.summary}
      </p>

      <ol
        data-testid="result-steps"
        className="text-xs text-slate-400 space-y-0.5 list-decimal list-inside font-mono"
      >
        {result.steps.map((step) => (
          <li key={step}>{step}</li>
        ))}
      </ol>

      <MetricsTable before={result.metricsBefore} after={result.metricsAfter} />
    </div>
  );
}
