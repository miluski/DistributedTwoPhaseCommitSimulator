import ScenarioBenchmarkReport from '@features/scenario-benchmark/components/ScenarioBenchmarkReport';
import type { ScenarioResult } from '@features/scenario-benchmark/types';
import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

Object.assign(navigator, { clipboard: { writeText: vi.fn().mockResolvedValue(undefined) } });

const makeResult = (overrides: Partial<ScenarioResult> = {}): ScenarioResult => ({
  verdict: 'PASS',
  summary: 'Transaction committed successfully',
  steps: ['Step 1: Cleared faults', 'Step 2: Initiated transaction', 'Step 3: COMMITTED'],
  durationMs: 450,
  metricsBefore: { committed: 5, aborted: 3, uncertain: 0, avgDecisionMs: 200 },
  metricsAfter: { committed: 6, aborted: 3, uncertain: 0, avgDecisionMs: 210 },
  ...overrides,
});

describe('ScenarioBenchmarkReport', () => {
  it('render_givenPassVerdict_showsGreenBadge', () => {
    render(<ScenarioBenchmarkReport label="Happy Path" result={makeResult({ verdict: 'PASS' })} />);
    const badge = screen.getByTestId('verdict-badge');
    expect(badge.textContent).toContain('Zaliczone');
    expect(badge.className).toMatch(/green/i);
  });

  it('render_givenFailVerdict_showsRedBadge', () => {
    render(<ScenarioBenchmarkReport label="Fail" result={makeResult({ verdict: 'FAIL' })} />);
    const badge = screen.getByTestId('verdict-badge');
    expect(badge.textContent).toContain('Niezaliczone');
    expect(badge.className).toMatch(/red/i);
  });

  it('render_givenDegradedVerdict_showsYellowBadge', () => {
    render(
      <ScenarioBenchmarkReport label="Degraded" result={makeResult({ verdict: 'DEGRADED' })} />
    );
    const badge = screen.getByTestId('verdict-badge');
    expect(badge.textContent).toContain('Częściowo');
    expect(badge.className).toMatch(/yellow/i);
  });

  it('render_givenResult_showsSummaryText', () => {
    render(<ScenarioBenchmarkReport label="Test" result={makeResult()} />);
    expect(screen.getByTestId('result-summary').textContent).toContain('committed successfully');
  });

  it('render_givenResult_showsAllSteps', () => {
    render(<ScenarioBenchmarkReport label="Test" result={makeResult()} />);
    const steps = screen.getByTestId('result-steps');
    expect(steps.textContent).toContain('Step 1: Cleared faults');
    expect(steps.textContent).toContain('Step 2: Initiated transaction');
    expect(steps.textContent).toContain('Step 3: COMMITTED');
  });

  it('render_givenResult_showsDurationMs', () => {
    render(<ScenarioBenchmarkReport label="Test" result={makeResult({ durationMs: 450 })} />);
    expect(screen.getByTestId('benchmark-report').textContent).toContain('450');
  });

  it('render_givenMetricsBefore_showsMetricsTable', () => {
    render(<ScenarioBenchmarkReport label="Test" result={makeResult()} />);
    const report = screen.getByTestId('benchmark-report');
    expect(report.textContent).toContain('Zatwierdzone');
    expect(report.textContent).toContain('Przerwane');
  });

  it('render_givenMetricsDelta_showsPositiveDelta', () => {
    const result = makeResult({
      metricsBefore: { committed: 5, aborted: 3, uncertain: 0, avgDecisionMs: 200 },
      metricsAfter: { committed: 6, aborted: 3, uncertain: 0, avgDecisionMs: 200 },
    });
    render(<ScenarioBenchmarkReport label="Test" result={result} />);
    expect(screen.getByTestId('benchmark-report').textContent).toContain('+1');
  });

  it('render_givenZeroDelta_showsPlusMinusZero', () => {
    const result = makeResult({
      metricsBefore: { committed: 5, aborted: 3, uncertain: 0, avgDecisionMs: 200 },
      metricsAfter: { committed: 5, aborted: 3, uncertain: 0, avgDecisionMs: 200 },
    });
    render(<ScenarioBenchmarkReport label="Test" result={result} />);
    expect(screen.getByTestId('benchmark-report').textContent).toContain('\u00b10');
  });

  it('render_showsScenarioLabel', () => {
    render(<ScenarioBenchmarkReport label="Happy Path" result={makeResult()} />);
    expect(screen.getByTestId('benchmark-report').textContent).toContain('Happy Path');
  });

  it('copyBtn_givenClick_callsClipboardWriteText', () => {
    render(<ScenarioBenchmarkReport label="Test" result={makeResult()} />);
    fireEvent.click(screen.getByTestId('copy-report-btn'));
    expect(navigator.clipboard.writeText).toHaveBeenCalled();
  });

  it('copyBtn_givenClick_writesVerdictToClipboard', () => {
    render(<ScenarioBenchmarkReport label="Test" result={makeResult({ verdict: 'PASS' })} />);
    fireEvent.click(screen.getByTestId('copy-report-btn'));
    expect(vi.mocked(navigator.clipboard.writeText).mock.calls[0][0]).toContain('PASS');
  });
});
