/**
 * Extracts vote and participant information from an event payload.
 *
 * @param payload raw event payload map.
 * @returns formatted vote annotation string, or an empty string when absent.
 */
export function resolveVoteInfo(payload: Record<string, unknown>): string {
  const vote = payload['vote'];
  const participant = payload['participantId'];
  if (typeof vote === 'string' && typeof participant === 'string' && vote && participant) {
    return ` — ${participant}: ${vote}`;
  }
  return '';
}
