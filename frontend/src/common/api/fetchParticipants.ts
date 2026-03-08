import type { ParticipantInfo } from '@common/types';

const BASE = '/api';

/** Fetches all registered participants from the coordinator. */
export async function fetchParticipants(): Promise<ParticipantInfo[]> {
  const res = await fetch(`${BASE}/participants`);
  if (!res.ok) throw new Error(`Failed to fetch participants: ${res.status}`);
  const data: unknown = await res.json();
  return Array.isArray(data) ? data : [];
}
