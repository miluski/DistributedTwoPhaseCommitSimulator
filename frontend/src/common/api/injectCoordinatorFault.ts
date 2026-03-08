const BASE = '/api';

/** Injects or clears a fault on the coordinator. */
export async function injectCoordinatorFault(
  faultType: string,
  enabled: boolean,
  parameters: Record<string, unknown> = {}
): Promise<void> {
  const res = await fetch(`${BASE}/coordinator/fault`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ type: faultType, enabled, parameters }),
  });
  if (!res.ok) throw new Error(`Failed to inject coordinator fault: ${res.status}`);
}
