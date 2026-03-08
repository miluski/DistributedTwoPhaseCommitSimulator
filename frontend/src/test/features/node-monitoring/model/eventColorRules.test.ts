import { EVENT_COLOR_RULES } from '@features/node-monitoring/model/eventColorRules';
import { describe, expect, it } from 'vitest';

describe('EVENT_COLOR_RULES', () => {
  it('givenExport_hasFourRules', () => {
    expect(EVENT_COLOR_RULES).toHaveLength(4);
  });

  it('givenCommittedEventType_matchesGreenRule', () => {
    const rule = EVENT_COLOR_RULES.find((r) => r.test('TRANSACTION_COMMITTED'));
    expect(rule?.color).toBe('text-green-400');
  });

  it('givenTransactionCompleted_matchesGreenRule', () => {
    const rule = EVENT_COLOR_RULES.find((r) => r.test('TRANSACTION_COMPLETED'));
    expect(rule?.color).toBe('text-green-400');
  });

  it('givenAbortEventType_matchesRedRule', () => {
    const rule = EVENT_COLOR_RULES.find((r) => r.test('ABORT_SENT'));
    expect(rule?.color).toBe('text-red-400');
  });

  it('givenCrashEventType_matchesRedRule', () => {
    const rule = EVENT_COLOR_RULES.find((r) => r.test('COORDINATOR_CRASH'));
    expect(rule?.color).toBe('text-red-400');
  });

  it('givenFaultInjected_matchesRedRule', () => {
    const rule = EVENT_COLOR_RULES.find((r) => r.test('FAULT_INJECTED'));
    expect(rule?.color).toBe('text-red-400');
  });

  it('givenVoteEventType_matchesYellowRule', () => {
    const rule = EVENT_COLOR_RULES.find((r) => r.test('VOTE_RECEIVED'));
    expect(rule?.color).toBe('text-yellow-300');
  });

  it('givenElectionEventType_matchesPurpleRule', () => {
    const rule = EVENT_COLOR_RULES.find((r) => r.test('ELECTION_STARTED'));
    expect(rule?.color).toBe('text-purple-400');
  });

  it('givenUnknownEventType_matchesNoRule', () => {
    const matchingRules = EVENT_COLOR_RULES.filter((r) => r.test('UNKNOWN_EVENT'));
    expect(matchingRules).toHaveLength(0);
  });
});
