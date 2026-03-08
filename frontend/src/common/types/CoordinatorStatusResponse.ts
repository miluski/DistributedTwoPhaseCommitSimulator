import type { NodeStatus } from './NodeStatus';

/** Status response returned by {@code GET /api/coordinator/status}. */
export interface CoordinatorStatusResponse {
  serverId: string;
  status: NodeStatus;
  activeFaults: string[];
}
