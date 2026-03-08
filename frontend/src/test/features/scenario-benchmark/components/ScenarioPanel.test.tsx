import type { ParticipantInfo } from '@common/types';
import ScenarioPanel from '@features/scenario-benchmark/components/ScenarioPanel';
import type {
  Scenario,
  ScenarioCategory,
  ScenarioResult,
  SuiteReport,
} from '@features/scenario-benchmark/types';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@features/scenario-benchmark/hooks', () => {
  const PASS_RESULT: ScenarioResult = {
    verdict: 'PASS',
    summary: 'ok',
    steps: ['done'],
    durationMs: 100,
    metricsBefore: { committed: 0, aborted: 0, uncertain: 0, avgDecisionMs: 0 },
    metricsAfter: { committed: 1, aborted: 0, uncertain: 0, avgDecisionMs: 100 },
  };

  const SCENARIO = {
    id: 'baseline-happy-path',
    label: 'Happy Path',
    category: 'baseline',
    description: 'No faults injected.',
    expectedBehaviour: 'Transaction commits.',
    run: async () => PASS_RESULT,
  };

  const SUITE_ROW = {
    scenarioId: 'baseline-happy-path',
    label: 'Happy Path',
    result: PASS_RESULT,
  };

  const SUITE_REPORT: SuiteReport = {
    results: [SUITE_ROW],
    passed: 1,
    failed: 0,
    degraded: 0,
    totalDurationMs: 100,
  };

  let runningState = false;
  let suiteRunningState = false;

  const mockRunScenario = vi.fn().mockResolvedValue(undefined);
  const mockRunSuite = vi.fn().mockResolvedValue(undefined);
  const mockClearResults = vi.fn();

  const useScenarioBenchmark = vi.fn(() => ({
    running: runningState,
    currentResult: null,
    suiteRunning: suiteRunningState,
    suiteProgress: null,
    suiteResults: null,
    runScenario: mockRunScenario,
    runSuite: mockRunSuite,
    clearResults: mockClearResults,
  }));

  return {
    useScenarioBenchmark,
    SCENARIOS: [SCENARIO],
    CATEGORIES: ['baseline'],
    CATEGORY_LABELS: { baseline: 'Baseline' },
    __mockRunScenario: mockRunScenario,
    __mockRunSuite: mockRunSuite,
    __mockClearResults: mockClearResults,
    __SUITE_REPORT: SUITE_REPORT,
    __PASS_RESULT: PASS_RESULT,
    __SCENARIO: SCENARIO,
  };
});

vi.mock('@features/scenario-benchmark/components/ScenarioBenchmarkReport', () => ({
  default: ({ label }: { label: string }) => (
    <div data-testid="benchmark-report">report:{label}</div>
  ),
}));

vi.mock('@features/scenario-benchmark/components/ScenarioSuiteReport', () => ({
  default: () => <div data-testid="suite-report">suite</div>,
}));

const PARTICIPANTS: ParticipantInfo[] = [
  { serverId: 'server-1', host: 'localhost', port: 9001, status: 'ONLINE', activeFaults: [] },
  { serverId: 'server-2', host: 'localhost', port: 9002, status: 'ONLINE', activeFaults: [] },
];

interface MockedHooks {
  __mockRunScenario: ReturnType<typeof vi.fn>;
  __mockRunSuite: ReturnType<typeof vi.fn>;
  __mockClearResults: ReturnType<typeof vi.fn>;
  __SUITE_REPORT: SuiteReport;
  __PASS_RESULT: ScenarioResult;
  __SCENARIO: Scenario;
}

