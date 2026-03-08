import type { ScenarioVerdict } from '@features/scenario-benchmark/types';

/** Badge, icon, and Polish label for each scenario verdict. */
export const VERDICT_STYLES: Record<
  ScenarioVerdict,
  { badge: string; icon: string; label: string }
> = {
  PASS: {
    badge: 'bg-green-800 text-green-100 border border-green-600',
    icon: '\u2713 Zaliczone',
    label: 'Zaliczone',
  },
  FAIL: {
    badge: 'bg-red-800 text-red-100 border border-red-600',
    icon: '\u2717 Niezaliczone',
    label: 'Niezaliczone',
  },
  DEGRADED: {
    badge: 'bg-yellow-800 text-yellow-100 border border-yellow-600',
    icon: '\u26a0 Częściowo',
    label: 'Częściowo',
  },
};
