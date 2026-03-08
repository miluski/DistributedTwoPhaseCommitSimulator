/**
 * Returns the Tailwind class string for a scenario category tab button.
 *
 * @param active whether this tab is currently selected.
 * @returns Tailwind class string.
 */
export function categoryTabStyle(active: boolean): string {
  return `px-3 py-1 text-xs font-medium rounded transition-colors ${
    active
      ? 'bg-purple-600 text-white'
      : 'bg-slate-800 text-slate-400 hover:bg-slate-700 hover:text-slate-100'
  }`;
}
