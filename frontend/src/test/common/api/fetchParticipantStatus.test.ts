import { fetchParticipantStatus } from '@common/api';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

describe('fetchParticipantStatus', () => {
  const mockFetch = vi.fn();

  beforeEach(() => {
    vi.stubGlobal('fetch', mockFetch);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('fetchParticipantStatus_givenOkResponse_returnsStatusData', async () => {
    const body = {
      serverId: 'participant-1',
      status: 'ONLINE',
      activeFaults: [],
      committedValue: 'hello',
    };
    mockFetch.mockResolvedValue({ ok: true, json: async () => body });

    const result = await fetchParticipantStatus(8444);

    expect(mockFetch).toHaveBeenCalledWith('/participant-proxy/8444/api/status');
    expect(result).toEqual(body);
  });

  it('fetchParticipantStatus_givenErrorResponse_returnsNull', async () => {
    mockFetch.mockResolvedValue({ ok: false });

    const result = await fetchParticipantStatus(8444);

    expect(result).toBeNull();
  });

  it('fetchParticipantStatus_givenNetworkException_returnsNull', async () => {
    mockFetch.mockRejectedValue(new Error('network error'));

    const result = await fetchParticipantStatus(8444);

    expect(result).toBeNull();
  });

  it('fetchParticipantStatus_givenNullCommittedValue_includesNullInResponse', async () => {
    const body = {
      serverId: 'participant-2',
      status: 'ONLINE',
      activeFaults: ['NETWORK_DELAY'],
      committedValue: null,
    };
    mockFetch.mockResolvedValue({ ok: true, json: async () => body });

    const result = await fetchParticipantStatus(8445);

    expect(result?.committedValue).toBeNull();
    expect(result?.activeFaults).toContain('NETWORK_DELAY');
  });
});
