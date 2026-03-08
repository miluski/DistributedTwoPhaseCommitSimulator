import type { NodeStatus } from './NodeStatus';

/** Registered participant as returned by the coordinator. */
export interface ParticipantInfo {
  serverId: string;
  host: string;
  port: number;
  status: NodeStatus;
  activeFaults: string[];
  lastVote?: string | null;
  committedValue?: string | null;
}
