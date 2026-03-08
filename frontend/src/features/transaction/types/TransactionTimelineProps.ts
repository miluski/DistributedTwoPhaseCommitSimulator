import type { SystemEvent } from '@common/types';

/** Props for the {@link TransactionTimeline} component. */
export interface TransactionTimelineProps {
  /** Full ordered event buffer from the WebSocket subscription. */
  readonly events: readonly SystemEvent[];
  /** Transaction ID to filter on, or {@code null} to show the most recent events. */
  readonly selectedTxId: string | null;
  /** Optional callback to clear the current transaction selection. */
  readonly onClearSelection?: () => void;
}
