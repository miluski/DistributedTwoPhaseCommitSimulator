import { injectCoordinatorFault, injectFault } from '@common/api';
import type { ParticipantInfo } from '@common/types';

/**
 * Clears all known fault types from every participant and from the coordinator.
 *
 * @param participants list of registered participant nodes.
 */
export async function clearAllFaults(participants: readonly ParticipantInfo[]): Promise<void> {
  const ptypes = ['CRASH', 'NETWORK_DELAY', 'FORCE_ABORT_VOTE', 'INTERMITTENT', 'TRANSIENT'];
  await Promise.allSettled(
    participants.flatMap((p) => ptypes.map((t) => injectFault(p.port, t, false)))
  );
  await Promise.allSettled(
    ['CRASH', 'NETWORK_DELAY', 'MESSAGE_LOSS'].map((t) => injectCoordinatorFault(t, false))
  );
}
