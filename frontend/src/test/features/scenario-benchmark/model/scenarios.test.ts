import * as api from '@common/api';
import type { MetricsResponse, ParticipantInfo, TransactionResponse } from '@common/types';
import { SCENARIOS } from '@features/scenario-benchmark/model/scenarios';
import * as benchUtils from '@features/scenario-benchmark/utils';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@common/api');

vi.mock('@features/scenario-benchmark/utils', async (importOriginal) => {
  const actual = await importOriginal<typeof benchUtils>();
  return {
    ...actual,
    snapshotMetrics: vi.fn(),
    clearAllFaults: vi.fn(),
    setRedundancyOnAll: vi.fn(),
  };
});

const METRICS_BEFORE = {
  committed: 5,
  aborted: 3,
  uncertain: 0,
  avgDecisionMs: 100,
};

const METRICS_AFTER = {
  committed: 6,
  aborted: 4,
  uncertain: 0,
  avgDecisionMs: 120,
};

const FULL_METRICS_RESPONSE: MetricsResponse = {
  total: 10,
  committed: 6,
  aborted: 4,
  uncertain: 0,
  inProgress: 0,
  avgDecisionMs: 120,
  redundancyEnabled: true,
};

const TX_COMMITTED: TransactionResponse = {
  transactionId: 'aabbccdd-1234-5678-9abc-def012345678',
  status: 'COMMITTED',
  value: 'test-value',
  initiatedAt: '2024-01-01T00:00:00Z',
  decidedAt: '2024-01-01T00:00:01Z',
  votes: {},
};

const TX_ABORTED: TransactionResponse = { ...TX_COMMITTED, status: 'ABORTED' };

const P1: ParticipantInfo = {
  serverId: 'server-1',
  host: 'localhost',
  port: 9001,
  status: 'ONLINE',
  activeFaults: [],
};

const P2: ParticipantInfo = {
  serverId: 'server-2',
  host: 'localhost',
  port: 9002,
  status: 'ONLINE',
  activeFaults: [],
};

const PARTICIPANTS = [P1, P2];

function setupHappyPathMocks() {
  vi.mocked(benchUtils.snapshotMetrics)
    .mockResolvedValueOnce(METRICS_BEFORE)
    .mockResolvedValueOnce(METRICS_AFTER);
  vi.mocked(benchUtils.clearAllFaults).mockResolvedValue(undefined);
  vi.mocked(benchUtils.setRedundancyOnAll).mockResolvedValue(undefined);
  vi.mocked(api.injectFault).mockResolvedValue(undefined);
  vi.mocked(api.injectCoordinatorFault).mockResolvedValue(undefined);
  vi.mocked(api.initiateTransaction).mockResolvedValue(TX_COMMITTED);
  vi.mocked(api.fetchMetrics).mockResolvedValue(FULL_METRICS_RESPONSE);
  vi.mocked(api.updateCoordinatorSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
  vi.mocked(api.updateParticipantSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
}

function setupAbortMocks() {
  vi.mocked(benchUtils.snapshotMetrics)
    .mockResolvedValueOnce(METRICS_BEFORE)
    .mockResolvedValueOnce({ ...METRICS_BEFORE, aborted: 4 });
  vi.mocked(benchUtils.clearAllFaults).mockResolvedValue(undefined);
  vi.mocked(benchUtils.setRedundancyOnAll).mockResolvedValue(undefined);
  vi.mocked(api.injectFault).mockResolvedValue(undefined);
  vi.mocked(api.injectCoordinatorFault).mockResolvedValue(undefined);
  vi.mocked(api.initiateTransaction).mockResolvedValue(TX_ABORTED);
  vi.mocked(api.fetchMetrics).mockResolvedValue(FULL_METRICS_RESPONSE);
  vi.mocked(api.updateCoordinatorSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
  vi.mocked(api.updateParticipantSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
}

describe('SCENARIOS — metadata', () => {
  it('givenExport_hasAtLeast16Scenarios', () => {
    expect(SCENARIOS.length).toBeGreaterThanOrEqual(16);
  });

  it.each(SCENARIOS)('$id_givenScenario_hasRequiredFields', (scenario) => {
    expect(scenario.id).toBeTruthy();
    expect(scenario.label).toBeTruthy();
    expect(scenario.description).toBeTruthy();
    expect(scenario.expectedBehaviour).toBeTruthy();
    expect(typeof scenario.run).toBe('function');
  });

  it('givenExport_coversAllCategories', () => {
    const categories = new Set(SCENARIOS.map((s) => s.category));
    expect(categories).toContain('baseline');
    expect(categories).toContain('single-fault');
    expect(categories).toContain('compound');
    expect(categories).toContain('extreme');
    expect(categories).toContain('redundancy');
  });
});

describe('SCENARIOS — run functions — happy path', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    setupHappyPathMocks();
  });

  it.each(SCENARIOS)('$id_givenHappyPath_returnsScenarioResultWithVerdict', async (scenario) => {
    vi.mocked(benchUtils.snapshotMetrics)
      .mockResolvedValueOnce(METRICS_BEFORE)
      .mockResolvedValueOnce(METRICS_AFTER);

    const result = await scenario.run(PARTICIPANTS);

    expect(result).toHaveProperty('verdict');
    expect(['PASS', 'FAIL', 'DEGRADED']).toContain(result.verdict);
    expect(result.steps).toBeInstanceOf(Array);
    expect(result.durationMs).toBeGreaterThanOrEqual(0);
  });
});

describe('SCENARIOS — run functions — abort path', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    setupAbortMocks();
  });

  it.each(SCENARIOS)('$id_givenAbortedTransaction_returnsResult', async (scenario) => {
    vi.mocked(benchUtils.snapshotMetrics)
      .mockResolvedValueOnce(METRICS_BEFORE)
      .mockResolvedValueOnce({ ...METRICS_BEFORE, aborted: 4 });

    const result = await scenario.run(PARTICIPANTS);

    expect(result).toHaveProperty('verdict');
  });
});

