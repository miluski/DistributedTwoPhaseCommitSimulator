import { CATEGORIES } from '@features/scenario-benchmark/model/categories';
import { describe, expect, it } from 'vitest';

describe('CATEGORIES', () => {
  it('givenExport_containsAllFiveCategories', () => {
    expect(CATEGORIES).toHaveLength(5);
  });

  it.each([['baseline'], ['single-fault'], ['compound'], ['extreme'], ['redundancy']] as const)(
    'givenCategory_%s_isPresentInList',
    (cat) => {
      expect(CATEGORIES).toContain(cat);
    }
  );

  it('givenExport_isOrderedCorrectly', () => {
    expect(CATEGORIES[0]).toBe('baseline');
    expect(CATEGORIES[4]).toBe('redundancy');
  });
});
