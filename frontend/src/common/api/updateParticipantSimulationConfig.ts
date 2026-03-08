import type { SimulationConfig } from '@common/types';

/** Updates the simulation configuration on a single participant node. */
export async function updateParticipantSimulationConfig(
  port: number,
  redundancyEnabled: boolean
): Promise<SimulationConfig> {
  const res = await fetch(`/participant-proxy/${port}/api/simulation/config`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ redundancyEnabled }),
  });
  if (!res.ok)
    throw new Error(`Failed to update participant config on port ${port}: ${res.status}`);
  return res.json();
}
