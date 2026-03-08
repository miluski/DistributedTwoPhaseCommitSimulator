import type { EventStyle } from '@features/transaction/types/EventStyle';

/** Maps each 2PC event type to its icon and Tailwind colour classes. */
export const EVENT_STYLES: Record<string, EventStyle> = {
  TRANSACTION_STARTED: { icon: '🚀', colorClass: 'border-blue-500 text-blue-300' },
  PREPARE_SENT: { icon: '📤', colorClass: 'border-blue-400 text-blue-200' },
  VOTE_RECEIVED: { icon: '🗳️', colorClass: 'border-yellow-500 text-yellow-300' },
  ALL_VOTES_COLLECTED: { icon: '📊', colorClass: 'border-yellow-400 text-yellow-200' },
  DECISION_MADE: { icon: '⚖️', colorClass: 'border-purple-500 text-purple-300' },
  COMMIT_SENT: { icon: '✅', colorClass: 'border-green-500 text-green-300' },
  ABORT_SENT: { icon: '❌', colorClass: 'border-red-500 text-red-300' },
  TRANSACTION_COMPLETED: { icon: '🏁', colorClass: 'border-green-400 text-green-200' },
  TRANSACTION_TIMED_OUT: { icon: '⏱️', colorClass: 'border-orange-500 text-orange-300' },
  COORDINATOR_CRASHED: { icon: '💥', colorClass: 'border-red-600 text-red-400' },
  COORDINATOR_RECOVERED: { icon: '🔄', colorClass: 'border-teal-500 text-teal-300' },
  PARTICIPANT_CRASHED: { icon: '💥', colorClass: 'border-red-500 text-red-300' },
  PARTICIPANT_RECOVERED: { icon: '🔄', colorClass: 'border-teal-400 text-teal-200' },
  ELECTION_STARTED: { icon: '🗳️', colorClass: 'border-violet-500 text-violet-300' },
  ELECTION_RESULT: { icon: '🏆', colorClass: 'border-violet-400 text-violet-200' },
  FAULT_INJECTED: { icon: '⚡', colorClass: 'border-orange-600 text-orange-400' },
  FAULT_CLEARED: { icon: '🛡️', colorClass: 'border-green-600 text-green-400' },
};
