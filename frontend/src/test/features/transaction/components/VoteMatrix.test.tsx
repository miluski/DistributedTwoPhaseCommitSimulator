import type { TransactionResponse } from '@common/types';
import VoteMatrix from '@features/transaction/components/VoteMatrix';
import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

const TX1: TransactionResponse = {
  transactionId: 'tx-001',
  status: 'COMMITTED',
  value: 'v1',
  initiatedAt: '2024-01-01T00:00:00Z',
  decidedAt: '2024-01-01T00:00:01Z',
  votes: { 'server-1': 'YES', 'server-2': 'YES' },
};

const TX2: TransactionResponse = {
  transactionId: 'tx-002',
  status: 'ABORTED',
  value: 'v2',
  initiatedAt: '2024-01-01T00:00:02Z',
  decidedAt: '2024-01-01T00:00:03Z',
  votes: { 'server-1': 'NO', 'server-2': 'YES' },
};

const TX_UNCERTAIN: TransactionResponse = {
  transactionId: 'tx-003',
  status: 'UNCERTAIN',
  value: 'v3',
  initiatedAt: '2024-01-01T00:00:04Z',
  decidedAt: null,
  votes: {},
};

describe('VoteMatrix', () => {
  it('render_givenEmptyTransactions_showsEmptyState', () => {
    render(<VoteMatrix transactions={[]} />);

    expect(screen.getByTestId('vote-matrix-empty')).toBeInTheDocument();
  });

  it('render_givenTransactionsWithNoVotes_showsEmptyState', () => {
    render(<VoteMatrix transactions={[TX_UNCERTAIN]} />);

    expect(screen.getByTestId('vote-matrix-empty')).toBeInTheDocument();
  });

  it('render_givenTransactions_displaysParticipantRows', () => {
    render(<VoteMatrix transactions={[TX1]} />);

    expect(screen.getByText('server-1')).toBeInTheDocument();
    expect(screen.getByText('server-2')).toBeInTheDocument();
  });

  it('render_givenYesVote_displaysTakLabel', () => {
    render(<VoteMatrix transactions={[TX1]} />);

    const yesCells = screen.getAllByText('✅ TAK');
    expect(yesCells.length).toBeGreaterThan(0);
  });

  it('render_givenNoVote_displaysNieLabel', () => {
    render(<VoteMatrix transactions={[TX2]} />);

    expect(screen.getByText('\u274c NIE')).toBeInTheDocument();
  });

  it('render_givenMissingVote_displaysDash', () => {
    const txPartial: TransactionResponse = {
      ...TX1,
      transactionId: 'tx-partial',
      votes: { 'server-1': 'YES' },
    };
    render(<VoteMatrix transactions={[TX1, txPartial]} />);

    expect(screen.getByText('—')).toBeInTheDocument();
  });

  it('render_givenTransactionStatus_displaysStatusLabel', () => {
    render(<VoteMatrix transactions={[TX1, TX2]} />);

    expect(screen.getByText('COMMITTED')).toBeInTheDocument();
    expect(screen.getByText('ABORTED')).toBeInTheDocument();
  });

  it('render_givenMoreThan8Transactions_showsLast8Only', () => {
    const many = Array.from({ length: 10 }, (_, i) => ({
      ...TX1,
      transactionId: `tx-${String(i).padStart(3, '0')}`,
    }));
    render(<VoteMatrix transactions={many} />);

    expect(screen.queryByTitle('tx-000')).not.toBeInTheDocument();
    expect(screen.getByTitle('tx-009')).toBeInTheDocument();
  });

  it('render_givenUncertainStatus_displaysAmberStatusColour', () => {
    const txUncertainWithVotes: TransactionResponse = {
      transactionId: 'tx-unc',
      status: 'UNCERTAIN',
      value: 'v3',
      initiatedAt: '2024-01-01T00:00:04Z',
      decidedAt: null,
      votes: { 'server-1': 'YES' },
    };
    const { container } = render(<VoteMatrix transactions={[txUncertainWithVotes]} />);

    expect(container.querySelector('.text-amber-500')).toBeInTheDocument();
  });
});
