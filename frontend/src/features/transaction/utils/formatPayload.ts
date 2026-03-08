import type { EventType } from '@common/types';

type PayloadFormatter = (payload: Record<string, unknown>) => string;

const PAYLOAD_FORMATTERS: Partial<Record<EventType, PayloadFormatter>> = {
  TRANSACTION_STARTED: (p) => {
    const value = p['value'];
    return typeof value === 'string' ? `wartość: "${value}"` : '';
  },
  VOTE_RECEIVED: (p) => {
    const vote = p['vote'];
    if (vote === 'YES') return '✅ TAK';
    if (vote === 'NO') return '❌ NIE';
    return '';
  },
  ALL_VOTES_COLLECTED: (p) => {
    const yes = p['yesCount'];
    const no = p['noCount'];
    if (typeof yes === 'number' && typeof no === 'number') return `TAK: ${yes} / NIE: ${no}`;
    return '';
  },
  DECISION_MADE: (p) => {
    const decision = p['decision'];
    if (decision === 'COMMITTED') return '✅ ZATWIERDZONA';
    if (decision === 'ABORTED') return '❌ PRZERWANA';
    return '';
  },
  FAULT_INJECTED: (p) => {
    const faultType = p['faultType'];
    return typeof faultType === 'string' ? `typ: ${faultType}` : '';
  },
  FAULT_CLEARED: (p) => {
    const faultType = p['faultType'];
    return typeof faultType === 'string' ? `typ: ${faultType}` : '';
  },
};

/**
 * Formats the event payload into a human-readable Polish summary string.
 *
 * @param eventType the type of the system event.
 * @param payload the raw event payload map.
 * @returns a formatted string, or an empty string when no formatter applies.
 */
export function formatPayload(eventType: EventType, payload: Record<string, unknown>): string {
  const formatter = PAYLOAD_FORMATTERS[eventType];
  return formatter ? formatter(payload) : '';
}
