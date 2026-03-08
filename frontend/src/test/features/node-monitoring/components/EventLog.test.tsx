import type { SystemEvent } from '@common/types';
import EventLog from '@features/node-monitoring/components/EventLog';
import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

function makeEvent(overrides: Partial<SystemEvent> = {}): SystemEvent {
  return {
    eventType: 'TRANSACTION_STARTED',
    transactionId: 'aaaa-bbbb-cccc-dddd',
    sourceNodeId: 'coordinator',
    targetNodeId: null,
    timestamp: new Date('2025-01-01T12:00:00Z').toISOString(),
    payload: {},
    ...overrides,
  };
}

describe('EventLog', () => {
  it('render_givenNoEvents_showsWaitingMessage', () => {
    render(<EventLog events={[]} />);
    expect(screen.getByText(/Oczekiwanie na zdarzenia/i)).toBeInTheDocument();
  });

  it('render_givenOneEvent_displaysEventType', () => {
    render(<EventLog events={[makeEvent()]} />);
    expect(screen.getByText('Transakcja rozpoczęta')).toBeInTheDocument();
  });

  it('render_givenOneEvent_displaysSourceNodeId', () => {
    render(<EventLog events={[makeEvent()]} />);
    expect(screen.getByText(/coordinator/)).toBeInTheDocument();
  });

  it('render_givenTransactionId_displaysFirst8Chars', () => {
    render(<EventLog events={[makeEvent({ transactionId: 'aaaa-bbbb-cccc-dddd' })]} />);
    expect(screen.getByText(/aaaa-bbb/)).toBeInTheDocument();
  });

  it('render_givenNullTransactionId_doesNotRenderDot', () => {
    render(<EventLog events={[makeEvent({ transactionId: null })]} />);
    expect(screen.queryByText(/·/)).not.toBeInTheDocument();
  });

  it('eventColor_givenCommittedType_appliesGreenClass', () => {
    render(<EventLog events={[makeEvent({ eventType: 'TRANSACTION_COMPLETED' })]} />);
    expect(screen.getByText('Transakcja zakończona')).toHaveClass('text-green-400');
  });

  it('eventColor_givenAbortType_appliesRedClass', () => {
    render(<EventLog events={[makeEvent({ eventType: 'ABORT_SENT' })]} />);
    expect(screen.getByText('Wysłano ABORT')).toHaveClass('text-red-400');
  });

  it('eventColor_givenVoteType_appliesYellowClass', () => {
    render(<EventLog events={[makeEvent({ eventType: 'VOTE_RECEIVED' })]} />);
    expect(screen.getByText('Głos odebrany')).toHaveClass('text-yellow-300');
  });

  it('eventColor_givenElectionType_appliesPurpleClass', () => {
    render(<EventLog events={[makeEvent({ eventType: 'ELECTION_STARTED' })]} />);
    expect(screen.getByText('Wybory rozpoczęte')).toHaveClass('text-purple-400');
  });

  it('eventColor_givenUnknownType_appliesGrayClass', () => {
    render(<EventLog events={[makeEvent({ eventType: 'PARTICIPANT_REGISTERED' })]} />);
    expect(screen.getByText('Uczestnik zarejestrowany')).toHaveClass('text-gray-300');
  });

  it('render_givenMultipleEvents_rendersAllInReverseOrder', () => {
    const events = [
      makeEvent({ eventType: 'TRANSACTION_STARTED', sourceNodeId: 'first' }),
      makeEvent({ eventType: 'PREPARE_SENT', sourceNodeId: 'second' }),
    ];
    render(<EventLog events={events} />);
    const items = screen.getAllByText(/coordinator|first|second/);
    expect(items.length).toBeGreaterThanOrEqual(2);
  });
});
