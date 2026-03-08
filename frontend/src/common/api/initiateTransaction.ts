import type { TransactionResponse } from '@common/types';

const BASE = '/api';

const HTTP_ERROR_MESSAGES: Record<number, string> = {
  400: 'Nieprawidłowe żądanie transakcji.',
  503: 'Koordynator jest niedostępny — sprawdź czy nie działa w trybie awarii.',
  500: 'Błąd serwera koordynatora. Spróbuj ponownie.',
};

/** Initiates a new 2PC transaction with the given value. */
export async function initiateTransaction(value: string): Promise<TransactionResponse> {
  const res = await fetch(`${BASE}/transactions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ value }),
  });
  if (!res.ok) {
    const message =
      HTTP_ERROR_MESSAGES[res.status] ??
      `Nie udało się zainicjować transakcji (HTTP ${res.status}).`;
    throw new Error(message);
  }
  return res.json();
}
