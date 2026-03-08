import { buildEventBadges } from '@features/transaction/utils';
import { describe, expect, it } from 'vitest';

describe('buildEventBadges', () => {
  it('buildEventBadges_givenTransactionStartedWithValue_returnsInfoBadge', () => {
    const result = buildEventBadges('TRANSACTION_STARTED', { value: 'hello' });
    expect(result).toHaveLength(1);
    expect(result[0].label).toBe('wartość: "hello"');
    expect(result[0].colorClass).toContain('text-sky-300');
  });

  it('buildEventBadges_givenTransactionStartedWithoutValue_returnsEmpty', () => {
    const result = buildEventBadges('TRANSACTION_STARTED', {});
    expect(result).toHaveLength(0);
  });

  it('buildEventBadges_givenVoteReceivedYes_returnsSuccessBadgeWithTak', () => {
    const result = buildEventBadges('VOTE_RECEIVED', { vote: 'YES' });
    expect(result).toHaveLength(1);
    expect(result[0].label).toBe('✅ TAK');
    expect(result[0].colorClass).toContain('text-emerald-300');
  });

  it('buildEventBadges_givenVoteReceivedNo_returnsErrorBadgeWithNie', () => {
    const result = buildEventBadges('VOTE_RECEIVED', { vote: 'NO' });
    expect(result).toHaveLength(1);
    expect(result[0].label).toBe('❌ NIE');
    expect(result[0].colorClass).toContain('text-rose-300');
  });

  it('buildEventBadges_givenAllVotesCollected_returnsTwoBadgesWithCounts', () => {
    const result = buildEventBadges('ALL_VOTES_COLLECTED', { yesCount: 3, noCount: 1 });
    expect(result).toHaveLength(2);
    expect(result[0].label).toBe('✅ TAK: 3');
    expect(result[1].label).toBe('❌ NIE: 1');
  });

  it('buildEventBadges_givenDecisionMadeCommitted_returnsVerdictBadge', () => {
    const result = buildEventBadges('DECISION_MADE', {
      decision: 'COMMITTED',
      yesCount: 3,
      noCount: 0,
    });
    expect(result).toHaveLength(2);
    expect(result[0].label).toBe('✅ ZATWIERDZONA');
    expect(result[0].colorClass).toContain('text-emerald-300');
  });

  it('buildEventBadges_givenDecisionMadeAborted_returnsAbortedVerdictBadge', () => {
    const result = buildEventBadges('DECISION_MADE', {
      decision: 'ABORTED',
      yesCount: 1,
      noCount: 2,
    });
    expect(result).toHaveLength(2);
    expect(result[0].label).toBe('❌ PRZERWANA');
    expect(result[0].colorClass).toContain('text-rose-300');
  });

  it('buildEventBadges_givenDecisionMadeWithCounts_returnsCountBadgeAsSecondEntry', () => {
    const result = buildEventBadges('DECISION_MADE', {
      decision: 'COMMITTED',
      yesCount: 4,
      noCount: 1,
    });
    expect(result[1].label).toBe('4 TAK / 1 NIE');
    expect(result[1].colorClass).toContain('text-slate-400');
  });

  it('buildEventBadges_givenUnknownEventType_returnsEmptyArray', () => {
    const result = buildEventBadges('PREPARE_SENT', {});
    expect(result).toHaveLength(0);
  });
});
