import { CATEGORIES } from '@features/scenario-benchmark/model/categories';
import { CATEGORY_LABELS } from '@features/scenario-benchmark/model/categoryLabels';
import { describe, expect, it } from 'vitest';

describe('CATEGORY_LABELS', () => {
  it('givenExport_hasLabelForEveryCategory', () => {
    CATEGORIES.forEach((cat) => {
      expect(CATEGORY_LABELS[cat]).toBeTruthy();
    });
  });

  it('givenBaseline_hasPolishLabel', () => {
    expect(CATEGORY_LABELS.baseline).toBe('Punkty odniesienia');
  });

  it('givenSingleFault_hasPolishLabel', () => {
    expect(CATEGORY_LABELS['single-fault']).toBe('Pojedynczy błąd');
  });

  it('givenCompound_hasPolishLabel', () => {
    expect(CATEGORY_LABELS.compound).toBe('Błędy złożone');
  });

  it('givenExtreme_hasPolishLabel', () => {
    expect(CATEGORY_LABELS.extreme).toBe('Sytuacje ekstremalne');
  });

  it('givenRedundancy_hasPolishLabel', () => {
    expect(CATEGORY_LABELS.redundancy).toBe('Porównanie redundancji');
  });
});
