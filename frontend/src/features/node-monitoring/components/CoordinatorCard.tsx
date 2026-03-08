import { NODE_STATUS_COLOR } from '@features/node-monitoring/model';
import type { CoordinatorCardProps } from '@features/node-monitoring/types';
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
 * Displays the live health status and active faults of the coordinator node.
 */
export default function CoordinatorCard({ coordinatorStatus }: Readonly<CoordinatorCardProps>) {
  const status = coordinatorStatus?.status ?? 'ONLINE';
  const activeFaults = coordinatorStatus?.activeFaults ?? [];
  const serverId = coordinatorStatus?.serverId ?? 'coordinator';

  return (
    <div
      className={clsx(
        'rounded-2xl p-4 border text-slate-100 flex flex-col gap-2.5 shadow-lg ring-1 ring-inset ring-violet-500/20',
        NODE_STATUS_COLOR[status] ?? 'bg-emerald-950/40 border-emerald-700/40'
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

      <span className="text-[10px] font-semibold uppercase tracking-wider text-violet-300 bg-violet-500/15 border border-violet-500/25 px-1.5 py-0.5 rounded self-start">
        KOORDYNATOR
      </span>

      <span className="text-xs text-slate-400">{STATUS_LABEL[status] ?? status}</span>

      {activeFaults.length > 0 && (
        <p className="text-[11px] text-amber-400/80 bg-amber-500/5 border border-amber-500/15 rounded-lg px-2.5 py-1.5 leading-relaxed">
          {activeFaults.join(', ')}
        </p>
      )}
    </div>
  );
}
