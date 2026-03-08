import type { EventType } from '@common/types';

/** Polish display labels for each WebSocket event type. */
export const EVENT_LABELS: Record<EventType, string> = {
  TRANSACTION_STARTED: 'Transakcja rozpoczęta',
  PREPARE_SENT: 'Faza 1: PREPARE wysłany',
  VOTE_RECEIVED: 'Głos odebrany',
  ALL_VOTES_COLLECTED: 'Wszystkie głosy zebrane',
  DECISION_MADE: 'Decyzja podjęta',
  COMMIT_SENT: 'Faza 2: COMMIT wysłany',
  ABORT_SENT: 'Faza 2: ABORT wysłany',
  TRANSACTION_COMPLETED: 'Transakcja zakończona',
  TRANSACTION_TIMED_OUT: 'Transakcja przekroczyła limit czasu',
  COORDINATOR_CRASHED: 'Koordynator uległ awarii',
  COORDINATOR_RECOVERED: 'Koordynator wznowił działanie',
  PARTICIPANT_CRASHED: 'Uczestnik uległ awarii',
  PARTICIPANT_RECOVERED: 'Uczestnik wznowił działanie',
  ELECTION_STARTED: 'Wybory lidera rozpoczęte',
  ELECTION_RESULT: 'Wynik wyborów lidera',
  PEER_CONSULTED: 'Konsultacja z węzłem',
  FAULT_INJECTED: 'Błąd wstrzyknięty',
  FAULT_CLEARED: 'Błąd wyczyszczony',
  PARTICIPANT_REGISTERED: 'Uczestnik zarejestrowany',
};
