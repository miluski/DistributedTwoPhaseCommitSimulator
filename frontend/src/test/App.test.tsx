import App from '@/App';
import * as coordinatorApi from '@common/api';
import type { ParticipantInfo, SystemEvent } from '@common/types';
import * as nodeMonitoring from '@features/node-monitoring';
import { render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@common/api');

vi.mock('@features/node-monitoring', () => ({
  CoordinatorCard: () => <div data-testid="coordinator-card" />,
  EventLog: ({ events }: { events: SystemEvent[] }) => (
    <div data-testid="event-log">{events.length}</div>
  ),
  MetricsBar: () => <div data-testid="metrics-bar" />,
  NodeCard: ({ serverId }: { serverId: string }) => <div data-testid="node-card">{serverId}</div>,
  useCoordinatorStatus: () => ({ coordinatorStatus: null }),
  useSystemEvents: vi.fn(),
  useMetrics: vi.fn(),
  useSimulationConfig: vi.fn(),
}));

vi.mock('@features/transaction', () => ({
  TransactionPanel: () => <div data-testid="transaction-panel" />,
  TransactionTimeline: () => <div data-testid="transaction-timeline" />,
  VoteMatrix: () => <div data-testid="vote-matrix" />,
}));

vi.mock('@features/fault-injection', () => ({
  FaultInjectionPanel: () => <div data-testid="fault-panel" />,
}));

vi.mock('@features/scenario-benchmark', () => ({
  ScenarioPanel: () => <div data-testid="scenario-panel" />,
}));

const PARTICIPANT: ParticipantInfo = {
  serverId: 'p1',
  host: 'localhost',
  port: 8081,
  status: 'ONLINE',
  activeFaults: [],
};

const EVENT: SystemEvent = {
  eventType: 'TRANSACTION_STARTED',
  transactionId: 'txn-1',
  sourceNodeId: 'coordinator',
  targetNodeId: null,
  timestamp: '2024-01-01T00:00:00Z',
  payload: {},
};

const SYSTEM_EVENT: SystemEvent = {
  eventType: 'FAULT_INJECTED',
  transactionId: null,
  sourceNodeId: 'coordinator',
  targetNodeId: null,
  timestamp: '2024-01-01T00:00:00Z',
  payload: { faultType: 'CRASH' },
};

beforeEach(() => {
  vi.clearAllTimers();
  vi.mocked(nodeMonitoring.useSystemEvents).mockReturnValue({ events: [], connected: false });
  vi.mocked(nodeMonitoring.useMetrics).mockReturnValue({ metrics: null });
  vi.mocked(nodeMonitoring.useSimulationConfig).mockReturnValue({
    redundancyEnabled: true,
    toggling: false,
    toggleRedundancy: vi.fn(),
  });
  vi.mocked(coordinatorApi.fetchParticipants).mockResolvedValue([]);
  vi.mocked(coordinatorApi.fetchTransactions).mockResolvedValue([]);
});

afterEach(() => {
  vi.restoreAllMocks();
  vi.clearAllTimers();
});

describe('App', () => {
  it('render_givenNoParticipants_showsNoParticipantsMessage', async () => {
    render(<App />);

    await waitFor(() =>
      expect(screen.getByText('Brak zarejestrowanych węzłów.')).toBeInTheDocument()
    );
  });

  it('render_givenParticipants_rendersNodeCards', async () => {
    vi.mocked(coordinatorApi.fetchParticipants).mockResolvedValue([PARTICIPANT]);

    render(<App />);

    await waitFor(() => expect(screen.getByTestId('node-card')).toHaveTextContent('p1'));
  });

  it('render_givenConnected_showsLiveIndicator', () => {
    vi.mocked(nodeMonitoring.useSystemEvents).mockReturnValue({ events: [], connected: true });

    render(<App />);

    expect(screen.getByText(/Na żywo/)).toBeInTheDocument();
  });

  it('render_givenDisconnected_showsDisconnectedIndicator', () => {
    vi.mocked(nodeMonitoring.useSystemEvents).mockReturnValue({
      events: [],
      connected: false,
    });

    render(<App />);

    expect(screen.getByText(/Rozłączony/)).toBeInTheDocument();
  });

  it('render_givenSystemEvent_showsInEventLog', async () => {
    vi.mocked(nodeMonitoring.useSystemEvents).mockReturnValue({
      events: [SYSTEM_EVENT],
      connected: true,
    });
    vi.mocked(coordinatorApi.fetchParticipants).mockResolvedValue([]);

    render(<App />);

    await waitFor(() => expect(screen.getByTestId('event-log')).toHaveTextContent('1'));
  });

  it('render_givenNewEvents_refetchesParticipants', async () => {
    vi.mocked(coordinatorApi.fetchParticipants).mockResolvedValue([]);
    vi.mocked(nodeMonitoring.useSystemEvents).mockReturnValue({
      events: [EVENT],
      connected: true,
    });

    render(<App />);

    await waitFor(() => expect(coordinatorApi.fetchParticipants).toHaveBeenCalledTimes(2));
  });

  it('loadParticipants_givenFetchError_keepsStaleDateAndDoesNotThrow', async () => {
    vi.mocked(coordinatorApi.fetchParticipants).mockRejectedValue(new Error('network failure'));

    expect(() => render(<App />)).not.toThrow();
    await waitFor(() => expect(coordinatorApi.fetchParticipants).toHaveBeenCalled());
  });

  it('loadTransactions_givenFetchError_doesNotThrow', async () => {
    vi.mocked(coordinatorApi.fetchTransactions).mockRejectedValue(new Error('server error'));

    expect(() => render(<App />)).not.toThrow();
    await waitFor(() => expect(coordinatorApi.fetchTransactions).toHaveBeenCalled());
  });

  it('unmount_clearsPollingInterval', async () => {
    vi.useFakeTimers();
    const { unmount } = render(<App />);

    unmount();

    vi.advanceTimersByTime(10000);
    expect(coordinatorApi.fetchParticipants).toHaveBeenCalledTimes(1);
    vi.useRealTimers();
  });
});
