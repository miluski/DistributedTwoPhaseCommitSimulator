import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as api from '@common/api';
import type { ParticipantInfo } from '@common/types';
import FaultInjectionPanel from '@features/fault-injection/components/FaultInjectionPanel';

vi.mock('@common/api');

const participants: ParticipantInfo[] = [
  {
    serverId: 'server-1',
    host: 'localhost',
    port: 9001,
    status: 'ONLINE',
    lastVote: null,
    activeFaults: [],
  },
  {
    serverId: 'server-2',
    host: 'localhost',
    port: 9002,
    status: 'ONLINE',
    lastVote: null,
    activeFaults: [],
  },
];

describe('FaultInjectionPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(api.injectCoordinatorFault).mockResolvedValue(undefined);
    vi.mocked(api.injectFault).mockResolvedValue(undefined);
  });

  it('render_givenParticipants_showsCoordinatorAndParticipantOptions', () => {
    render(<FaultInjectionPanel participants={participants} />);
    expect(screen.getByRole('option', { name: /Koordynator/i })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: /server-1/i })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: /server-2/i })).toBeInTheDocument();
  });

  it('render_givenDefaultFaultType_doesNotShowDelayInput', () => {
    render(<FaultInjectionPanel participants={participants} />);
    expect(screen.queryByRole('spinbutton', { name: /Delay/i })).not.toBeInTheDocument();
  });

  it('render_givenNetworkDelaySelected_showsDelayInput', async () => {
    render(<FaultInjectionPanel participants={participants} />);
    const faultSelect = screen.getAllByRole('combobox')[1];
    await userEvent.selectOptions(faultSelect, 'NETWORK_DELAY');
    expect(await screen.findByRole('spinbutton')).toBeInTheDocument();
  });

  it('injectFault_givenCoordinatorTarget_callsInjectCoordinatorFault', async () => {
    render(<FaultInjectionPanel participants={participants} />);
    await userEvent.click(screen.getByRole('button', { name: /Wstrzyknij/i }));
    await waitFor(() => expect(api.injectCoordinatorFault).toHaveBeenCalledWith('CRASH', true, {}));
  });

  it('clearFault_givenCoordinatorTarget_callsInjectCoordinatorFaultWithDisabled', async () => {
    render(<FaultInjectionPanel participants={participants} />);
    await userEvent.click(screen.getByRole('button', { name: /Wyczyść/i }));
    await waitFor(() =>
      expect(api.injectCoordinatorFault).toHaveBeenCalledWith('CRASH', false, {})
    );
  });

  it('injectFault_givenParticipantTarget_callsInjectFaultWithPort', async () => {
    render(<FaultInjectionPanel participants={participants} />);
    const targetSelect = screen.getAllByRole('combobox')[0];
    await userEvent.selectOptions(targetSelect, 'server-1');
    await userEvent.click(screen.getByRole('button', { name: /Wstrzyknij/i }));
    await waitFor(() => expect(api.injectFault).toHaveBeenCalledWith(9001, 'CRASH', true, {}));
  });

  it('injectFault_givenNetworkDelayType_includesDelayInParameters', async () => {
    render(<FaultInjectionPanel participants={participants} />);
    const faultSelect = screen.getAllByRole('combobox')[1];
    await userEvent.selectOptions(faultSelect, 'NETWORK_DELAY');
    const delayInput = await screen.findByRole('spinbutton');
    await userEvent.clear(delayInput);
    await userEvent.type(delayInput, '1000');
    await userEvent.click(screen.getByRole('button', { name: /Wstrzyknij/i }));
    await waitFor(() =>
      expect(api.injectCoordinatorFault).toHaveBeenCalledWith('NETWORK_DELAY', true, {
        delayMs: 1000,
      })
    );
  });

  it('injectFault_givenSuccess_displaysInjectedMessage', async () => {
    render(<FaultInjectionPanel participants={participants} />);
    await userEvent.click(screen.getByRole('button', { name: /Wstrzyknij/i }));
    await waitFor(() => expect(screen.getByText('Usterka wstrzyknięta.')).toBeInTheDocument());
  });

  it('clearFault_givenSuccess_displaysClearedMessage', async () => {
    render(<FaultInjectionPanel participants={participants} />);
    await userEvent.click(screen.getByRole('button', { name: /Wyczyść/i }));
    await waitFor(() => expect(screen.getByText('Usterka wyczyszczona.')).toBeInTheDocument());
  });

  it('injectFault_givenApiError_displaysErrorMessage', async () => {
    vi.mocked(api.injectCoordinatorFault).mockRejectedValue(new Error('Service unavailable'));
    render(<FaultInjectionPanel participants={participants} />);
    await userEvent.click(screen.getByRole('button', { name: /Wstrzyknij/i }));
    await waitFor(() => expect(screen.getByText('Service unavailable')).toBeInTheDocument());
  });

  it('injectFault_givenApiError_doesNotDisplaySuccessMessage', async () => {
    vi.mocked(api.injectCoordinatorFault).mockRejectedValue(new Error('fail'));
    render(<FaultInjectionPanel participants={participants} />);
    await userEvent.click(screen.getByRole('button', { name: /Wstrzyknij/i }));
    await waitFor(() => screen.getByText('fail'));
    expect(screen.queryByText('Usterka wstrzyknięta.')).not.toBeInTheDocument();
  });
});
