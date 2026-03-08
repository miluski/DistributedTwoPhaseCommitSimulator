import { useState } from 'react';
import { initiateTransaction } from '@common/api';
import type { TransactionResponse } from '@common/types';

/**
 * Manages the lifecycle of a single 2PC transaction submission.
 *
 * @returns `result`, `loading`, `error` state and a `submit` function
 *          that triggers the coordinator API call.
 */
export function useTransactionSubmit() {
  const [result, setResult] = useState<TransactionResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * Initiates a 2PC transaction with the given value.
   *
   * @param value the transaction payload to commit.
   */
  async function submit(value: string) {
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const response = await initiateTransaction(value);
      setResult(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  }

  return { result, loading, error, submit };
}
