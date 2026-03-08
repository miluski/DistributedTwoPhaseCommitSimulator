import { EVENT_TYPE_LABELS } from '@features/node-monitoring/model/eventTypeLabels';
import { describe, expect, it } from 'vitest';

describe('EVENT_TYPE_LABELS', () => {
  it('givenFaultCleared_returnsPolishLabel', () => {
    expect(EVENT_TYPE_LABELS['FAULT_CLEARED']).toBe('Błąd wyczyszczony');
  });

  it('givenFaultInjected_returnsPolishLabel', () => {
    expect(EVENT_TYPE_LABELS['FAULT_INJECTED']).toBe('Błąd wstrzyknięty');
  });

  it('givenTransactionStarted_returnsPolishLabel', () => {
    expect(EVENT_TYPE_LABELS['TRANSACTION_STARTED']).toBe('Transakcja rozpoczęta');
  });

  it('givenTransactionCompleted_returnsPolishLabel', () => {
    expect(EVENT_TYPE_LABELS['TRANSACTION_COMPLETED']).toBe('Transakcja zakończona');
  });

  it('givenAbortSent_returnsPolishLabel', () => {
    expect(EVENT_TYPE_LABELS['ABORT_SENT']).toBe('Wysłano ABORT');
  });

  it('givenCommitSent_returnsPolishLabel', () => {
    expect(EVENT_TYPE_LABELS['COMMIT_SENT']).toBe('Wysłano COMMIT');
  });

  it('givenCoordinatorCrashed_returnsPolishLabel', () => {
    expect(EVENT_TYPE_LABELS['COORDINATOR_CRASHED']).toBe('Awaria koordynatora');
  });

  it('givenParticipantCrashed_returnsPolishLabel', () => {
    expect(EVENT_TYPE_LABELS['PARTICIPANT_CRASHED']).toBe('Awaria uczestnika');
  });

  it('givenElectionStarted_returnsPolishLabel', () => {
    expect(EVENT_TYPE_LABELS['ELECTION_STARTED']).toBe('Wybory rozpoczęte');
  });

  it('givenParticipantRegistered_returnsPolishLabel', () => {
    expect(EVENT_TYPE_LABELS['PARTICIPANT_REGISTERED']).toBe('Uczestnik zarejestrowany');
  });
});
