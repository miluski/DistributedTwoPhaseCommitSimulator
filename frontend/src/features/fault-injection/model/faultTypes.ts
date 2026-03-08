import type { FaultType } from '@features/fault-injection/types';

/** Ordered list of all injectable fault types. */
export const FAULT_TYPES: FaultType[] = [
  'CRASH',
  'NETWORK_DELAY',
  'FORCE_ABORT_VOTE',
  'MESSAGE_LOSS',
  'TRANSIENT',
  'INTERMITTENT',
];

/** Fault types supported by the coordinator node. */
export const COORDINATOR_FAULT_TYPES: FaultType[] = ['CRASH', 'NETWORK_DELAY', 'MESSAGE_LOSS'];
