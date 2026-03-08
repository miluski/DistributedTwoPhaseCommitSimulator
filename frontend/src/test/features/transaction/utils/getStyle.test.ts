import { getStyle } from '@features/transaction/utils/getStyle';
import { describe, expect, it } from 'vitest';

describe('getStyle', () => {
  it('givenKnownEventType_returnsMatchingStyle', () => {
    const style = getStyle('TRANSACTION_STARTED');

    expect(style.icon).toBe('🚀');
    expect(style.colorClass).toContain('blue');
  });

  it('givenCommitSentType_returnsGreenStyle', () => {
    const style = getStyle('COMMIT_SENT');

    expect(style.icon).toBe('✅');
    expect(style.colorClass).toContain('green');
  });

  it('givenUnknownEventType_returnsFallbackStyle', () => {
    const style = getStyle('UNKNOWN_EVENT_TYPE');

    expect(style.icon).toBe('•');
    expect(style.colorClass).toBe('border-gray-500 text-gray-400');
  });

  it('givenEmptyString_returnsFallbackStyle', () => {
    const style = getStyle('');

    expect(style.icon).toBe('•');
    expect(style.colorClass).toContain('gray');
  });

  it.each([
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
  ])('givenEventType_%s_returnsNonFallbackStyle', (eventType) => {
    const style = getStyle(eventType);

    expect(style.icon).not.toBe('•');
  });
});
