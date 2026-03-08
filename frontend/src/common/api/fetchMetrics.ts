import type { MetricsResponse } from '@common/types';

const BASE = '/api';

/** Fetches aggregate transaction outcome metrics from the coordinator. */
export async function fetchMetrics(): Promise<MetricsResponse> {
  const res = await fetch(`${BASE}/metrics`);
  if (!res.ok) throw new Error(`Failed to fetch metrics: ${res.status}`);
  return res.json();
}
