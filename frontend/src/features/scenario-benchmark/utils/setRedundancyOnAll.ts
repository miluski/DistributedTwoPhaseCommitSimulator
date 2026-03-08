import { updateCoordinatorSimulationConfig, updateParticipantSimulationConfig } from '@common/api';
import type { ParticipantInfo } from '@common/types';

/**
 * Enables or disables redundancy on all nodes (coordinator and all participants).
 *
 * @param ps      list of registered participant nodes.
 * @param enabled target redundancy state.
 */
export async function setRedundancyOnAll(
  ps: readonly ParticipantInfo[],
  enabled: boolean
): Promise<void> {
  await updateCoordinatorSimulationConfig(enabled);
  await Promise.allSettled(ps.map((p) => updateParticipantSimulationConfig(p.port, enabled)));
}
