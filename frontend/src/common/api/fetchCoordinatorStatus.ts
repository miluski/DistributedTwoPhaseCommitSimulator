import type { CoordinatorStatusResponse } from '@common/types';

const BASE = '/api';

/** Fetches the current health status and active faults of the coordinator node. */
export async function fetchCoordinatorStatus(): Promise<CoordinatorStatusResponse> {
  const res = await fetch(`${BASE}/coordinator/status`);
  if (!res.ok) throw new Error(`Failed to fetch coordinator status: ${res.status}`);
  return res.json();
}
