/**
 * Formats an ISO timestamp string as a locale time string.
 *
 * @param ts ISO 8601 timestamp.
 * @returns formatted time string (HH:MM:SS).
 */
export function formatTime(ts: string): string {
  return new Date(ts).toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}