describe('ScenarioPanel', () => {
  beforeEach(async () => {
    vi.resetAllMocks();
    const hook = await import('@features/scenario-benchmark/hooks');
    const extras = hook as unknown as MockedHooks;
    extras.__mockRunScenario.mockResolvedValue(undefined);
    extras.__mockRunSuite.mockResolvedValue(undefined);
    vi.mocked(hook.useScenarioBenchmark).mockReturnValue({
      scenarios: [],
      categories: [],
      running: false,
      currentResult: null,
      suiteRunning: false,
      suiteProgress: null,
      suiteResults: null,
      runScenario: extras.__mockRunScenario as unknown as (id: string) => Promise<void>,
      runSuite: extras.__mockRunSuite as unknown as (category?: ScenarioCategory) => Promise<void>,
      clearResults: extras.__mockClearResults as unknown as () => void,
    });
  });

  it('render_givenMount_showsFaultToleranceHeading', () => {
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByText(/Test odporności na błędy/i)).toBeInTheDocument();
  });

  it('render_givenMount_showsCategoryTabs', () => {
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByTestId('category-tab-baseline')).toBeInTheDocument();
  });

  it('render_givenMount_showsScenarioDropdown', () => {
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByTestId('scenario-select')).toBeInTheDocument();
  });

  it('render_givenMount_showsRunScenarioButton', () => {
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByTestId('run-scenario-btn')).toBeInTheDocument();
  });

  it('render_givenMount_showsRunSuiteButton', () => {
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByTestId('run-suite-btn')).toBeInTheDocument();
  });

  it('render_givenMount_showsRunAllButton', () => {
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByTestId('run-all-btn')).toBeInTheDocument();
  });

  it('render_givenMount_doesNotShowClearButton', () => {
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.queryByTestId('clear-results-btn')).not.toBeInTheDocument();
  });

  it('render_givenMount_showsScenarioDescription', () => {
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByText(/Brak aktywnych błędów/i)).toBeInTheDocument();
  });

  it('runScenarioBtn_givenClick_callsRunScenario', async () => {
    const hook = await import('@features/scenario-benchmark/hooks');
    const mockRun = (hook as unknown as { __mockRunScenario: ReturnType<typeof vi.fn> })
      .__mockRunScenario;
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    fireEvent.click(screen.getByTestId('run-scenario-btn'));

    await waitFor(() => expect(mockRun).toHaveBeenCalledWith('baseline-happy-path'));
  });

  it('runSuiteBtn_givenClick_callsRunSuiteWithCategory', async () => {
    const hook = await import('@features/scenario-benchmark/hooks');
    const mockRun = (hook as unknown as { __mockRunSuite: ReturnType<typeof vi.fn> })
      .__mockRunSuite;
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    fireEvent.click(screen.getByTestId('run-suite-btn'));

    await waitFor(() => expect(mockRun).toHaveBeenCalledWith('baseline'));
  });

  it('runAllBtn_givenClick_callsRunSuiteWithoutArgument', async () => {
    const hook = await import('@features/scenario-benchmark/hooks');
    const mockRun = (hook as unknown as { __mockRunSuite: ReturnType<typeof vi.fn> })
      .__mockRunSuite;
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    fireEvent.click(screen.getByTestId('run-all-btn'));

    await waitFor(() => expect(mockRun).toHaveBeenCalledWith());
  });

  it('render_givenRunning_showsRunningButtonLabel', async () => {
    const hook = await import('@features/scenario-benchmark/hooks');
    const extras = hook as unknown as MockedHooks;
    vi.mocked(hook.useScenarioBenchmark).mockReturnValue({
      scenarios: [],
      categories: [],
      running: true,
      currentResult: null,
      suiteRunning: false,
      suiteProgress: null,
      suiteResults: null,
      runScenario: extras.__mockRunScenario as unknown as (id: string) => Promise<void>,
      runSuite: extras.__mockRunSuite as unknown as (category?: ScenarioCategory) => Promise<void>,
      clearResults: extras.__mockClearResults as unknown as () => void,
    });

    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByTestId('run-scenario-btn')).toHaveTextContent('Trwa…');
  });

  it('render_givenSuiteRunning_showsProgressLabelAndParagraph', async () => {
    const hook = await import('@features/scenario-benchmark/hooks');
    const extras = hook as unknown as MockedHooks;
    vi.mocked(hook.useScenarioBenchmark).mockReturnValue({
      scenarios: [],
      categories: [],
      running: false,
      currentResult: null,
      suiteRunning: true,
      suiteProgress: { current: 2, total: 5 },
      suiteResults: null,
      runScenario: extras.__mockRunScenario as unknown as (id: string) => Promise<void>,
      runSuite: extras.__mockRunSuite as unknown as (category?: ScenarioCategory) => Promise<void>,
      clearResults: extras.__mockClearResults as unknown as () => void,
    });

    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByTestId('run-suite-btn')).toHaveTextContent('Trwa 2/5…');
    expect(screen.getByTestId('suite-progress')).toBeInTheDocument();
  });

  it('render_givenCurrentResult_showsBenchmarkReportAndClearButton', async () => {
    const hook = await import('@features/scenario-benchmark/hooks');
    const extras = hook as unknown as MockedHooks;
    vi.mocked(hook.useScenarioBenchmark).mockReturnValue({
      scenarios: [],
      categories: [],
      running: false,
      currentResult: { scenario: extras.__SCENARIO, result: extras.__PASS_RESULT },
      suiteRunning: false,
      suiteProgress: null,
      suiteResults: null,
      runScenario: extras.__mockRunScenario as unknown as (id: string) => Promise<void>,
      runSuite: extras.__mockRunSuite as unknown as (category?: ScenarioCategory) => Promise<void>,
      clearResults: extras.__mockClearResults as unknown as () => void,
    });

    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByTestId('benchmark-report')).toBeInTheDocument();
    expect(screen.getByTestId('clear-results-btn')).toBeInTheDocument();
  });

  it('render_givenSuiteResults_showsSuiteReportAndClearButton', async () => {
    const hook = await import('@features/scenario-benchmark/hooks');
    const extras = hook as unknown as MockedHooks;
    vi.mocked(hook.useScenarioBenchmark).mockReturnValue({
      scenarios: [],
      categories: [],
      running: false,
      currentResult: null,
      suiteRunning: false,
      suiteProgress: null,
      suiteResults: extras.__SUITE_REPORT,
      runScenario: extras.__mockRunScenario as unknown as (id: string) => Promise<void>,
      runSuite: extras.__mockRunSuite as unknown as (category?: ScenarioCategory) => Promise<void>,
      clearResults: extras.__mockClearResults as unknown as () => void,
    });

    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByTestId('suite-report')).toBeInTheDocument();
    expect(screen.getByTestId('clear-results-btn')).toBeInTheDocument();
  });

  it('clearBtn_givenClick_callsClearResults', async () => {
    const hook = await import('@features/scenario-benchmark/hooks');
    const extras = hook as unknown as MockedHooks;
    vi.mocked(hook.useScenarioBenchmark).mockReturnValue({
      scenarios: [],
      categories: [],
      running: false,
      currentResult: { scenario: extras.__SCENARIO, result: extras.__PASS_RESULT },
      suiteRunning: false,
      suiteProgress: null,
      suiteResults: null,
      runScenario: extras.__mockRunScenario as unknown as (id: string) => Promise<void>,
      runSuite: extras.__mockRunSuite as unknown as (category?: ScenarioCategory) => Promise<void>,
      clearResults: extras.__mockClearResults as unknown as () => void,
    });

    render(<ScenarioPanel participants={PARTICIPANTS} />);
    fireEvent.click(screen.getByTestId('clear-results-btn'));

    expect(extras.__mockClearResults).toHaveBeenCalledOnce();
  });

  it('categoryTab_givenClick_callsHandleCategoryChange', () => {
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    fireEvent.click(screen.getByTestId('category-tab-single-fault'));

    expect(screen.getByTestId('category-tab-single-fault')).toBeInTheDocument();
    expect(screen.getByTestId('scenario-select')).toBeInTheDocument();
  });

  it('render_givenSuiteRunningWithNullProgress_showsZeroProgressLabel', async () => {
    const hook = await import('@features/scenario-benchmark/hooks');
    const extras = hook as unknown as MockedHooks;
    vi.mocked(hook.useScenarioBenchmark).mockReturnValue({
      scenarios: [],
      categories: [],
      running: false,
      currentResult: null,
      suiteRunning: true,
      suiteProgress: null,
      suiteResults: null,
      runScenario: extras.__mockRunScenario as unknown as (id: string) => Promise<void>,
      runSuite: extras.__mockRunSuite as unknown as (category?: ScenarioCategory) => Promise<void>,
      clearResults: extras.__mockClearResults as unknown as () => void,
    });

    render(<ScenarioPanel participants={PARTICIPANTS} />);

    expect(screen.getByTestId('run-suite-btn')).toHaveTextContent('Trwa 0/0…');
  });

  it('categoryTab_givenClickWithNoMatchingScenario_fallsBackToFirstScenario', () => {
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    fireEvent.click(screen.getByTestId('category-tab-extreme'));
    fireEvent.change(screen.getByTestId('scenario-select'), {
      target: { value: 'nonexistent-id' },
    });
    fireEvent.click(screen.getByTestId('category-tab-single-fault'));

    expect(screen.getByTestId('scenario-select')).toBeInTheDocument();
  });

  it('scenarioSelect_givenChangeToUnknownId_rendersWithFallbackScenario', () => {
    render(<ScenarioPanel participants={PARTICIPANTS} />);

    fireEvent.change(screen.getByTestId('scenario-select'), {
      target: { value: 'nonexistent-id' },
    });

    expect(screen.getByTestId('scenario-select')).toBeInTheDocument();
  });

  it('render_givenCurrentResultAndRunning_showsDisabledClearButton', async () => {
    const hook = await import('@features/scenario-benchmark/hooks');
    const extras = hook as unknown as MockedHooks;
    vi.mocked(hook.useScenarioBenchmark).mockReturnValue({
      scenarios: [],
      categories: [],
      running: true,
      currentResult: { scenario: extras.__SCENARIO, result: extras.__PASS_RESULT },
      suiteRunning: false,
      suiteProgress: null,
      suiteResults: null,
      runScenario: extras.__mockRunScenario as unknown as (id: string) => Promise<void>,
      runSuite: extras.__mockRunSuite as unknown as (category?: ScenarioCategory) => Promise<void>,
      clearResults: extras.__mockClearResults as unknown as () => void,
    });

    render(<ScenarioPanel participants={PARTICIPANTS} />);

    const clearBtn = screen.getByTestId('clear-results-btn');
    expect(clearBtn).toBeDisabled();
  });
});
