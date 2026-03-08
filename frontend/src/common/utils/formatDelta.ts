/**
 * Formats a numeric delta with a leading sign and optional unit suffix.
 *
 * @param delta numeric difference.
 * @param unit  unit suffix appended after the number (e.g. "ms").
 * @returns formatted string such as "+12ms", "-3", or "±0ms".
 */
export function formatDelta(delta: number, unit: string): string {
  if (delta === 0) return `±0${unit}`;
  const sign = delta > 0 ? '+' : '';
  const val = Number.isInteger(delta) ? delta : delta.toFixed(1);
  return `${sign}${val}${unit}`;
}
