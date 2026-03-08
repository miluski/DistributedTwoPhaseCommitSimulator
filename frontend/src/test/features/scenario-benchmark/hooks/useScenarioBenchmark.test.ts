import * as api from '@common/api';
import type { MetricsResponse, ParticipantInfo, TransactionResponse } from '@common/types';
import { useScenarioBenchmark } from '@features/scenario-benchmark/hooks/useScenarioBenchmark';
import { CATEGORIES, CATEGORY_LABELS, SCENARIOS } from '@features/scenario-benchmark/model';
import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@common/api');

const PARTICIPANTS: ParticipantInfo[] = [
  { serverId: 'server-1', host: 'localhost', port: 9001, status: 'ONLINE', activeFaults: [] },
  { serverId: 'server-2', host: 'localhost', port: 9002, status: 'ONLINE', activeFaults: [] },
];

const METRICS: MetricsResponse = {
  total: 10,
  committed: 5,
  aborted: 3,
  uncertain: 0,
  inProgress: 0,
  avgDecisionMs: 200,
  redundancyEnabled: true,
};

const TX_COMMITTED: TransactionResponse = {
  transactionId: 'aaaabbbb',
  status: 'COMMITTED',
  value: 'test',
  initiatedAt: '',
  decidedAt: '',
  votes: {},
};

const TX_ABORTED: TransactionResponse = { ...TX_COMMITTED, status: 'ABORTED' };

describe('useScenarioBenchmark — constants', () => {
  it('SCENARIOS_givenExport_containsAllCategories', () => {
    const cats = new Set(SCENARIOS.map((s) => s.category));
    expect(cats).toContain('baseline');
    expect(cats).toContain('single-fault');
    expect(cats).toContain('compound');
    expect(cats).toContain('extreme');
    expect(cats).toContain('redundancy');
  });

  it('CATEGORIES_givenExport_containsFiveCategories', () => {
    expect(CATEGORIES).toHaveLength(5);
  });

  it('CATEGORY_LABELS_givenAllCategories_hasLabelForEach', () => {
    CATEGORIES.forEach((cat) => {
      expect(CATEGORY_LABELS[cat]).toBeTruthy();
    });
  });

  it('SCENARIOS_givenExport_countIsAtLeast16', () => {
    expect(SCENARIOS.length).toBeGreaterThanOrEqual(16);
  });

  it('SCENARIOS_givenAllEntries_eachHasRequiredFields', () => {
    SCENARIOS.forEach((s) => {
      expect(s.id).toBeTruthy();
      expect(s.label).toBeTruthy();
      expect(s.description).toBeTruthy();
      expect(s.expectedBehaviour).toBeTruthy();
      expect(typeof s.run).toBe('function');
    });
  });
});

describe('useScenarioBenchmark — hook state', () => {
  beforeEach(() => {
    vi.mocked(api.fetchMetrics).mockResolvedValue(METRICS);
    vi.mocked(api.initiateTransaction).mockResolvedValue(TX_COMMITTED);
    vi.mocked(api.injectFault).mockResolvedValue(undefined);
    vi.mocked(api.injectCoordinatorFault).mockResolvedValue(undefined);
    vi.mocked(api.updateCoordinatorSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
    vi.mocked(api.updateParticipantSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
  });

  it('initialState_givenMount_runningIsFalse', () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    expect(result.current.running).toBe(false);
    expect(result.current.currentResult).toBeNull();
    expect(result.current.suiteResults).toBeNull();
  });

  it('runScenario_givenValidId_setsCurrentResult', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runScenario('baseline-happy-path');
    });

    expect(result.current.currentResult).not.toBeNull();
    expect(result.current.currentResult?.scenario.id).toBe('baseline-happy-path');
  });

  it('runScenario_givenInvalidId_doesNothing', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runScenario('non-existent-id');
    });

    expect(result.current.currentResult).toBeNull();
  });

  it('runScenario_givenCommittedTransaction_returnsPassVerdict', async () => {
    vi.mocked(api.fetchMetrics)
      .mockResolvedValueOnce(METRICS)
      .mockResolvedValueOnce({ ...METRICS, committed: 6 });
    vi.mocked(api.initiateTransaction).mockResolvedValue(TX_COMMITTED);
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runScenario('baseline-happy-path');
    });

    expect(result.current.currentResult?.result.verdict).toBe('PASS');
  });

  it('runScenario_givenAbortedTransaction_returnsPassVerdictForForcedAbort', async () => {
    vi.mocked(api.fetchMetrics)
      .mockResolvedValueOnce(METRICS)
      .mockResolvedValueOnce({ ...METRICS, aborted: 4 });
    vi.mocked(api.initiateTransaction).mockResolvedValue(TX_ABORTED);
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runScenario('baseline-forced-abort');
    });

    expect(result.current.currentResult?.result.verdict).toBe('PASS');
  });

  it('runScenario_givenResult_populatesSteps', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runScenario('baseline-happy-path');
    });

    expect(result.current.currentResult?.result.steps.length).toBeGreaterThan(0);
  });

  it('runScenario_givenResult_populatesMetrics', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runScenario('baseline-happy-path');
    });

    expect(result.current.currentResult?.result.metricsBefore).toBeDefined();
    expect(result.current.currentResult?.result.metricsAfter).toBeDefined();
  });

  it('clearResults_givenExistingResult_resetsState', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));
    await act(async () => {
      await result.current.runScenario('baseline-happy-path');
    });

    act(() => {
      result.current.clearResults();
    });

    expect(result.current.currentResult).toBeNull();
    expect(result.current.suiteResults).toBeNull();
  });

  it('runSuite_givenCategoryFilter_runsOnlyCategoryScenarios', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runSuite('baseline');
    });

    const report = result.current.suiteResults;
    expect(report).not.toBeNull();
    const baselineCount = SCENARIOS.filter((s) => s.category === 'baseline').length;
    expect(report?.results).toHaveLength(baselineCount);
  });

  it('runSuite_givenNoCategory_runsAllScenarios', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runSuite();
    });

    expect(result.current.suiteResults?.results).toHaveLength(SCENARIOS.length);
  });

  it('runSuite_givenCompletedSuite_computesPassFailDegraded', async () => {
    vi.mocked(api.fetchMetrics)
      .mockResolvedValueOnce(METRICS)
      .mockResolvedValueOnce({ ...METRICS, committed: 6 })
      .mockResolvedValueOnce(METRICS)
      .mockResolvedValueOnce({ ...METRICS, aborted: 4 });
    vi.mocked(api.initiateTransaction)
      .mockResolvedValueOnce(TX_COMMITTED)
      .mockResolvedValueOnce(TX_ABORTED);
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runSuite('baseline');
    });

    const report = result.current.suiteResults!;
    expect(report.passed + report.failed + report.degraded).toBe(report.results.length);
  });

  it('runSuite_givenApiError_gracefullyIncludesFailResult', async () => {
    vi.mocked(api.fetchMetrics).mockRejectedValue(new Error('network error'));
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runSuite('baseline');
    });

    expect(result.current.suiteResults?.results[0].result.verdict).toBe('FAIL');
  });
});

