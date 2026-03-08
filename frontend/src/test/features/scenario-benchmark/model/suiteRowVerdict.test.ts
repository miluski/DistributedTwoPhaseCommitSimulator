import { SUITE_ROW_VERDICT } from '@features/scenario-benchmark/model/suiteRowVerdict';
import { describe, expect, it } from 'vitest';

describe('SUITE_ROW_VERDICT', () => {
  it('givenPassVerdict_hasGreenTextClass', () => {
    expect(SUITE_ROW_VERDICT['PASS']).toBe('text-green-400');
  });

  it('givenFailVerdict_hasRedTextClass', () => {
    expect(SUITE_ROW_VERDICT['FAIL']).toBe('text-red-400');
  });

  it('givenDegradedVerdict_hasYellowTextClass', () => {
    expect(SUITE_ROW_VERDICT['DEGRADED']).toBe('text-yellow-400');
  });

  it('givenExport_hasExactlyThreeVerdicts', () => {
    expect(Object.keys(SUITE_ROW_VERDICT)).toHaveLength(3);
  });
});