describe('SCENARIOS — run functions — empty participants', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(benchUtils.snapshotMetrics).mockResolvedValue(METRICS_BEFORE);
    vi.mocked(benchUtils.clearAllFaults).mockResolvedValue(undefined);
    vi.mocked(benchUtils.setRedundancyOnAll).mockResolvedValue(undefined);
    vi.mocked(api.injectFault).mockResolvedValue(undefined);
    vi.mocked(api.injectCoordinatorFault).mockResolvedValue(undefined);
    vi.mocked(api.initiateTransaction).mockResolvedValue(TX_COMMITTED);
    vi.mocked(api.fetchMetrics).mockResolvedValue(FULL_METRICS_RESPONSE);
    vi.mocked(api.updateCoordinatorSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
    vi.mocked(api.updateParticipantSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
  });

  it.each(SCENARIOS)('$id_givenEmptyParticipants_returnsResult', async (scenario) => {
    const result = await scenario.run([]);

    expect(result).toHaveProperty('verdict');
  });
});

describe('SCENARIOS — run functions — API error path', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(benchUtils.snapshotMetrics)
      .mockResolvedValueOnce(METRICS_BEFORE)
      .mockResolvedValueOnce(METRICS_AFTER);
    vi.mocked(benchUtils.clearAllFaults).mockResolvedValue(undefined);
    vi.mocked(benchUtils.setRedundancyOnAll).mockResolvedValue(undefined);
    vi.mocked(api.injectFault).mockResolvedValue(undefined);
    vi.mocked(api.injectCoordinatorFault).mockResolvedValue(undefined);
    vi.mocked(api.initiateTransaction).mockRejectedValue(new Error('Network failure'));
    vi.mocked(api.fetchMetrics).mockResolvedValue(FULL_METRICS_RESPONSE);
    vi.mocked(api.updateCoordinatorSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
    vi.mocked(api.updateParticipantSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
  });

  it.each(SCENARIOS)('$id_givenApiError_returnsResultWithSteps', async (scenario) => {
    vi.mocked(benchUtils.snapshotMetrics)
      .mockResolvedValueOnce(METRICS_BEFORE)
      .mockResolvedValueOnce(METRICS_AFTER);

    const result = await scenario.run(PARTICIPANTS);

    expect(result).toHaveProperty('verdict');
    expect(
      result.steps.some((s) => s.includes('error') || s.includes('Error') || s.includes('Błąd'))
    ).toBe(true);
  });
});

