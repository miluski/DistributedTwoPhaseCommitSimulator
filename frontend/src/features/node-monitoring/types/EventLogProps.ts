import type { SystemEvent } from '@common/types';

/** Props for the {@link EventLog} component. */
export interface EventLogProps {
  /** All system events received from the coordinator WebSocket. */
  readonly events: readonly SystemEvent[];
}
