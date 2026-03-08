import { EVENT_COLOR_RULES } from '@features/node-monitoring/model';

/**
 * Resolves a Tailwind colour class for a given event type using the rule table.
 *
 * @param type the event type string.
 * @returns Tailwind text-colour class, defaulting to text-gray-300.
 */
export function eventColor(type: string): string {
  return EVENT_COLOR_RULES.find((rule) => rule.test(type))?.color ?? 'text-gray-300';
}
