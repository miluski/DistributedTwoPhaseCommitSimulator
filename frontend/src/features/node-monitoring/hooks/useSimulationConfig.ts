import {
  fetchSimulationConfig,
  updateCoordinatorSimulationConfig,
  updateParticipantSimulationConfig,
} from '@common/api';
import type { ParticipantInfo } from '@common/types';
import { useCallback, useEffect, useState } from 'react';

/**
 * Manages the redundancy toggle across the coordinator and all registered
 * participants.
 *
 * <p>Calling {@link toggleRedundancy} broadcasts the new mode to the coordinator
 * and every participant simultaneously, enabling direct comparison of system
 * behaviour with and without the recovery protocol.
 *
 * @param participants list used to propagate the config change to each node.
 * @returns current mode flag and a toggle function.
 */
export function useSimulationConfig(participants: ParticipantInfo[]): {
  redundancyEnabled: boolean;
  toggling: boolean;
  toggleRedundancy: () => Promise<void>;
} {
  const [redundancyEnabled, setRedundancyEnabled] = useState(true);
  const [toggling, setToggling] = useState(false);

  useEffect(() => {
    fetchSimulationConfig()
      .then((cfg) => setRedundancyEnabled(cfg.redundancyEnabled))
      .catch(() => {});
  }, []);

  const toggleRedundancy = useCallback(async () => {
    const next = !redundancyEnabled;
    setToggling(true);
    try {
      await updateCoordinatorSimulationConfig(next);
      await Promise.allSettled(
        participants.map((p) => updateParticipantSimulationConfig(p.port, next))
      );
      setRedundancyEnabled(next);
    } finally {
      setToggling(false);
    }
  }, [redundancyEnabled, participants]);

  return { redundancyEnabled, toggling, toggleRedundancy };
}
