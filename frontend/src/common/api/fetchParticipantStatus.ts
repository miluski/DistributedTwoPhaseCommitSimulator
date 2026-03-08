/** Response shape returned by a participant's {@code GET /api/status} endpoint. */
export interface ParticipantStatusResponse {
  serverId: string;
  status: string;
  activeFaults: string[];
  committedValue: string | null;
}

/**
 * Fetches the live status of a single participant via the nginx reverse-proxy.
 *
 * @param port The participant's port number used to route through the proxy.
 * @returns Resolved status response, or {@code null} if the participant is unreachable.
 */
export async function fetchParticipantStatus(
  port: number
): Promise<ParticipantStatusResponse | null> {
  try {
    const res = await fetch(`/participant-proxy/${port}/api/status`);
    if (!res.ok) return null;
    const data: unknown = await res.json();
    return data as ParticipantStatusResponse;
  } catch {
    return null;
  }
}
