import type { EventType } from '@common/types';
import { EVENT_LABELS } from '@features/transaction/model/eventLabels';
import { describe, expect, it } from 'vitest';

const ALL_EVENT_TYPES: EventType[] = [
  'TRANSACTION_STARTED',
  'PREPARE_SENT',
  'VOTE_RECEIVED',
  'ALL_VOTES_COLLECTED',
  'DECISION_MADE',
  'COMMIT_SENT',
  'ABORT_SENT',
  'TRANSACTION_COMPLETED',
  'TRANSACTION_TIMED_OUT',
  'COORDINATOR_CRASHED',
  'COORDINATOR_RECOVERED',
  'PARTICIPANT_CRASHED',
  'PARTICIPANT_RECOVERED',
  'ELECTION_STARTED',
  'ELECTION_RESULT',
  'PEER_CONSULTED',
  'FAULT_INJECTED',
  'FAULT_CLEARED',
  'PARTICIPANT_REGISTERED',
];

describe('EVENT_LABELS', () => {
  it('givenExport_hasNineteenEntries', () => {
    expect(Object.keys(EVENT_LABELS)).toHaveLength(19);
  });

  it.each(ALL_EVENT_TYPES)('givenEventType_%s_hasNonEmptyPolishLabel', (eventType) => {
    expect(typeof EVENT_LABELS[eventType]).toBe('string');
    expect(EVENT_LABELS[eventType].length).toBeGreaterThan(0);
  });

  it('givenTransactionStarted_labelMentionsTransakcja', () => {
    expect(EVENT_LABELS['TRANSACTION_STARTED'].toLowerCase()).toContain('transakcja');
  });

  it('givenDecisionMade_labelMentionsDecyzja', () => {
    expect(EVENT_LABELS['DECISION_MADE'].toLowerCase()).toContain('decyzja');
  });
});
