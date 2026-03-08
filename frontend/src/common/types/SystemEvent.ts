import type { EventType } from './EventType';

/** Real-time system event broadcast over WebSocket. */
export interface SystemEvent {
  eventType: EventType;
  transactionId: string | null;
  sourceNodeId: string;
  targetNodeId: string | null;
  timestamp: string;
  payload: Record<string, unknown>;
}
