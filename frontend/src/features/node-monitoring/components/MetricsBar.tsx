import { percentage } from '@common/utils';
import type { MetricsBarProps } from '@features/node-monitoring/types';

/**
 * Horizontal bar displaying aggregate transaction outcome statistics and a
 * toggle button for enabling or disabling the redundancy protocol.
 *
 * @param metrics aggregate stats from the coordinator, or {@code null} while loading.
 * @param redundancyEnabled current redundancy mode across all nodes.
 * @param toggling whether a mode-change request is in flight.
 * @param onToggle called when the user clicks the redundancy toggle button.
 */
export default function MetricsBar({
  metrics,
  redundancyEnabled,
  toggling,
  onToggle,
}: MetricsBarProps) {
  const total = metrics?.total ?? 0;
  const redundancyActiveLabel = redundancyEnabled ? 'Redundancja WŁ' : 'Redundancja WYŁ';
  const redundancyLabel = toggling ? 'Aktualizacja…' : redundancyActiveLabel;

  return (
    <div
      data-testid="metrics-bar"
      className="bg-slate-900 border border-slate-800 rounded-2xl px-4 py-4 shadow-lg"
    >
      <div className="flex flex-wrap items-center gap-2">
        <span className="flex items-center gap-1.5 rounded-lg bg-slate-800/70 border border-slate-700/50 px-3 py-2 text-xs">
          <span className="text-slate-500">Łącznie:</span>
          <strong className="text-slate-100 font-bold tabular-nums">{total}</strong>
        </span>

        <span className="flex items-center gap-1.5 rounded-lg bg-emerald-500/10 border border-emerald-500/15 px-3 py-2 text-xs">
          <span className="text-slate-500">Zatwierdzone:</span>
          <strong className="text-emerald-400 tabular-nums">{metrics?.committed ?? 0}</strong>
          <span className="text-slate-600 text-[10px]">
            ({percentage(metrics?.committed ?? 0, total)}%)
          </span>
        </span>

        <span className="flex items-center gap-1.5 rounded-lg bg-rose-500/10 border border-rose-500/15 px-3 py-2 text-xs">
          <span className="text-slate-500">Przerwane:</span>
          <strong className="text-rose-400 tabular-nums">{metrics?.aborted ?? 0}</strong>
          <span className="text-slate-600 text-[10px]">
            ({percentage(metrics?.aborted ?? 0, total)}%)
          </span>
        </span>

        <span className="flex items-center gap-1.5 rounded-lg bg-amber-500/10 border border-amber-500/15 px-3 py-2 text-xs">
          <span className="text-slate-500">Niepewne:</span>
          <strong className="text-amber-400 tabular-nums">{metrics?.uncertain ?? 0}</strong>
        </span>

        <span className="flex items-center gap-1.5 rounded-lg bg-sky-500/10 border border-sky-500/15 px-3 py-2 text-xs">
          <span className="text-slate-500">W toku:</span>
          <strong className="text-sky-400 tabular-nums">{metrics?.inProgress ?? 0}</strong>
        </span>

        <span className="flex items-center gap-1.5 rounded-lg bg-slate-800/40 border border-slate-700/30 px-3 py-2 text-xs">
          <span className="text-slate-500">Śr. decyzja:</span>
          <strong className="text-slate-200 tabular-nums">{metrics?.avgDecisionMs ?? 0} ms</strong>
        </span>

        <button
          data-testid="redundancy-toggle"
          onClick={onToggle}
          disabled={toggling}
          className={`ml-auto px-4 py-2 rounded-lg text-xs font-semibold border transition-colors disabled:opacity-40 ${
            redundancyEnabled
              ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30 hover:bg-emerald-500/20'
              : 'bg-rose-500/10 text-rose-400 border-rose-500/30 hover:bg-rose-500/20'
          }`}
        >
          {redundancyLabel}
        </button>
      </div>
    </div>
  );
}
