import * as api from '@common/api';
import { fetchCoordinatorStatus } from '@common/api';
import { describe, expect, it, vi } from 'vitest';

vi.mock('@common/api');

describe('fetchCoordinatorStatus', () => {
  it('fetchCoordinatorStatus_givenSuccessfulResponse_returnsStatusData', async () => {
    const mockResponse = {
      serverId: 'coordinator',
      status: 'ONLINE' as const,
      activeFaults: [],
    };
    vi.mocked(api.fetchCoordinatorStatus).mockResolvedValue(mockResponse);

    const result = await fetchCoordinatorStatus();

    expect(result).toEqual(mockResponse);
  });

  it('fetchCoordinatorStatus_givenCrashedCoordinator_returnsCrashedStatus', async () => {
    const mockResponse = {
      serverId: 'coordinator',
      status: 'CRASHED' as const,
      activeFaults: ['CRASH'],
    };
    vi.mocked(api.fetchCoordinatorStatus).mockResolvedValue(mockResponse);

    const result = await fetchCoordinatorStatus();

    expect(result.status).toBe('CRASHED');
    expect(result.activeFaults).toContain('CRASH');
  });

  it('fetchCoordinatorStatus_givenNetworkError_throwsError', async () => {
    vi.mocked(api.fetchCoordinatorStatus).mockRejectedValue(new Error('network error'));

    await expect(fetchCoordinatorStatus()).rejects.toThrow('network error');
  });
});
