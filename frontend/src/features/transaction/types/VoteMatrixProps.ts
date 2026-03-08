import type { TransactionResponse } from '@common/types';

/** Props for the {@link VoteMatrix} component. */
export interface VoteMatrixProps {
  /** List of transaction responses from the coordinator. */
  readonly transactions: readonly TransactionResponse[];
}
