import type { ScenarioCategory } from '@features/scenario-benchmark/types';

/** Human-readable Polish label for each scenario category. */
export const CATEGORY_LABELS: Record<ScenarioCategory, string> = {
  baseline: 'Punkty odniesienia',
  'single-fault': 'Pojedynczy błąd',
  compound: 'Błędy złożone',
  extreme: 'Sytuacje ekstremalne',
  redundancy: 'Porównanie redundancji',
};
