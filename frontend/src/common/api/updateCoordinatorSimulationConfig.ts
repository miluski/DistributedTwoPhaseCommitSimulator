import type { SimulationConfig } from '@common/types';

const BASE = '/api';

/** Updates the simulation configuration on the coordinator. */
export async function updateCoordinatorSimulationConfig(
  redundancyEnabled: boolean
): Promise<SimulationConfig> {
  const res = await fetch(`${BASE}/simulation/config`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ redundancyEnabled }),
  });
  if (!res.ok) throw new Error(`Failed to update simulation config: ${res.status}`);
  return res.json();
}
