import ScenarioSuiteReport from '@features/scenario-benchmark/components/ScenarioSuiteReport';
import type { ScenarioResult, SuiteReport } from '@features/scenario-benchmark/types';
import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

Object.assign(navigator, { clipboard: { writeText: vi.fn().mockResolvedValue(undefined) } });

const makeResult = (overrides: Partial<ScenarioResult> = {}): ScenarioResult => ({
  verdict: 'PASS',
  summary: 'Transaction committed successfully',
  steps: ['Step 1', 'Step 2'],
  durationMs: 450,
  metricsBefore: { committed: 5, aborted: 3, uncertain: 0, avgDecisionMs: 200 },
  metricsAfter: { committed: 6, aborted: 3, uncertain: 0, avgDecisionMs: 210 },
  ...overrides,
});

const makeReport = (): SuiteReport => ({
  results: [
    {
      scenarioId: 'baseline-happy-path',
      label: 'Happy Path',
      result: makeResult({ verdict: 'PASS' }),
    },
    {
      scenarioId: 'baseline-forced-abort',
      label: 'Forced Abort',
      result: makeResult({ verdict: 'FAIL', summary: 'Unexpected commit' }),
    },
    {
      scenarioId: 'single-crash-participant',
      label: 'Crash Participant',
      result: makeResult({ verdict: 'DEGRADED', summary: 'Partial completion' }),
    },
  ],
  passed: 1,
  failed: 1,
  degraded: 1,
  totalDurationMs: 1200,
});

describe('ScenarioSuiteReport', () => {
  it('render_showsPassCount', () => {
    render(<ScenarioSuiteReport report={makeReport()} />);
    expect(screen.getByTestId('suite-report').textContent).toContain('1 Zal.');
  });

  it('render_showsFailCount', () => {
    render(<ScenarioSuiteReport report={makeReport()} />);
    expect(screen.getByTestId('suite-report').textContent).toContain('1 Niezal.');
  });

  it('render_showsTotalDuration', () => {
    render(<ScenarioSuiteReport report={makeReport()} />);
    expect(screen.getByTestId('suite-report').textContent).toContain('1200');
  });

  it('render_showsRowForEachScenario', () => {
    render(<ScenarioSuiteReport report={makeReport()} />);
    expect(screen.getByTestId('suite-row-baseline-happy-path')).toBeTruthy();
    expect(screen.getByTestId('suite-row-baseline-forced-abort')).toBeTruthy();
    expect(screen.getByTestId('suite-row-single-crash-participant')).toBeTruthy();
  });

  it('render_givenRow_showsVerdictAndSummary', () => {
    render(<ScenarioSuiteReport report={makeReport()} />);
    const row = screen.getByTestId('suite-row-baseline-forced-abort');
    expect(row.textContent).toContain('Niezaliczone');
    expect(row.textContent).toContain('Unexpected commit');
  });

  it('copySuiteBtn_givenClick_callsClipboardWriteText', () => {
    render(<ScenarioSuiteReport report={makeReport()} />);
    fireEvent.click(screen.getByTestId('copy-suite-btn'));
    expect(navigator.clipboard.writeText).toHaveBeenCalled();
  });

  it('copySuiteBtn_givenClick_writesAllVerdicts', () => {
    render(<ScenarioSuiteReport report={makeReport()} />);
    fireEvent.click(screen.getByTestId('copy-suite-btn'));
    const text = vi.mocked(navigator.clipboard.writeText).mock.calls.at(-1)![0];
    expect(text).toContain('PASS');
    expect(text).toContain('FAIL');
    expect(text).toContain('DEGRADED');
  });
});
