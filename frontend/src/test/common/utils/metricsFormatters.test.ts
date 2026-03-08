import type { MetricsSnapshot } from '@common/types';
import { buildMetricRows, deltaClass, formatDelta, percentage } from '@common/utils';
import { describe, expect, it } from 'vitest';

describe('percentage', () => {
  it('percentage_givenZeroTotal_returnsZeroString', () => {
    expect(percentage(5, 0)).toBe('0');
  });

  it('percentage_givenNonZeroTotal_returnsRoundedPercentage', () => {
    expect(percentage(1, 4)).toBe('25');
  });
});

describe('buildMetricRows', () => {
  it('buildMetricRows_givenTwoSnapshots_returnsFourRows', () => {
    const before: MetricsSnapshot = {
      committed: 1,
      aborted: 2,
      uncertain: 3,
      avgDecisionMs: 10,
    };
    const after: MetricsSnapshot = {
      committed: 4,
      aborted: 5,
      uncertain: 6,
      avgDecisionMs: 20,
    };
    const rows = buildMetricRows(before, after);
    expect(rows).toHaveLength(4);
    expect(rows[0]).toEqual({ label: 'Zatwierdzone', before: 1, after: 4, unit: '' });
    expect(rows[3]).toEqual({ label: 'Śr. decyzja', before: 10, after: 20, unit: 'ms' });
  });
});

describe('deltaClass', () => {
  it('deltaClass_givenPositiveDelta_returnsYellow', () => {
    expect(deltaClass(5)).toBe('text-yellow-300');
  });

  it('deltaClass_givenNegativeDelta_returnsGreen', () => {
    expect(deltaClass(-3)).toBe('text-green-300');
  });

  it('deltaClass_givenZeroDelta_returnsGray', () => {
    expect(deltaClass(0)).toBe('text-gray-500');
  });
});

describe('formatDelta', () => {
  it('formatDelta_givenZero_returnsPlusMinusZero', () => {
    expect(formatDelta(0, 'ms')).toBe('±0ms');
  });

  it('formatDelta_givenPositiveInteger_returnsPlusSign', () => {
    expect(formatDelta(3, '')).toBe('+3');
  });

  it('formatDelta_givenNegativeInteger_returnsNegativeSign', () => {
    expect(formatDelta(-2, 'ms')).toBe('-2ms');
  });

  it('formatDelta_givenNonIntegerDelta_returnsOneDecimalPlace', () => {
    expect(formatDelta(1.5, 'ms')).toBe('+1.5ms');
  });
});
