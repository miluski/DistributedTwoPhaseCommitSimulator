import type { CoordinatorStatusResponse } from '@common/types';
import CoordinatorCard from '@features/node-monitoring/components/CoordinatorCard';
import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

const ONLINE_STATUS: CoordinatorStatusResponse = {
  serverId: 'coordinator',
  status: 'ONLINE',
  activeFaults: [],
};

describe('CoordinatorCard', () => {
  it('render_givenNullStatus_displaysCoordinatorLabelWithOnlineStyling', () => {
    const { container } = render(<CoordinatorCard coordinatorStatus={null} />);
    expect(container.firstChild).toHaveClass('bg-emerald-950/40');
    expect(screen.getByText('coordinator')).toBeInTheDocument();
  });

  it('render_givenOnlineStatus_showsGreenBackground', () => {
    const { container } = render(<CoordinatorCard coordinatorStatus={ONLINE_STATUS} />);
    expect(container.firstChild).toHaveClass('bg-emerald-950/40');
  });

  it('render_givenCrashedStatus_showsRedBackground', () => {
    const { container } = render(
      <CoordinatorCard
        coordinatorStatus={{ ...ONLINE_STATUS, status: 'CRASHED', activeFaults: ['CRASH'] }}
      />
    );
    expect(container.firstChild).toHaveClass('bg-rose-950/40');
  });

  it('render_givenDegradedStatus_showsYellowBackground', () => {
    const { container } = render(
      <CoordinatorCard
        coordinatorStatus={{
          ...ONLINE_STATUS,
          status: 'DEGRADED',
          activeFaults: ['NETWORK_DELAY'],
        }}
      />
    );
    expect(container.firstChild).toHaveClass('bg-amber-950/40');
  });

  it('render_givenOnlineStatus_displaysOnlineLabel', () => {
    render(<CoordinatorCard coordinatorStatus={ONLINE_STATUS} />);
    expect(screen.getByText('Online')).toBeInTheDocument();
  });

  it('render_givenCrashedStatus_displaysAwariaLabel', () => {
    render(
      <CoordinatorCard
        coordinatorStatus={{ ...ONLINE_STATUS, status: 'CRASHED', activeFaults: ['CRASH'] }}
      />
    );
    expect(screen.getByText('Awaria')).toBeInTheDocument();
  });

  it('render_givenDegradedStatus_displaysZdegradowanyLabel', () => {
    render(
      <CoordinatorCard
        coordinatorStatus={{
          ...ONLINE_STATUS,
          status: 'DEGRADED',
          activeFaults: ['NETWORK_DELAY'],
        }}
      />
    );
    expect(screen.getByText('Zdegradowany')).toBeInTheDocument();
  });

  it('render_givenActiveFaults_displaysFaultList', () => {
    render(
      <CoordinatorCard
        coordinatorStatus={{
          ...ONLINE_STATUS,
          status: 'DEGRADED',
          activeFaults: ['NETWORK_DELAY', 'MESSAGE_LOSS'],
        }}
      />
    );
    expect(screen.getByText('NETWORK_DELAY, MESSAGE_LOSS')).toBeInTheDocument();
  });

  it('render_givenNoActiveFaults_doesNotRenderFaultRow', () => {
    render(<CoordinatorCard coordinatorStatus={ONLINE_STATUS} />);
    expect(screen.queryByText(/CRASH|NETWORK_DELAY|MESSAGE_LOSS/)).not.toBeInTheDocument();
  });

  it('render_givenPulsingDot_onlineStatusHasPulseAnimation', () => {
    const { container } = render(<CoordinatorCard coordinatorStatus={ONLINE_STATUS} />);
    const dot = container.querySelector('.animate-pulse');
    expect(dot).toBeInTheDocument();
  });

  it('render_givenCrashedStatus_doesNotHavePulseAnimation', () => {
    const { container } = render(
      <CoordinatorCard
        coordinatorStatus={{ ...ONLINE_STATUS, status: 'CRASHED', activeFaults: ['CRASH'] }}
      />
    );
    const dot = container.querySelector('.animate-pulse');
    expect(dot).not.toBeInTheDocument();
  });
});
