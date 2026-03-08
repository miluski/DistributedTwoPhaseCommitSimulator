import { useTransactionSubmit } from '@features/transaction/hooks';
import type { TransactionPanelProps } from '@features/transaction/types';
import { type SyntheticEvent, useState } from 'react';

/**
 * Form panel that allows the user to initiate a new 2PC transaction.
 *
 * <p>Displays the final result (COMMITTED / ABORTED) once the round completes.
 * Calls {@code onTransactionSelect} with the transaction ID when the user
 * clicks "View Timeline", enabling the {@link TransactionTimeline} to filter
 * events for that specific transaction.
 *
 * @param onTransactionSelect optional callback invoked with the transaction ID.
 */
export default function TransactionPanel({ onTransactionSelect }: TransactionPanelProps) {
  const [value, setValue] = useState('');
  const { result, loading, error, submit } = useTransactionSubmit();

  const handleSubmit = async (e: SyntheticEvent<HTMLFormElement, SubmitEvent>) => {
    e.preventDefault();
    await submit(value);
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg">
      <h2 className="text-sm font-semibold text-slate-200 mb-4 flex items-center gap-2">
        <span className="w-1.5 h-1.5 rounded-full bg-cyan-500" />
        <span>Zainicjuj transakcję</span>
      </h2>

      <form onSubmit={handleSubmit} className="flex gap-2 w-full">
        <input
          className="flex-1 bg-slate-800 border border-slate-700 text-slate-100 rounded-xl px-3 py-2 text-sm placeholder:text-slate-600 focus:outline-none focus:border-cyan-500/50 transition-colors"
          placeholder="Wartość transakcji…"
          value={value}
          onChange={(e) => setValue(e.target.value)}
          required
        />
        <button
          type="submit"
          disabled={loading}
          className="bg-cyan-600 hover:bg-cyan-500 disabled:opacity-40 text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors whitespace-nowrap"
        >
          {loading ? 'Trwa…' : 'Zatwierdź'}
        </button>
      </form>

      {error && (
        <div className="mt-3 flex items-start gap-2 bg-rose-950/40 border border-rose-700/40 rounded-xl px-3 py-3">
          <span className="text-rose-400 text-sm leading-none mt-0.5">⚠</span>
          <p className="text-rose-300 text-xs font-medium leading-snug">{error}</p>
        </div>
      )}

      {result && (
        <div className="mt-4 bg-slate-800/50 border border-slate-700/60 rounded-xl p-4 space-y-3">
          <div className="flex items-center justify-between gap-2">
            <span
              className={`font-bold px-2.5 py-1 rounded-full text-xs border ${
                result.status === 'COMMITTED'
                  ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                  : 'bg-rose-500/10 text-rose-400 border-rose-500/20'
              }`}
            >
              {result.status}
            </span>
            <span className="text-xs text-slate-400 font-medium">
              {result.status === 'COMMITTED' ? 'Transakcja zatwierdzona' : 'Transakcja przerwana'}
            </span>
          </div>
          <p className="text-[11px] text-slate-500 font-mono break-all">{result.transactionId}</p>
          {onTransactionSelect && (
            <button
              data-testid="view-timeline-btn"
              onClick={() => onTransactionSelect(result.transactionId)}
              className="w-full text-xs text-center text-cyan-400 hover:text-cyan-300 border border-cyan-500/20 hover:border-cyan-500/40 bg-cyan-500/5 hover:bg-cyan-500/10 rounded-lg py-1.5 transition-colors"
            >
              Pokaż oś czasu ↓
            </button>
          )}
        </div>
      )}
    </div>
  );
}
