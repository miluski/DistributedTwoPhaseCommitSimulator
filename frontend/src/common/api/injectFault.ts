/** Injects or clears a fault on a participant node. */
export async function injectFault(
  port: number,
  faultType: string,
  enabled: boolean,
  parameters: Record<string, unknown> = {}
): Promise<void> {
  const res = await fetch(`/participant-proxy/${port}/api/faults`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ type: faultType, enabled, parameters }),
  });
  if (!res.ok) throw new Error(`Failed to inject fault: ${res.status}`);
}
