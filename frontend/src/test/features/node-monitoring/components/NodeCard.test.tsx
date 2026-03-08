import NodeCard from '@features/node-monitoring/components/NodeCard';
import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

const baseProps = {
  serverId: 'server-1',
  port: 8444,
  status: 'ONLINE' as const,
  activeFaults: [],
  onInjectFault: vi.fn(),
};

describe('NodeCard', () => {
  it('render_givenOnlineStatus_showsGreenBackground', () => {
    const { container } = render(<NodeCard {...baseProps} />);
    expect(container.firstChild).toHaveClass('bg-emerald-950/40');
  });

  it('render_givenCrashedStatus_showsRedBackground', () => {
    const { container } = render(<NodeCard {...baseProps} status="CRASHED" />);
    expect(container.firstChild).toHaveClass('bg-rose-950/40');
  });

  it('render_givenDegradedStatus_showsYellowBackground', () => {
    const { container } = render(<NodeCard {...baseProps} status="DEGRADED" />);
    expect(container.firstChild).toHaveClass('bg-amber-950/40');
  });

  it('render_givenProps_displaysServerIdAndPort', () => {
    render(<NodeCard {...baseProps} />);
    expect(screen.getByText('server-1')).toBeInTheDocument();
    expect(screen.getByText(':8444')).toBeInTheDocument();
  });

  it('render_givenLastVoteYes_displaysVoteWithGreenColour', () => {
    render(<NodeCard {...baseProps} lastVote="YES" />);
    const voteEl = screen.getByText(/YES/);
    expect(voteEl).toHaveClass('text-emerald-300');
  });

  it('render_givenLastVoteNo_displaysVoteWithRedColour', () => {
    render(<NodeCard {...baseProps} lastVote="NO" />);
    const voteEl = screen.getByText(/NO/);
    expect(voteEl).toHaveClass('text-rose-300');
  });

  it('render_givenNoLastVote_doesNotRenderVoteRow', () => {
    render(<NodeCard {...baseProps} />);
    expect(screen.queryByText(/Last vote/i)).not.toBeInTheDocument();
  });

  it('render_givenActiveFaults_displaysFaultList', () => {
    render(<NodeCard {...baseProps} activeFaults={['CRASH', 'NETWORK_DELAY']} />);
    expect(screen.getByText(/CRASH, NETWORK_DELAY/)).toBeInTheDocument();
  });

  it('render_givenNoActiveFaults_doesNotRenderFaultRow', () => {
    render(<NodeCard {...baseProps} />);
    expect(screen.queryByText(/Faults/i)).not.toBeInTheDocument();
  });

  it('onInjectFault_givenButtonClick_callsCallbackWithServerIdAndPort', async () => {
    const handler = vi.fn();
    render(<NodeCard {...baseProps} onInjectFault={handler} />);
    screen.getByRole('button', { name: /Wstrzyknij błąd/i }).click();
    expect(handler).toHaveBeenCalledWith('server-1', 8444);
  });

  it('render_givenUnknownStatus_showsFallbackDotColour', () => {
    const { container } = render(<NodeCard {...baseProps} status={'UNKNOWN' as 'ONLINE'} />);
    const dot = container.querySelector('.bg-slate-500');
    expect(dot).toBeInTheDocument();
  });

  it('render_givenCommittedValue_displaysValue', () => {
    render(<NodeCard {...baseProps} committedValue="hello-world" />);
    expect(screen.getByText('hello-world')).toBeInTheDocument();
    expect(screen.getByText(/wartość:/)).toBeInTheDocument();
  });

  it('render_givenNoCommittedValue_doesNotRenderValueRow', () => {
    render(<NodeCard {...baseProps} />);
    expect(screen.queryByText(/wartość:/)).not.toBeInTheDocument();
  });
});
