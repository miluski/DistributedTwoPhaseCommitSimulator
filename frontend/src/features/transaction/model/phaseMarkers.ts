import type { EventType } from '@common/types';
import type { PhaseMarker } from '@features/transaction/types';

/**
 * Maps 2PC protocol event types that open a new phase to their phase header labels.
 * Used by the TransactionTimeline to insert visual phase dividers.
 *
 * <p>ALL_VOTES_COLLECTED acts as a fallback Phase-2 trigger when DECISION_MADE
 * is not emitted by the backend (e.g. when votes arrive only via gossip).
 */
export const PHASE_MARKERS: Partial<Record<EventType, PhaseMarker>> = {
  PREPARE_SENT: { number: '①', label: 'Faza 1 — Przygotowanie' },
  ALL_VOTES_COLLECTED: { number: '②', label: 'Faza 2 — Decyzja' },
  DECISION_MADE: { number: '②', label: 'Faza 2 — Decyzja' },
};
