import { injectCoordinatorFault, injectFault } from '@common/api';
import type { ParticipantInfo } from '@common/types';
import type { FaultType } from '@features/fault-injection/types';
import { useState } from 'react';

/**
 * Manages fault injection state and API calls for any node target.
 *
 * @param participants list of registered participants for port resolution.
 * @returns stateful values and `send` action consumed by {@link FaultInjectionPanel}.
 */
export function useFaultInjection(participants: readonly ParticipantInfo[]) {
  const [target, setTarget] = useState<string>('coordinator');
  const [faultType, setFaultType] = useState<FaultType>('CRASH');
  const [delayMs, setDelayMs] = useState('500');
  const [status, setStatus] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  function buildParameters(): Record<string, unknown> {
    if (faultType === 'NETWORK_DELAY') return { delayMs: Number(delayMs) };
    return {};
  }

  /**
   * Injects or clears the selected fault on the selected target node.
   *
   * @param enabled {@code true} to inject, {@code false} to clear.
   */
  async function send(enabled: boolean) {
    setStatus(null);
    setError(null);
    try {
      if (target === 'coordinator') {
        await injectCoordinatorFault(faultType, enabled, buildParameters());
      } else {
        const p = participants.find((x) => x.serverId === target);
        if (!p) throw new Error(`Unknown participant: ${target}`);
        await injectFault(p.port, faultType, enabled, buildParameters());
      }
      setStatus(enabled ? 'Usterka wstrzyknięta.' : 'Usterka wyczyszczona.');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unknown error');
    }
  }

  return { target, setTarget, faultType, setFaultType, delayMs, setDelayMs, status, error, send };
}
