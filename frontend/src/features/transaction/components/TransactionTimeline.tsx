import type { TransactionTimelineProps } from '@features/transaction/types';
import PhaseColumn from './PhaseColumn';

const PHASE_1_EVENT_TYPES = new Set([
  'TRANSACTION_STARTED',
  'PREPARE_SENT',
  'VOTE_RECEIVED',
  'ALL_VOTES_COLLECTED',
]);

/**
 * Displays the 2PC protocol event sequence split into Phase 1 (voting) and
 * Phase 2 (decision) columns for a selected transaction, or the most recent
 * protocol events when no transaction is selected.
 *
 * @param events full ordered event buffer from the WebSocket subscription.
 * @param selectedTxId transaction ID to filter on, or {@code null} for recent events.
 * @param onClearSelection optional callback invoked when the user clears the filter.
 */
export default function TransactionTimeline({
  events,
  selectedTxId,
  onClearSelection,
}: Readonly<TransactionTimelineProps>) {
  const visible = selectedTxId
    ? events.filter((e) => e.transactionId === selectedTxId)
    : events.filter((e) => e.transactionId !== null).slice(-40);

  const phase1Events = visible.filter((e) => PHASE_1_EVENT_TYPES.has(e.eventType));
  const phase2Events = visible.filter((e) => !PHASE_1_EVENT_TYPES.has(e.eventType));

  return (
    <div
      data-testid="transaction-timeline"
      className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg"
    >
      <div className="flex items-center justify-between mb-5">
        <h2 className="text-sm font-semibold text-slate-300 flex items-center gap-2">
          <span className="w-1.5 h-1.5 rounded-full bg-sky-400" />
          {selectedTxId ? (
            <>
              {'Oś czasu: '}
              <span className="font-mono text-xs text-slate-500">{selectedTxId}</span>
            </>
          ) : (
            'Zdarzenia protokołu 2PC'
          )}
        </h2>
        {selectedTxId && onClearSelection && (
          <button
            onClick={onClearSelection}
            className="text-[11px] text-slate-500 hover:text-slate-300 transition-colors border border-slate-700 hover:border-slate-500 rounded-lg px-2 py-0.5"
          >
            × Wyczyść filtr
          </button>
        )}
      </div>

      <div className="grid grid-cols-2 gap-5">
        <div className="bg-slate-800/40 border border-slate-700/60 rounded-xl p-4">
          <PhaseColumn marker="①" title="Faza 1 — Przygotowanie" phaseEvents={phase1Events} />
        </div>
        <div className="bg-slate-800/40 border border-slate-700/60 rounded-xl p-4">
          <PhaseColumn marker="②" title="Faza 2 — Decyzja" phaseEvents={phase2Events} />
        </div>
      </div>
    </div>
  );
}
