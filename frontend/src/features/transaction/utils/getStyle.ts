import { EVENT_STYLES } from '@features/transaction/model';
import type { EventStyle } from '@features/transaction/types';

/**
 * Returns the icon and colour class for the given event type,
 * falling back to a neutral style for unknown types.
 *
 * @param eventType the 2PC event type string.
 * @returns matching {@link EventStyle} or a neutral fallback.
 */
export function getStyle(eventType: string): EventStyle {
  return EVENT_STYLES[eventType] ?? { icon: '•', colorClass: 'border-gray-500 text-gray-400' };
}
