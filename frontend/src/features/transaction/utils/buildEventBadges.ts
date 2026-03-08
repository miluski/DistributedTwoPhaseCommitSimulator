import type { EventType } from '@common/types';
import { FAULT_LABELS } from '@features/fault-injection/model';
import type { EventBadge } from '@features/transaction/types';

const BADGE_CLASSES = {
  success: 'border border-emerald-500/30 bg-emerald-500/15 text-emerald-300',
  error: 'border border-rose-500/30 bg-rose-500/15 text-rose-300',
  info: 'border border-sky-500/30 bg-sky-500/15 text-sky-300',
  warning: 'border border-orange-500/30 bg-orange-500/15 text-orange-300',
  neutral: 'border border-slate-600/40 bg-slate-700/40 text-slate-400',
} as const;

type BadgeBuilder = (payload: Record<string, unknown>) => EventBadge[];

const BADGE_BUILDERS: Partial<Record<EventType, BadgeBuilder>> = {
  TRANSACTION_STARTED: (p) => {
    const value = p['value'];
    return typeof value === 'string'
      ? [{ label: `wartość: "${value}"`, colorClass: BADGE_CLASSES.info }]
      : [];
  },
  VOTE_RECEIVED: (p) => {
    const vote = p['vote'];
    if (vote === 'YES') return [{ label: '✅ TAK', colorClass: BADGE_CLASSES.success }];
    if (vote === 'NO') return [{ label: '❌ NIE', colorClass: BADGE_CLASSES.error }];
    return [];
  },
  ALL_VOTES_COLLECTED: (p) => {
    const yes = p['yesCount'];
    const no = p['noCount'];
    const badges: EventBadge[] = [];
    if (typeof yes === 'number')
      badges.push({ label: `✅ TAK: ${yes}`, colorClass: BADGE_CLASSES.success });
    if (typeof no === 'number')
      badges.push({ label: `❌ NIE: ${no}`, colorClass: BADGE_CLASSES.error });
    return badges;
  },
  DECISION_MADE: (p) => {
    const decision = p['decision'];
    const yes = p['yesCount'];
    const no = p['noCount'];
    const verdictBadge: EventBadge =
      decision === 'COMMITTED'
        ? { label: '✅ ZATWIERDZONA', colorClass: BADGE_CLASSES.success }
        : { label: '❌ PRZERWANA', colorClass: BADGE_CLASSES.error };
    const badges = [verdictBadge];
    if (typeof yes === 'number' && typeof no === 'number') {
      badges.push({ label: `${yes} TAK / ${no} NIE`, colorClass: BADGE_CLASSES.neutral });
    }
    return badges;
  },
  FAULT_INJECTED: (p) => {
    const faultType = p['faultType'];
    if (typeof faultType !== 'string') return [];
    const label = FAULT_LABELS[faultType as keyof typeof FAULT_LABELS] ?? faultType;
    return [{ label: `⚡ ${label}`, colorClass: BADGE_CLASSES.warning }];
  },
  FAULT_CLEARED: (p) => {
    const faultType = p['faultType'];
    if (typeof faultType !== 'string') return [];
    const label = FAULT_LABELS[faultType as keyof typeof FAULT_LABELS] ?? faultType;
    return [{ label: `🛡 ${label}`, colorClass: BADGE_CLASSES.neutral }];
  },
};

/**
 * Builds styled badge descriptors for the relevant fields of an event payload.
 *
 * @param eventType the type of the system event.
 * @param payload the raw event payload map.
 * @returns array of badge descriptors, empty when no badge applies.
 */
export function buildEventBadges(
  eventType: EventType,
  payload: Record<string, unknown>
): EventBadge[] {
  const builder = BADGE_BUILDERS[eventType];
  return builder ? builder(payload) : [];
}
