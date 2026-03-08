import { NODE_STATUS_COLOR } from '@features/node-monitoring/model';
import type { NodeCardProps } from '@features/node-monitoring/types';
import clsx from 'clsx';

const STATUS_DOT: Record<string, string> = {
  ONLINE: 'bg-emerald-400',
  CRASHED: 'bg-rose-500',
  DEGRADED: 'bg-amber-400',
};

const STATUS_LABEL: Record<string, string> = {
  ONLINE: 'Online',
  CRASHED: 'Awaria',
  DEGRADED: 'Zdegradowany',
};

/**
 * Displays the live status, vote, and fault information for a single participant node.
 */
export default function NodeCard({
  serverId,
  port,
  status,
  activeFaults,
  lastVote,
  committedValue,
  onInjectFault,
}: NodeCardProps) {
  const faults = activeFaults ?? [];
  return (
    <div
      className={clsx(
        'rounded-2xl p-4 border text-slate-100 flex flex-col gap-2.5 shadow-lg',
        NODE_STATUS_COLOR[status]
      )}
    >
      <div className="flex items-center justify-between gap-1">
        <span className="font-bold text-sm truncate">{serverId}</span>
        <span
          className={clsx(
            'w-2 h-2 rounded-full flex-shrink-0',
            STATUS_DOT[status] ?? 'bg-slate-500',
            status === 'ONLINE' && 'animate-pulse'
          )}
        />
      </div>

      <div className="flex items-center justify-between gap-1">
        <span className="text-[10px] font-semibold uppercase tracking-wider text-slate-500 bg-slate-800/70 px-1.5 py-0.5 rounded">
          UCZESTNIK
        </span>
        <span className="text-xs text-slate-400 font-mono">:{port}</span>
      </div>

      <span className="text-xs text-slate-400">{STATUS_LABEL[status] ?? status}</span>

      {lastVote && (
        <span
          className={clsx(
            'self-start text-xs font-semibold px-2 py-0.5 rounded-full border',
            lastVote === 'YES'
              ? 'text-emerald-300 bg-emerald-500/10 border-emerald-500/20'
              : 'text-rose-300 bg-rose-500/10 border-rose-500/20'
          )}
        >
          {lastVote === 'YES' ? '✅ TAK (YES)' : '❌ NIE (NO)'}
        </span>
      )}

      {faults.length > 0 && (
        <p className="text-[11px] text-amber-400/80 bg-amber-500/5 border border-amber-500/15 rounded-lg px-2.5 py-1.5 leading-relaxed">
          {faults.join(', ')}
        </p>
      )}

      {committedValue != null && (
        <div className="text-xs text-cyan-300/80 font-mono bg-cyan-500/5 border border-cyan-500/15 rounded-lg px-2.5 py-1.5">
          <span className="text-slate-500 text-[10px] mr-1">wartość:</span>
          {committedValue}
        </div>
      )}

      {onInjectFault && (
        <button
          className="mt-auto w-full bg-slate-700/80 hover:bg-slate-600 text-slate-200 text-xs rounded-lg px-2 py-2 font-medium transition-colors border border-slate-600/40"
          onClick={() => onInjectFault(serverId, port)}
        >
          Wstrzyknij błąd
        </button>
      )}
    </div>
  );
}
