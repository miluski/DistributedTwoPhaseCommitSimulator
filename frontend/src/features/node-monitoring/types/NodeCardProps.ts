import type { NodeStatus } from '@common/types';

/** Props for the {@link NodeCard} component. */
export interface NodeCardProps {
  /** Unique server identifier. */
  readonly serverId: string;
  /** HTTP port the participant listens on. */
  readonly port: number;
  /** Current operational status of the node. */
  readonly status: NodeStatus;
  /** Names of currently active fault types on this node. */
  readonly activeFaults: readonly string[];
  /** The node's most recent transaction vote, or null if none. */
  readonly lastVote?: string | null;
  /** The most recently committed value on this node, or null if none. */
  readonly committedValue?: string | null;
  /** Optional callback invoked when the user triggers fault injection from the card. */
  readonly onInjectFault?: (serverId: string, port: number) => void;
}
