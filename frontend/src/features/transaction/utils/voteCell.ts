/**
 * Returns the display label and CSS class for a vote cell.
 *
 * @param vote the vote string, or undefined when no vote was cast.
 * @returns label text and Tailwind className for the cell.
 */
export function voteCell(vote: string | undefined): { label: string; className: string } {
  const map: Record<string, { label: string; className: string }> = {
    YES: { label: '\u2705 TAK', className: 'bg-green-900 text-green-300' },
    NO: { label: '\u274c NIE', className: 'bg-red-900 text-red-300' },
  };
  return map[vote ?? ''] ?? { label: '—', className: 'text-gray-600' };
}