describe('useScenarioBenchmark — scenario injections', () => {
  beforeEach(() => {
    vi.mocked(api.fetchMetrics).mockResolvedValue(METRICS);
    vi.mocked(api.initiateTransaction).mockResolvedValue(TX_COMMITTED);
    vi.mocked(api.injectFault).mockResolvedValue(undefined);
    vi.mocked(api.injectCoordinatorFault).mockResolvedValue(undefined);
    vi.mocked(api.updateCoordinatorSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
    vi.mocked(api.updateParticipantSimulationConfig).mockResolvedValue({ redundancyEnabled: true });
  });

  it('scenario_givenSingleCrash_injectsCrashOnFirstParticipant', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runScenario('single-crash-participant');
    });

    expect(api.injectFault).toHaveBeenCalledWith(9001, 'CRASH', true);
    expect(api.injectFault).toHaveBeenCalledWith(9001, 'CRASH', false);
  });

  it('scenario_givenAllCrash_injectsCrashOnAllParticipants', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runScenario('extreme-all-crash');
    });

    expect(api.injectFault).toHaveBeenCalledWith(9001, 'CRASH', true);
    expect(api.injectFault).toHaveBeenCalledWith(9002, 'CRASH', true);
  });

  it('scenario_givenPartialSend_injectsMessageLossOnCoordinator', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runScenario('single-partial-send');
    });

    expect(api.injectCoordinatorFault).toHaveBeenCalledWith('MESSAGE_LOSS', true, { count: 1 });
    expect(api.injectCoordinatorFault).toHaveBeenCalledWith('MESSAGE_LOSS', false);
  });

  it('scenario_givenRedundancyOff_togglesAndRestores', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runScenario('redundancy-off-partial-send');
    });

    expect(api.updateCoordinatorSimulationConfig).toHaveBeenCalledWith(false);
    expect(api.updateCoordinatorSimulationConfig).toHaveBeenCalledWith(true);
  });

  it('runScenario_givenAlreadyCrash_injectsAndClearsCrash', async () => {
    vi.mocked(api.initiateTransaction)
      .mockRejectedValueOnce(new Error('503'))
      .mockResolvedValueOnce(TX_COMMITTED);
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runScenario('extreme-coordinator-crash');
    });

    expect(api.injectCoordinatorFault).toHaveBeenCalledWith('CRASH', true);
    expect(api.injectCoordinatorFault).toHaveBeenCalledWith('CRASH', false);
  });

  it('runScenario_givenInvalidId_doesNotCallInitiateTransaction', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));
    const callsBefore = vi.mocked(api.initiateTransaction).mock.calls.length;

    await act(async () => {
      await result.current.runScenario('not-a-valid-scenario-id');
    });

    expect(vi.mocked(api.initiateTransaction).mock.calls.length).toBe(callsBefore);
    expect(result.current.running).toBe(false);
  });

  it('runSuite_givenCompletedRun_suiteRunningReturnsFalse', async () => {
    const { result } = renderHook(() => useScenarioBenchmark(PARTICIPANTS));

    await act(async () => {
      await result.current.runSuite('baseline');
    });

    expect(result.current.suiteRunning).toBe(false);
    expect(result.current.suiteResults).not.toBeNull();
  });
});
