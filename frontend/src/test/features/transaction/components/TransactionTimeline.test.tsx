import type { SystemEvent } from '@common/types';
import TransactionTimeline from '@features/transaction/components/TransactionTimeline';
import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

const makeEvent = (
  eventType: SystemEvent['eventType'],
  txId: string | null = 'tx-001',
  override: Partial<SystemEvent> = {}
): SystemEvent => ({
  eventType,
  transactionId: txId,
  sourceNodeId: 'coordinator',
  targetNodeId: null,
  timestamp: '2024-01-01T00:00:00.000Z',
  payload: {},
  ...override,
});

const TX_EVENTS: SystemEvent[] = [
  makeEvent('TRANSACTION_STARTED'),
  makeEvent('PREPARE_SENT'),
  makeEvent('VOTE_RECEIVED', 'tx-001', {
    sourceNodeId: 'server-1',
    payload: { participantId: 'server-1', vote: 'YES' },
  }),
  makeEvent('DECISION_MADE'),
  makeEvent('COMMIT_SENT'),
];

describe('TransactionTimeline', () => {
  it('render_givenNoEvents_showsNoEventsMessage', () => {
    render(<TransactionTimeline events={[]} selectedTxId={null} />);

    expect(screen.getAllByText('Brak zdarzeń w tej fazie.').length).toBe(2);
  });

  it('render_givenSelectedTxIdWithNoMatchingEvents_showsNoEventsForTransaction', () => {
    render(<TransactionTimeline events={TX_EVENTS} selectedTxId="tx-missing" />);

    expect(screen.getAllByText('Brak zdarzeń w tej fazie.').length).toBe(2);
  });

  it('render_givenSelectedTxId_showsOnlyMatchingEvents', () => {
    const other = makeEvent('TRANSACTION_STARTED', 'tx-other');
    render(<TransactionTimeline events={[...TX_EVENTS, other]} selectedTxId="tx-001" />);

    expect(screen.getAllByText('Transakcja rozpoczęta').length).toBe(1);
  });

  it('render_givenNullSelectedTxId_showsProtocolEventsHeading', () => {
    render(<TransactionTimeline events={TX_EVENTS} selectedTxId={null} />);

    expect(screen.getByText('Zdarzenia protokołu 2PC')).toBeInTheDocument();
  });

  it('render_givenSelectedTxId_showsTimelineHeading', () => {
    render(<TransactionTimeline events={TX_EVENTS} selectedTxId="tx-001" />);

    expect(screen.getByText(/Oś czasu/i)).toBeInTheDocument();
    expect(screen.getByText('tx-001')).toBeInTheDocument();
  });

  it('render_givenVoteReceivedEvent_displaysVoteInfo', () => {
    render(<TransactionTimeline events={TX_EVENTS} selectedTxId="tx-001" />);

    expect(screen.getByText('✅ TAK')).toBeInTheDocument();
  });

  it('render_givenEvents_displaysEventTypes', () => {
    render(<TransactionTimeline events={TX_EVENTS} selectedTxId="tx-001" />);

    expect(screen.getByText('Transakcja rozpoczęta')).toBeInTheDocument();
    expect(screen.getByText('Faza 2: COMMIT wysłany')).toBeInTheDocument();
  });

  it('render_givenTestId_isPresent', () => {
    render(<TransactionTimeline events={[]} selectedTxId={null} />);

    expect(screen.getByTestId('transaction-timeline')).toBeInTheDocument();
  });

  it('render_givenEventWithTargetNodeId_displaysArrowAndTarget', () => {
    const eventWithTarget = makeEvent('COMMIT_SENT', 'tx-001', { targetNodeId: 'server-2' });
    render(<TransactionTimeline events={[eventWithTarget]} selectedTxId="tx-001" />);

    expect(screen.getByText(/→ server-2/)).toBeInTheDocument();
  });
});
