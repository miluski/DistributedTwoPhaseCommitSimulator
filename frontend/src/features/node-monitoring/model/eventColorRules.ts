/** Priority-ordered rules mapping event type patterns to Tailwind colour classes. */
export const EVENT_COLOR_RULES: Array<{ test: (t: string) => boolean; color: string }> = [
  {
    test: (t) => t.includes('COMMITTED') || t === 'TRANSACTION_COMPLETED',
    color: 'text-green-400',
  },
  {
    test: (t) => t.includes('ABORT') || t.includes('CRASH') || t === 'FAULT_INJECTED',
    color: 'text-red-400',
  },
  { test: (t) => t.includes('VOTE'), color: 'text-yellow-300' },
  { test: (t) => t.includes('ELECTION'), color: 'text-purple-400' },
];
