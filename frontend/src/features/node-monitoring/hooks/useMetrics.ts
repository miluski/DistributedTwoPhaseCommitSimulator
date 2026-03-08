import { useCallback, useEffect, useState } from 'react';
import { fetchMetrics } from '@common/api';
import type { MetricsResponse } from '@common/types';

const POLL_INTERVAL_MS = 3_000;

/**
 * Polls the coordinator metrics endpoint every 3 seconds and returns the latest
 * aggregate transaction statistics.
 *
 * @returns the most recent {@link MetricsResponse}, or {@code null} while loading.
 */
export function useMetrics(): { metrics: MetricsResponse | null } {
  const [metrics, setMetrics] = useState<MetricsResponse | null>(null);

  const load = useCallback(async () => {
    try {
      const data = await fetchMetrics();
      setMetrics(data);
    } catch {}
  }, []);

  useEffect(() => {
    load();
    const id = setInterval(load, POLL_INTERVAL_MS);
    return () => clearInterval(id);
  }, [load]);

  return { metrics };
}
