import { formatPayload } from '@features/transaction/utils';
import { describe, expect, it } from 'vitest';

describe('formatPayload', () => {
  it('formatPayload_givenTransactionStarted_returnsValueString', () => {
    const result = formatPayload('TRANSACTION_STARTED', { value: 'hello' });
    expect(result).toBe('wartość: "hello"');
  });

  it('formatPayload_givenTransactionStartedWithoutValue_returnsEmpty', () => {
    const result = formatPayload('TRANSACTION_STARTED', {});
    expect(result).toBe('');
  });

  it('formatPayload_givenVoteReceivedWithYes_returnsYesMark', () => {
    const result = formatPayload('VOTE_RECEIVED', { vote: 'YES' });
    expect(result).toBe('✅ TAK');
  });

  it('formatPayload_givenVoteReceivedWithNo_returnsNoMark', () => {
    const result = formatPayload('VOTE_RECEIVED', { vote: 'NO' });
    expect(result).toBe('❌ NIE');
  });

  it('formatPayload_givenAllVotesCollected_returnsCounts', () => {
    const result = formatPayload('ALL_VOTES_COLLECTED', { yesCount: 4, noCount: 2 });
    expect(result).toBe('TAK: 4 / NIE: 2');
  });

  it('formatPayload_givenDecisionMadeCommitted_returnsCommittedLabel', () => {
    const result = formatPayload('DECISION_MADE', { decision: 'COMMITTED' });
    expect(result).toBe('✅ ZATWIERDZONA');
  });

  it('formatPayload_givenDecisionMadeAborted_returnsAbortedLabel', () => {
    const result = formatPayload('DECISION_MADE', { decision: 'ABORTED' });
    expect(result).toBe('❌ PRZERWANA');
  });

  it('formatPayload_givenUnknownEventType_returnsEmpty', () => {
    const result = formatPayload('PREPARE_SENT', { anything: 'value' });
    expect(result).toBe('');
  });
});
