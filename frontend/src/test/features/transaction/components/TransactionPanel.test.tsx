import * as api from '@common/api';
import type { TransactionResponse } from '@common/types';
import TransactionPanel from '@features/transaction/components/TransactionPanel';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@common/api');

const committedResponse: TransactionResponse = {
  transactionId: 'tx-1234-5678',
  status: 'COMMITTED',
  value: 'test-value',
  initiatedAt: new Date().toISOString(),
  decidedAt: new Date().toISOString(),
  votes: { 'server-1': 'YES' },
};

const abortedResponse: TransactionResponse = { ...committedResponse, status: 'ABORTED' };

describe('TransactionPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('render_givenInitialState_showsFormWithCommitButton', () => {
    render(<TransactionPanel />);
    expect(screen.getByPlaceholderText(/Wartość transakcji/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Zatwierdź/i })).toBeInTheDocument();
  });

  it('handleSubmit_givenSuccessfulCommit_displaysCommittedStatus', async () => {
    vi.mocked(api.initiateTransaction).mockResolvedValue(committedResponse);
    render(<TransactionPanel />);
    await userEvent.type(screen.getByPlaceholderText(/Wartość transakcji/i), 'hello');
    await userEvent.click(screen.getByRole('button', { name: /Zatwierdź/i }));
    await waitFor(() => expect(screen.getByText('COMMITTED')).toBeInTheDocument());
    expect(screen.getByText('COMMITTED')).toHaveClass('text-emerald-400');
  });

  it('handleSubmit_givenAbortedResult_displaysAbortedStatus', async () => {
    vi.mocked(api.initiateTransaction).mockResolvedValue(abortedResponse);
    render(<TransactionPanel />);
    await userEvent.type(screen.getByPlaceholderText(/Wartość transakcji/i), 'hello');
    await userEvent.click(screen.getByRole('button', { name: /Zatwierdź/i }));
    await waitFor(() => expect(screen.getByText('ABORTED')).toBeInTheDocument());
    expect(screen.getByText('ABORTED')).toHaveClass('text-rose-400');
  });

  it('handleSubmit_givenApiError_displaysErrorMessage', async () => {
    vi.mocked(api.initiateTransaction).mockRejectedValue(new Error('Network failure'));
    render(<TransactionPanel />);
    await userEvent.type(screen.getByPlaceholderText(/Wartość transakcji/i), 'hello');
    await userEvent.click(screen.getByRole('button', { name: /Zatwierdź/i }));
    await waitFor(() => expect(screen.getByText('Network failure')).toBeInTheDocument());
  });

  it('handleSubmit_givenApiError_doesNotDisplayResult', async () => {
    vi.mocked(api.initiateTransaction).mockRejectedValue(new Error('fail'));
    render(<TransactionPanel />);
    await userEvent.type(screen.getByPlaceholderText(/Wartość transakcji/i), 'hello');
    await userEvent.click(screen.getByRole('button', { name: /Zatwierdź/i }));
    await waitFor(() => screen.getByText('fail'));
    expect(screen.queryByText('COMMITTED')).not.toBeInTheDocument();
    expect(screen.queryByText('ABORTED')).not.toBeInTheDocument();
  });

  it('handleSubmit_givenInFlight_disablesButton', async () => {
    vi.mocked(api.initiateTransaction).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(committedResponse), 200))
    );
    render(<TransactionPanel />);
    await userEvent.type(screen.getByPlaceholderText(/Wartość transakcji/i), 'hello');
    await userEvent.click(screen.getByRole('button', { name: /Zatwierdź/i }));
    expect(screen.getByRole('button', { name: /Trwa/i })).toBeDisabled();
  });

  it('handleSubmit_givenSuccess_displaysTransactionId', async () => {
    vi.mocked(api.initiateTransaction).mockResolvedValue(committedResponse);
    render(<TransactionPanel />);
    await userEvent.type(screen.getByPlaceholderText(/Wartość transakcji/i), 'hello');
    await userEvent.click(screen.getByRole('button', { name: /Zatwierdź/i }));
    await waitFor(() => expect(screen.getByText(/tx-1234-5678/)).toBeInTheDocument());
  });

  it('handleSubmit_givenSuccessAndOnTransactionSelect_showsViewTimelineButton', async () => {
    vi.mocked(api.initiateTransaction).mockResolvedValue(committedResponse);
    const onSelect = vi.fn();
    render(<TransactionPanel onTransactionSelect={onSelect} />);
    await userEvent.type(screen.getByPlaceholderText(/Wartość transakcji/i), 'hello');
    await userEvent.click(screen.getByRole('button', { name: /Zatwierdź/i }));
    await waitFor(() => expect(screen.getByTestId('view-timeline-btn')).toBeInTheDocument());

    await userEvent.click(screen.getByTestId('view-timeline-btn'));

    expect(onSelect).toHaveBeenCalledWith('tx-1234-5678');
  });
});
