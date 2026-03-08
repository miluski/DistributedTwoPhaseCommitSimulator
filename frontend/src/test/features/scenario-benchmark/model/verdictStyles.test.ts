import { VERDICT_STYLES } from '@features/scenario-benchmark/model/verdictStyles';
import { describe, expect, it } from 'vitest';

describe('VERDICT_STYLES', () => {
  it('givenPassVerdict_hasGreenBadgeAndCheckIcon', () => {
    expect(VERDICT_STYLES['PASS'].badge).toContain('green');
    expect(VERDICT_STYLES['PASS'].icon).toContain('Zaliczone');
  });

  it('givenFailVerdict_hasRedBadgeAndCrossIcon', () => {
    expect(VERDICT_STYLES['FAIL'].badge).toContain('red');
    expect(VERDICT_STYLES['FAIL'].icon).toContain('Niezaliczone');
  });

  it('givenDegradedVerdict_hasYellowBadgeAndWarningIcon', () => {
    expect(VERDICT_STYLES['DEGRADED'].badge).toContain('yellow');
    expect(VERDICT_STYLES['DEGRADED'].icon).toContain('Częściowo');
  });

  it('givenExport_hasExactlyThreeVerdicts', () => {
    expect(Object.keys(VERDICT_STYLES)).toHaveLength(3);
  });

  it.each(['PASS', 'FAIL', 'DEGRADED'] as const)(
    'givenVerdict_%s_hasBothBadgeAndIconFields',
    (verdict) => {
      expect(VERDICT_STYLES[verdict]).toHaveProperty('badge');
      expect(VERDICT_STYLES[verdict]).toHaveProperty('icon');
    }
  );
});
