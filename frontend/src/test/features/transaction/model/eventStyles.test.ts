import { EVENT_STYLES } from '@features/transaction/model/eventStyles';
import { describe, expect, it } from 'vitest';

const EXPECTED_EVENT_TYPES = [
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
  'FAULT_INJECTED',
  'FAULT_CLEARED',
];

describe('EVENT_STYLES', () => {
  it('givenExport_containsAllExpectedEventTypes', () => {
    EXPECTED_EVENT_TYPES.forEach((type) => {
      expect(EVENT_STYLES).toHaveProperty(type);
    });
  });

  it.each(EXPECTED_EVENT_TYPES)('givenEventType_%s_hasIconAndColorClass', (eventType) => {
    const style = EVENT_STYLES[eventType];
    expect(style.icon).toBeTruthy();
    expect(style.colorClass).toBeTruthy();
  });

  it('givenTransactionStarted_hasBlueColorClass', () => {
    expect(EVENT_STYLES['TRANSACTION_STARTED'].colorClass).toContain('blue');
  });

  it('givenCommitSent_hasGreenColorClass', () => {
    expect(EVENT_STYLES['COMMIT_SENT'].colorClass).toContain('green');
  });

  it('givenAbortSent_hasRedColorClass', () => {
    expect(EVENT_STYLES['ABORT_SENT'].colorClass).toContain('red');
  });
});
