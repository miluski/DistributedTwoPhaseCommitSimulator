import * as api from '@common/api';
import type { CoordinatorStatusResponse } from '@common/types';
import { useCoordinatorStatus } from '@features/node-monitoring/hooks/useCoordinatorStatus';
import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@common/api');

const ONLINE_STATUS: CoordinatorStatusResponse = {
  serverId: 'coordinator',
  status: 'ONLINE',
  activeFaults: [],
};

const CRASHED_STATUS: CoordinatorStatusResponse = {
  serverId: 'coordinator',
  status: 'CRASHED',
  activeFaults: ['CRASH'],
};

describe('useCoordinatorStatus', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('initialState_givenMount_returnsNullStatus', () => {
    vi.mocked(api.fetchCoordinatorStatus).mockResolvedValue(ONLINE_STATUS);
    const { result } = renderHook(() => useCoordinatorStatus());

    expect(result.current.coordinatorStatus).toBeNull();
  });

  it('load_givenSuccessfulFetch_setsCoordinatorStatus', async () => {
    vi.mocked(api.fetchCoordinatorStatus).mockResolvedValue(ONLINE_STATUS);
    const { result } = renderHook(() => useCoordinatorStatus());

    await act(async () => {
      await Promise.resolve();
    });

    expect(result.current.coordinatorStatus).toEqual(ONLINE_STATUS);
  });

  it('load_givenCrashedCoordinator_returnsCrashedStatus', async () => {
    vi.mocked(api.fetchCoordinatorStatus).mockResolvedValue(CRASHED_STATUS);
    const { result } = renderHook(() => useCoordinatorStatus());

    await act(async () => {
      await Promise.resolve();
    });

    expect(result.current.coordinatorStatus?.status).toBe('CRASHED');
  });

  it('load_givenFetchError_keepsNullStatus', async () => {
    vi.mocked(api.fetchCoordinatorStatus).mockRejectedValue(new Error('network error'));
    const { result } = renderHook(() => useCoordinatorStatus());

    await act(async () => {
      await Promise.resolve();
    });

    expect(result.current.coordinatorStatus).toBeNull();
  });

  it('poll_givenIntervalElapsed_fetchesAgain', async () => {
    vi.mocked(api.fetchCoordinatorStatus).mockResolvedValue(ONLINE_STATUS);
    renderHook(() => useCoordinatorStatus());

    await act(async () => {
      await Promise.resolve();
    });

    await act(async () => {
      vi.advanceTimersByTime(3_000);
      await Promise.resolve();
    });

    expect(api.fetchCoordinatorStatus).toHaveBeenCalledTimes(2);
  });
});
