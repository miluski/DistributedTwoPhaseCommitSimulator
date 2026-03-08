import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { MetricsResponse } from '@common/types';
import MetricsBar from '@features/node-monitoring/components/MetricsBar';

const METRICS: MetricsResponse = {
  total: 10,
  committed: 7,
  aborted: 2,
  uncertain: 1,
  inProgress: 0,
  avgDecisionMs: 200,
  redundancyEnabled: true,
};

describe('MetricsBar', () => {
  it('render_givenNullMetrics_showsZeroValues', () => {
    render(<MetricsBar metrics={null} redundancyEnabled toggling={false} onToggle={vi.fn()} />);

    expect(screen.getByTestId('metrics-bar')).toBeInTheDocument();
    const zeros = screen.getAllByText('0');
    expect(zeros.length).toBeGreaterThan(0);
  });

  it('render_givenMetrics_displaysCommittedCount', () => {
    render(<MetricsBar metrics={METRICS} redundancyEnabled toggling={false} onToggle={vi.fn()} />);

    expect(screen.getByText('7')).toBeInTheDocument();
  });

  it('render_givenMetrics_displaysAbortedCount', () => {
    render(<MetricsBar metrics={METRICS} redundancyEnabled toggling={false} onToggle={vi.fn()} />);

    expect(screen.getByText('2')).toBeInTheDocument();
  });

  it('render_givenRedundancyEnabled_showsOnLabel', () => {
    render(<MetricsBar metrics={METRICS} redundancyEnabled toggling={false} onToggle={vi.fn()} />);

    expect(screen.getByTestId('redundancy-toggle')).toHaveTextContent('Redundancja WŁ');
  });

  it('render_givenRedundancyDisabled_showsOffLabel', () => {
    render(
      <MetricsBar metrics={METRICS} redundancyEnabled={false} toggling={false} onToggle={vi.fn()} />
    );

    expect(screen.getByTestId('redundancy-toggle')).toHaveTextContent('Redundancja WYŁ');
  });

  it('render_givenToggling_disablesButton', () => {
    render(<MetricsBar metrics={METRICS} redundancyEnabled toggling onToggle={vi.fn()} />);

    expect(screen.getByTestId('redundancy-toggle')).toBeDisabled();
  });

  it('onToggle_givenButtonClick_callsCallback', () => {
    const handler = vi.fn();
    render(<MetricsBar metrics={METRICS} redundancyEnabled toggling={false} onToggle={handler} />);

    fireEvent.click(screen.getByTestId('redundancy-toggle'));

    expect(handler).toHaveBeenCalledTimes(1);
  });

  it('render_givenMetrics_displaysAvgDecision', () => {
    render(<MetricsBar metrics={METRICS} redundancyEnabled toggling={false} onToggle={vi.fn()} />);

    expect(screen.getByText('200 ms')).toBeInTheDocument();
  });
});
