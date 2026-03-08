import * as api from '@common/api';
import type { TransactionResponse } from '@common/types';
import { useTransactionSubmit } from '@features/transaction/hooks/useTransactionSubmit';
import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@common/api');

const COMMITTED: TransactionResponse = {
  transactionId: 'tx-123',
  status: 'COMMITTED',
  value: 'hello',
  initiatedAt: new Date().toISOString(),
  decidedAt: new Date().toISOString(),
  votes: {},
};

describe('useTransactionSubmit', () => {
  beforeEach(() => vi.clearAllMocks());

  it('submit_givenSuccess_setsResult', async () => {
    vi.mocked(api.initiateTransaction).mockResolvedValue(COMMITTED);
    const { result } = renderHook(() => useTransactionSubmit());

    await act(async () => {
      await result.current.submit('hello');
    });

    expect(result.current.result).toEqual(COMMITTED);
  });

  it('submit_givenError_setsErrorMessage', async () => {
    vi.mocked(api.initiateTransaction).mockRejectedValue(new Error('Network failure'));
    const { result } = renderHook(() => useTransactionSubmit());

    await act(async () => {
      await result.current.submit('hello');
    });

    expect(result.current.error).toBe('Network failure');
  });

  it('submit_givenSuccess_clearsError', async () => {
    vi.mocked(api.initiateTransaction).mockResolvedValue(COMMITTED);
    const { result } = renderHook(() => useTransactionSubmit());

    await act(async () => {
      await result.current.submit('hello');
    });

    expect(result.current.error).toBeNull();
  });

  it('submit_givenSuccess_clearsLoadingAfterCompletion', async () => {
    vi.mocked(api.initiateTransaction).mockResolvedValue(COMMITTED);
    const { result } = renderHook(() => useTransactionSubmit());

    await act(async () => {
      await result.current.submit('hello');
    });

    expect(result.current.loading).toBe(false);
  });

  it('submit_givenInitialState_hasNullResultAndNoError', () => {
    const { result } = renderHook(() => useTransactionSubmit());

    expect(result.current.result).toBeNull();
    expect(result.current.error).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('submit_givenNonErrorThrown_setsUnknownErrorMessage', async () => {
    vi.mocked(api.initiateTransaction).mockRejectedValue('plain rejection');
    const { result } = renderHook(() => useTransactionSubmit());

    await act(async () => {
      await result.current.submit('hello');
    });

    expect(result.current.error).toBe('Unknown error');
  });
});
