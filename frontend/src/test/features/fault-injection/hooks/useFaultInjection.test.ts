import * as api from '@common/api';
import type { ParticipantInfo } from '@common/types';
import { useFaultInjection } from '@features/fault-injection/hooks/useFaultInjection';
import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@common/api');

const PARTICIPANTS: ParticipantInfo[] = [
  { serverId: 'server-1', host: 'localhost', port: 9001, status: 'ONLINE', activeFaults: [] },
];

describe('useFaultInjection', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(api.injectCoordinatorFault).mockResolvedValue(undefined);
    vi.mocked(api.injectFault).mockResolvedValue(undefined);
  });

  it('send_givenCoordinatorTarget_callsInjectCoordinatorFault', async () => {
    const { result } = renderHook(() => useFaultInjection(PARTICIPANTS));

    await act(async () => {
      await result.current.send(true);
    });

    expect(api.injectCoordinatorFault).toHaveBeenCalledWith('CRASH', true, {});
  });

  it('send_givenEnabled_setsStatusToInjected', async () => {
    const { result } = renderHook(() => useFaultInjection(PARTICIPANTS));

    await act(async () => {
      await result.current.send(true);
    });

    expect(result.current.status).toBe('Usterka wstrzyknięta.');
  });

  it('send_givenDisabled_setsStatusToCleared', async () => {
    const { result } = renderHook(() => useFaultInjection(PARTICIPANTS));

    await act(async () => {
      await result.current.send(false);
    });

    expect(result.current.status).toBe('Usterka wyczyszczona.');
  });

  it('send_givenParticipantTarget_callsInjectFaultWithPort', async () => {
    const { result } = renderHook(() => useFaultInjection(PARTICIPANTS));

    act(() => {
      result.current.setTarget('server-1');
    });
    await act(async () => {
      await result.current.send(true);
    });

    expect(api.injectFault).toHaveBeenCalledWith(9001, 'CRASH', true, {});
  });

  it('send_givenApiError_setsErrorMessage', async () => {
    vi.mocked(api.injectCoordinatorFault).mockRejectedValue(new Error('Network error'));
    const { result } = renderHook(() => useFaultInjection(PARTICIPANTS));

    await act(async () => {
      await result.current.send(true);
    });

    expect(result.current.error).toBe('Network error');
  });

  it('send_givenNetworkDelayType_includesDelayInParameters', async () => {
    const { result } = renderHook(() => useFaultInjection(PARTICIPANTS));

    act(() => {
      result.current.setFaultType('NETWORK_DELAY');
      result.current.setDelayMs('1000');
    });
    await act(async () => {
      await result.current.send(true);
    });

    expect(api.injectCoordinatorFault).toHaveBeenCalledWith('NETWORK_DELAY', true, {
      delayMs: 1000,
    });
  });

  it('send_givenUnknownParticipantTarget_setsErrorMessage', async () => {
    const { result } = renderHook(() => useFaultInjection(PARTICIPANTS));

    act(() => {
      result.current.setTarget('nonexistent');
    });
    await act(async () => {
      await result.current.send(true);
    });

    expect(result.current.error).toContain('Unknown participant');
  });

  it('send_givenNonErrorThrown_setsUnknownError', async () => {
    vi.mocked(api.injectCoordinatorFault).mockRejectedValue('plain rejection');
    const { result } = renderHook(() => useFaultInjection(PARTICIPANTS));

    await act(async () => {
      await result.current.send(true);
    });

    expect(result.current.error).toBe('Unknown error');
  });
});
