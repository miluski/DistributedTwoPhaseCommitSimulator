import type { SystemEvent } from '@common/types';
import { buildEventBadges } from '@features/transaction/utils';

/**
 * Renders a row of coloured badges derived from a protocol event's payload.
 *
 * @param event the protocol event whose payload is used to build the badges.
 */
export default function BadgeRow({ event }: Readonly<{ event: SystemEvent }>) {
  const badges = buildEventBadges(event.eventType, event.payload);
  if (badges.length === 0) return null;
  return (
    <div className="flex flex-wrap gap-1 mt-1.5">
      {badges.map((badge) => (
        <span
          key={badge.label}
          className={`inline-flex items-center text-[10px] font-semibold px-1.5 py-0.5 rounded-md ${badge.colorClass}`}
        >
          {badge.label}
        </span>
      ))}
    </div>
  );
}
