import type { ScenarioCategory } from '@features/scenario-benchmark/types';

/** Ordered list of scenario categories shown in the benchmark UI. */
export const CATEGORIES: ScenarioCategory[] = [
  'baseline',
  'single-fault',
  'compound',
  'extreme',
  'redundancy',
];
