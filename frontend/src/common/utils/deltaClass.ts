/**
 * Returns a Tailwind text colour class based on the sign of a delta value.
 *
 * @param delta numeric difference (after minus before).
 * @returns Tailwind class string.
 */
export function deltaClass(delta: number): string {
  if (delta > 0) return 'text-yellow-300';
  if (delta < 0) return 'text-green-300';
  return 'text-gray-500';
}
