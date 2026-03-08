import type { NodeStatus } from '@common/types';

/** Maps each node status to its Tailwind background and border classes. */
export const NODE_STATUS_COLOR: Record<NodeStatus, string> = {
  ONLINE: 'bg-emerald-950/40 border-emerald-700/40',
  CRASHED: 'bg-rose-950/40 border-rose-700/40',
  DEGRADED: 'bg-amber-950/40 border-amber-700/40',
};
