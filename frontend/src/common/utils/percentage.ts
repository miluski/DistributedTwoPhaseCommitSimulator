/**
 * Computes a percentage string from a value and total, returning "0" when total is zero.
 *
 * @param value the numerator.
 * @param total the denominator.
 * @returns percentage rounded to the nearest integer, as a string.
 */
export function percentage(value: number, total: number): string {
  if (total === 0) return '0';
  return ((value / total) * 100).toFixed(0);
}
