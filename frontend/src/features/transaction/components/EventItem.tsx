import type { SystemEvent } from '@common/types';
import { EVENT_LABELS } from '@features/transaction/model';
import { formatTime, getStyle } from '@features/transaction/utils';
import BadgeRow from './BadgeRow';

/**
 * Renders a single protocol event as a list item with icon, label, timestamp,
 * source/target node identifiers, and an optional badge row.
 *
 * @param event the protocol event to render.
 */
export default function EventItem({ event }: Readonly<{ event: SystemEvent }>) {
  const { icon, colorClass } = getStyle(event.eventType);
  return (
    <li className={`border-l-2 pl-3 py-1.5 ${colorClass}`}>
      <div className="flex items-baseline gap-2">
        <span className="text-base leading-none">{icon}</span>
        <span className="font-semibold text-xs">
          {EVENT_LABELS[event.eventType] ?? event.eventType}
        </span>
        <span className="text-slate-600 text-[10px] ml-auto tabular-nums">
          {formatTime(event.timestamp)}
        </span>
      </div>
      <div className="text-[11px] text-slate-500 mt-0.5">
        <span className="text-slate-400">{event.sourceNodeId}</span>
        {event.targetNodeId ? (
          <span className="text-slate-600">{` → ${event.targetNodeId}`}</span>
        ) : null}
      </div>
      <BadgeRow event={event} />
    </li>
  );
}
