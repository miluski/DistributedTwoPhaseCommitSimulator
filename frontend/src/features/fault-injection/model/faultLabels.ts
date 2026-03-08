import type { FaultType } from '@features/fault-injection/types';

/** Polish display labels for each injectable fault type. */
export const FAULT_LABELS: Record<FaultType, string> = {
  CRASH: 'Awaria węzła (CRASH)',
  NETWORK_DELAY: 'Opóźnienie sieci',
  FORCE_ABORT_VOTE: 'Wymuszony głos NIE',
  MESSAGE_LOSS: 'Utrata wiadomości',
  TRANSIENT: 'Błąd chwilowy',
  INTERMITTENT: 'Błąd przerywany',
};
