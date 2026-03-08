/** Fault types that can be injected into any node. */
export type FaultType =
  | 'CRASH'
  | 'NETWORK_DELAY'
  | 'FORCE_ABORT_VOTE'
  | 'MESSAGE_LOSS'
  | 'TRANSIENT'
  | 'INTERMITTENT';
