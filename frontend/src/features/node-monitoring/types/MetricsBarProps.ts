import type { MetricsResponse } from '@common/types';

/** Props for the {@link MetricsBar} component. */
export interface MetricsBarProps {
  /** Aggregate stats from the coordinator, or null while loading. */
  readonly metrics: MetricsResponse | null;
  /** Current redundancy mode across all nodes. */
  readonly redundancyEnabled: boolean;
  /** Whether a mode-change request is in flight. */
  readonly toggling: boolean;
  /** Called when the user clicks the redundancy toggle button. */
  readonly onToggle: () => void;
}
