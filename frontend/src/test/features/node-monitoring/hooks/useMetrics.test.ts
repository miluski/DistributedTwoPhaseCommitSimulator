import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import * as api from '@common/api';
import type { MetricsResponse } from '@common/types';
import { useMetrics } from '@features/node-monitoring/hooks/useMetrics';

vi.mock('@common/api');

const METRICS: MetricsResponse = {
  total: 10,
  committed: 7,
  aborted: 2,
  uncertain: 1,
  inProgress: 0,
  avgDecisionMs: 150,
  redundancyEnabled: true,
};

describe('useMetrics', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('initialState_givenMount_returnsNullMetrics', () => {
    vi.mocked(api.fetchMetrics).mockResolvedValue(METRICS);
    const { result } = renderHook(() => useMetrics());

    expect(result.current.metrics).toBeNull();
  });

  it('load_givenSuccessfulFetch_setsMetrics', async () => {
    vi.mocked(api.fetchMetrics).mockResolvedValue(METRICS);
    const { result } = renderHook(() => useMetrics());

    await act(async () => {
      await Promise.resolve();
    });

    expect(result.current.metrics).toEqual(METRICS);
  });

  it('load_givenFetchError_keepsNullMetrics', async () => {
    vi.mocked(api.fetchMetrics).mockRejectedValue(new Error('network error'));
    const { result } = renderHook(() => useMetrics());

    await act(async () => {
      await Promise.resolve();
    });

    expect(result.current.metrics).toBeNull();
  });

  it('poll_givenIntervalElapsed_fetchesAgain', async () => {
    vi.mocked(api.fetchMetrics).mockResolvedValue(METRICS);
    renderHook(() => useMetrics());

    await act(async () => {
      await Promise.resolve();
      vi.advanceTimersByTime(3_000);
      await Promise.resolve();
    });

    expect(api.fetchMetrics).toHaveBeenCalledTimes(2);
  });

  it('unmount_givenCleanup_stopsPolling', async () => {
    vi.mocked(api.fetchMetrics).mockResolvedValue(METRICS);
    const { unmount } = renderHook(() => useMetrics());

    await act(async () => {
      await Promise.resolve();
    });
    unmount();

    await act(async () => {
      vi.advanceTimersByTime(6_000);
      await Promise.resolve();
    });

    expect(api.fetchMetrics).toHaveBeenCalledTimes(1);
  });
});
