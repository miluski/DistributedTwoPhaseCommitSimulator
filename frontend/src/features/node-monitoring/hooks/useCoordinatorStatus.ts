import { fetchCoordinatorStatus } from '@common/api';
import type { CoordinatorStatusResponse } from '@common/types';
import { useCallback, useEffect, useState } from 'react';

const POLL_INTERVAL_MS = 3_000;

/**
 * Polls the coordinator status endpoint every 3 seconds and returns the latest
 * health state of the coordinator node.
 *
 * @returns the most recent {@link CoordinatorStatusResponse}, or {@code null} while loading.
 */
export function useCoordinatorStatus(): { coordinatorStatus: CoordinatorStatusResponse | null } {
  const [coordinatorStatus, setCoordinatorStatus] = useState<CoordinatorStatusResponse | null>(
    null
  );

  const load = useCallback(async () => {
    try {
      const data = await fetchCoordinatorStatus();
      setCoordinatorStatus(data);
    } catch {}
  }, []);

  useEffect(() => {
    load();
    const id = setInterval(load, POLL_INTERVAL_MS);
    return () => clearInterval(id);
  }, [load]);

  return { coordinatorStatus };
}
