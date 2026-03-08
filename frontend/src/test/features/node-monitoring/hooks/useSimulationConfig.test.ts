import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as api from '@common/api';
import type { ParticipantInfo, SimulationConfig } from '@common/types';
import { useSimulationConfig } from '@features/node-monitoring/hooks/useSimulationConfig';

vi.mock('@common/api');

const CONFIG: SimulationConfig = { redundancyEnabled: true };
const DISABLED_CONFIG: SimulationConfig = { redundancyEnabled: false };

const PARTICIPANTS: ParticipantInfo[] = [
  { serverId: 'p1', host: 'localhost', port: 9001, status: 'ONLINE', activeFaults: [] },
  { serverId: 'p2', host: 'localhost', port: 9002, status: 'ONLINE', activeFaults: [] },
];

describe('useSimulationConfig', () => {
  beforeEach(() => vi.clearAllMocks());

  it('initialState_givenMount_defaultsToRedundancyEnabled', () => {
    vi.mocked(api.fetchSimulationConfig).mockResolvedValue(CONFIG);
    const { result } = renderHook(() => useSimulationConfig(PARTICIPANTS));

    expect(result.current.redundancyEnabled).toBe(true);
  });

  it('load_givenRemoteConfigFalse_updatesRedundancyEnabled', async () => {
    vi.mocked(api.fetchSimulationConfig).mockResolvedValue(DISABLED_CONFIG);
    const { result } = renderHook(() => useSimulationConfig(PARTICIPANTS));

    await act(async () => {
      await Promise.resolve();
    });

    expect(result.current.redundancyEnabled).toBe(false);
  });

  it('initialState_givenMount_togglingIsFalse', () => {
    vi.mocked(api.fetchSimulationConfig).mockResolvedValue(CONFIG);
    const { result } = renderHook(() => useSimulationConfig(PARTICIPANTS));

    expect(result.current.toggling).toBe(false);
  });

  it('toggleRedundancy_givenRedundancyEnabled_disablesOnAllNodes', async () => {
    vi.mocked(api.fetchSimulationConfig).mockResolvedValue(CONFIG);
    vi.mocked(api.updateCoordinatorSimulationConfig).mockResolvedValue(DISABLED_CONFIG);
    vi.mocked(api.updateParticipantSimulationConfig).mockResolvedValue(DISABLED_CONFIG);
    const { result } = renderHook(() => useSimulationConfig(PARTICIPANTS));

    await act(async () => {
      await result.current.toggleRedundancy();
    });

    expect(api.updateCoordinatorSimulationConfig).toHaveBeenCalledWith(false);
    expect(api.updateParticipantSimulationConfig).toHaveBeenCalledWith(9001, false);
    expect(api.updateParticipantSimulationConfig).toHaveBeenCalledWith(9002, false);
    expect(result.current.redundancyEnabled).toBe(false);
  });

  it('toggleRedundancy_givenRedundancyDisabled_enablesOnAllNodes', async () => {
    vi.mocked(api.fetchSimulationConfig).mockResolvedValue(DISABLED_CONFIG);
    vi.mocked(api.updateCoordinatorSimulationConfig).mockResolvedValue(CONFIG);
    vi.mocked(api.updateParticipantSimulationConfig).mockResolvedValue(CONFIG);
    const { result } = renderHook(() => useSimulationConfig(PARTICIPANTS));

    await act(async () => {
      await Promise.resolve();
    });

    await act(async () => {
      await result.current.toggleRedundancy();
    });

    expect(api.updateCoordinatorSimulationConfig).toHaveBeenCalledWith(true);
    expect(result.current.redundancyEnabled).toBe(true);
  });

  it('toggleRedundancy_givenCoordinatorError_stillClearsToggling', async () => {
    vi.mocked(api.fetchSimulationConfig).mockResolvedValue(CONFIG);
    vi.mocked(api.updateCoordinatorSimulationConfig).mockRejectedValue(new Error('fail'));
    const { result } = renderHook(() => useSimulationConfig(PARTICIPANTS));

    await act(async () => {
      await result.current.toggleRedundancy().catch(() => {});
    });

    expect(result.current.toggling).toBe(false);
  });
});
