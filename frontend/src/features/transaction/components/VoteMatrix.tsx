import type { VoteMatrixProps } from '@features/transaction/types';
import { abbreviateTransactionId, voteCell } from '@features/transaction/utils';

const MAX_COLUMNS = 8;

const TX_STATUS_COLOUR: Readonly<Record<string, string>> = {
  COMMITTED: 'text-emerald-500',
  ABORTED: 'text-rose-500',
};

/**
 * Displays a grid of participant votes for the most recent transactions.
 *
 * <p>Rows represent participant nodes (derived from the votes maps). Columns
 * represent the last {@code MAX_COLUMNS} transactions, most-recent on the right.
 *
 * @param transactions list of transaction responses from the coordinator.
 */
export default function VoteMatrix({ transactions }: Readonly<VoteMatrixProps>) {
  const recent = transactions.slice(-MAX_COLUMNS);

  const participantIds = Array.from(new Set(recent.flatMap((tx) => Object.keys(tx.votes)))).sort(
    (a, b) => a.localeCompare(b)
  );

  if (participantIds.length === 0) {
    return (
      <div
        data-testid="vote-matrix-empty"
        className="bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-lg"
      >
        <h2 className="text-sm font-semibold text-slate-300 mb-2 flex items-center gap-2">
          <span className="w-1.5 h-1.5 rounded-full bg-indigo-400" />
          <span>Macierz głosów</span>
        </h2>
        <p className="text-slate-600 text-sm">Brak danych o głosach.</p>
      </div>
    );
  }

  return (
    <div
      data-testid="vote-matrix"
      className="bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-lg overflow-x-auto"
    >
      <h2 className="text-sm font-semibold text-slate-300 mb-3 flex items-center gap-2">
        <span className="w-1.5 h-1.5 rounded-full bg-indigo-400" />
        <span>Macierz głosów</span>
      </h2>
      <table className="text-xs w-full border-collapse">
        <thead>
          <tr>
            <th className="text-left text-slate-500 pr-3 pb-2 font-medium">Uczestnik</th>
            {recent.map((tx) => {
              const statusColour = TX_STATUS_COLOUR[tx.status] ?? 'text-amber-500';
              return (
                <th
                  key={tx.transactionId}
                  className="text-center text-slate-500 pb-2 px-2 font-medium min-w-[5rem] max-w-[7rem]"
                  title={tx.transactionId}
                >
                  <div className="truncate text-[10px] text-slate-400 font-mono mb-0.5">
                    {tx.value}
                  </div>
                  {abbreviateTransactionId(tx.transactionId)}
                  <div className={`text-[10px] mt-0.5 ${statusColour}`}>{tx.status}</div>
                </th>
              );
            })}
          </tr>
        </thead>
        <tbody>
          {participantIds.map((pid) => (
            <tr key={pid} className="border-t border-slate-800">
              <td className="py-1.5 pr-3 text-slate-400 font-medium whitespace-nowrap">{pid}</td>
              {recent.map((tx) => {
                const { label, className } = voteCell(tx.votes[pid]);
                return (
                  <td
                    key={tx.transactionId}
                    className={`text-center py-1.5 px-2 rounded ${className}`}
                  >
                    {label}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
