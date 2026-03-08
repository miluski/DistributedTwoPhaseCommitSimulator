import type { MetricsSnapshot } from '@common/types';
import { buildMetricRows, deltaClass, formatDelta } from '@common/utils';

/**
 * Renders a delta comparison table between two {@link MetricsSnapshot} values.
 *
 * @param before metrics captured before the scenario run.
 * @param after  metrics captured after the scenario run.
 */
export default function MetricsTable({
  before,
  after,
}: {
  readonly before: MetricsSnapshot;
  readonly after: MetricsSnapshot;
}) {
  const rows = buildMetricRows(before, after);
  return (
    <table className="w-full text-xs mt-3 border-collapse">
      <thead>
        <tr className="border-b border-slate-700 text-slate-500">
          <th className="text-left py-1.5 px-2">Metryka</th>
          <th className="text-right py-1.5 px-2">Przed</th>
          <th className="text-right py-1.5 px-2">Po</th>
          <th className="text-right py-1.5 px-2">Δ</th>
        </tr>
      </thead>
      <tbody>
        {rows.map((r) => {
          const delta = r.after - r.before;
          return (
            <tr key={r.label} className="border-b border-slate-800">
              <td className="py-1.5 px-2 text-slate-400">{r.label}</td>
              <td className="py-1.5 px-2 text-right text-slate-500 tabular-nums">
                {r.before}
                {r.unit}
              </td>
              <td className="py-1.5 px-2 text-right text-slate-300 tabular-nums">
                {r.after}
                {r.unit}
              </td>
              <td className={`py-1.5 px-2 text-right font-mono tabular-nums ${deltaClass(delta)}`}>
                {formatDelta(delta, r.unit)}
              </td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}