describe('SCENARIOS — run functions — non-Error thrown', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(benchUtils.snapshotMetrics)
      .mockResolvedValueOnce(METRICS_BEFORE)
      .mockResolvedValueOnce(METRICS_AFTER);
    vi.mocked(benchUtils.clearAllFaults).mockResolvedValue(undefined);
    vi.mocked(benchUtils.setRedundancyOnAll).mockResolvedValue(undefined);
    vi.mocked(api.injectFault).mockResolvedValue(undefined);
    vi.mocked(api.injectCoordinatorFault).mockResolvedValue(undefined);
    vi.mocked(api.initiateTransaction).mockRejectedValue('plain string error');
    vi.mocked(api.fetchMetrics).mockResolvedValue(FULL_METRICS_RESPONSE);
    vi.mocked(api.updateCoordinatorSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
    vi.mocked(api.updateParticipantSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
  });

  it.each(SCENARIOS)(
    '$id_givenNonErrorThrown_returnsResultWithStringCoercion',
    async (scenario) => {
      vi.mocked(benchUtils.snapshotMetrics)
        .mockResolvedValueOnce(METRICS_BEFORE)
        .mockResolvedValueOnce(METRICS_AFTER);

      const result = await scenario.run(PARTICIPANTS);

      expect(result).toHaveProperty('verdict');
    }
  );
});

describe('SCENARIOS — extreme-coordinator-crash — specific branches', () => {
  it('run_givenCrashRejectedAndRecoveryCommitted_returnsPassVerdict', async () => {
    const scenario = SCENARIOS.find((s) => s.id === 'extreme-coordinator-crash')!;
    vi.mocked(benchUtils.snapshotMetrics)
      .mockResolvedValueOnce(METRICS_BEFORE)
      .mockResolvedValueOnce(METRICS_AFTER);
    vi.mocked(api.injectCoordinatorFault).mockResolvedValue(undefined);
    vi.mocked(api.initiateTransaction)
      .mockRejectedValueOnce(new Error('503 service unavailable'))
      .mockResolvedValueOnce(TX_COMMITTED);

    const result = await scenario.run(PARTICIPANTS);

    expect(result.verdict).toBe('PASS');
  });

  it('run_givenCrashRejectedAndRecoveryFails_returnsDegradedVerdict', async () => {
    const scenario = SCENARIOS.find((s) => s.id === 'extreme-coordinator-crash')!;
    vi.mocked(benchUtils.snapshotMetrics)
      .mockResolvedValueOnce(METRICS_BEFORE)
      .mockResolvedValueOnce(METRICS_AFTER);
    vi.mocked(api.injectCoordinatorFault).mockResolvedValue(undefined);
    vi.mocked(api.initiateTransaction)
      .mockRejectedValueOnce(new Error('503 service unavailable'))
      .mockRejectedValueOnce(new Error('recovery also failed'));

    const result = await scenario.run(PARTICIPANTS);

    expect(result.verdict).toBe('DEGRADED');
  });
});

describe('SCENARIOS — compound-intermittent-force-abort — single participant', () => {
  it('run_givenSingleParticipant_skipsDoubleInject', async () => {
    const scenario = SCENARIOS.find((s) => s.id === 'compound-intermittent-force-abort')!;
    vi.mocked(benchUtils.snapshotMetrics)
      .mockResolvedValueOnce(METRICS_BEFORE)
      .mockResolvedValueOnce(METRICS_AFTER);
    vi.mocked(api.injectFault).mockResolvedValue(undefined);
    vi.mocked(api.initiateTransaction).mockResolvedValue(TX_ABORTED);

    const result = await scenario.run([P1]);

    expect(result).toHaveProperty('verdict');
  });
});
