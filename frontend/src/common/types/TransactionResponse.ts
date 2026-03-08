import type { TransactionStatus } from './TransactionStatus';
import type { VoteResult } from './VoteResult';

/** Transaction response DTO from coordinator REST API. */
export interface TransactionResponse {
  transactionId: string;
  status: TransactionStatus;
  value: string;
  initiatedAt: string;
  decidedAt: string | null;
  votes: Record<string, VoteResult>;
}
