import type { MetricsSnapshot } from '@common/types';
import { makeErrorResult } from '@features/scenario-benchmark/utils/makeErrorResult';
import { describe, expect, it, vi } from 'vitest';

const BEFORE: MetricsSnapshot = {
  committed: 5,
  aborted: 3,
  uncertain: 0,
  avgDecisionMs: 100,
};

describe('makeErrorResult', () => {
  it('givenErrorInstance_usesErrorMessage', () => {
    const result = makeErrorResult(new Error('something broke'), BEFORE);

    expect(result.verdict).toBe('FAIL');
    expect(result.summary).toContain('something broke');
    expect(result.steps[0]).toContain('something broke');
  });

  it('givenNonErrorValue_stringifiesIt', () => {
    const result = makeErrorResult('plain string error', BEFORE);

    expect(result.verdict).toBe('FAIL');
    expect(result.summary).toContain('plain string error');
    expect(result.steps[0]).toContain('plain string error');
  });

  it('givenNumberThrown_stringifiesIt', () => {
    const result = makeErrorResult(42, BEFORE);

    expect(result.verdict).toBe('FAIL');
    expect(result.summary).toContain('42');
  });

  it('givenError_copiesBeforeSnapshotForAfter', () => {
    const result = makeErrorResult(new Error('fail'), BEFORE);

    expect(result.metricsBefore).toEqual(BEFORE);
    expect(result.metricsAfter).toEqual(BEFORE);
  });

  it('givenError_populatesDurationMs', () => {
    vi.useFakeTimers();
    vi.setSystemTime(1000);

    const result = makeErrorResult(new Error('fail'), BEFORE);

    expect(result.durationMs).toBeGreaterThanOrEqual(0);
    vi.useRealTimers();
  });
});
