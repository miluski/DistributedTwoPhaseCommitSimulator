import { EVENT_TYPE_LABELS } from '@features/node-monitoring/model';
import type { EventLogProps } from '@features/node-monitoring/types';
import { eventColor } from '@features/node-monitoring/utils';

/**
 * Scrollable, reverse-chronological event log showing all system events received
 * from the coordinator WebSocket.
 */
export default function EventLog({ events }: EventLogProps) {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg flex flex-col h-96">
      <h2 className="text-sm font-semibold text-slate-300 mb-3 flex items-center gap-2">
        <span className="w-1.5 h-1.5 rounded-full bg-violet-400" />
        <span>Dziennik zdarzeń</span>
        <span className="ml-auto text-[10px] font-normal text-slate-600 tabular-nums">
          {events.length} zdarzeń
        </span>
      </h2>
      <div className="flex-1 overflow-y-auto font-mono space-y-1.5 pr-1">
        {events.length === 0 && <p className="text-slate-600 text-xs">Oczekiwanie na zdarzenia…</p>}
        {[...events].reverse().map((evt) => (
          <div
            key={`${evt.timestamp}-${evt.sourceNodeId}-${evt.eventType}`}
            className="flex gap-2 items-baseline py-0.5 border-b border-slate-800/50 last:border-0"
          >
            <span className="text-[10px] text-slate-600 shrink-0 tabular-nums">
              {new Date(evt.timestamp).toLocaleTimeString()}
            </span>
            <span className={`text-xs ${eventColor(evt.eventType)} font-semibold shrink-0`}>
              {EVENT_TYPE_LABELS[evt.eventType] ?? evt.eventType}
            </span>
            <span className="text-xs text-slate-500 truncate">
              {evt.sourceNodeId}
              {evt.transactionId ? ` · ${evt.transactionId.slice(0, 8)}` : ''}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
