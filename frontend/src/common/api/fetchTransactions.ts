import type { TransactionResponse } from '@common/types';

const BASE = '/api';

/** Fetches all transactions from the coordinator. */
export async function fetchTransactions(): Promise<TransactionResponse[]> {
  const res = await fetch(`${BASE}/transactions`);
  if (!res.ok) throw new Error(`Failed to fetch transactions: ${res.status}`);
  const data: unknown = await res.json();
  return Array.isArray(data) ? data : [];
}
