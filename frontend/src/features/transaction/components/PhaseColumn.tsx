import type { SystemEvent } from '@common/types';
import EventItem from './EventItem';

/**
 * Renders a single phase column (Phase 1 or Phase 2) of the transaction
 * timeline, displaying the phase header and a scrollable ordered event list.
 *
 * @param marker      the circled numeral displayed in the column header.
 * @param title       the full phase title displayed in the column header.
 * @param phaseEvents the events belonging to this phase.
 */
export default function PhaseColumn({
  marker,
  title,
  phaseEvents,
}: Readonly<{
  marker: string;
  title: string;
  phaseEvents: SystemEvent[];
}>) {
  return (
    <div className="flex flex-col">
      <div className="flex items-center gap-2 mb-3 pb-2 border-b border-slate-800">
        <span className="text-sky-400 font-bold">{marker}</span>
        <span className="text-[11px] font-bold uppercase tracking-widest text-slate-500">
          {title}
        </span>
      </div>
      {phaseEvents.length === 0 ? (
        <p className="text-slate-700 text-xs italic">Brak zdarzeń w tej fazie.</p>
      ) : (
        <ol className="border-l border-slate-800 ml-1 space-y-1 overflow-y-auto max-h-[32rem] pr-1">
          {phaseEvents.map((event) => (
            <EventItem
              key={`${event.timestamp}-${event.sourceNodeId}-${event.eventType}`}
              event={event}
            />
          ))}
        </ol>
      )}
    </div>
  );
}
