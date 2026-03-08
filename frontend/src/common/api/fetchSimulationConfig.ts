import type { SimulationConfig } from '@common/types';

const BASE = '/api';

/** Fetches the current simulation configuration from the coordinator. */
export async function fetchSimulationConfig(): Promise<SimulationConfig> {
  const res = await fetch(`${BASE}/simulation/config`);
  if (!res.ok) throw new Error(`Failed to fetch simulation config: ${res.status}`);
  return res.json();
}
