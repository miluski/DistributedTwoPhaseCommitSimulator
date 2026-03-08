import type { MetricsSnapshot } from '@common/types';
import type { ScenarioVerdict } from './ScenarioVerdict';

/** Structured result returned by every scenario run. */
export interface ScenarioResult {
  verdict: ScenarioVerdict;
  summary: string;
  steps: string[];
  durationMs: number;
  metricsBefore: MetricsSnapshot;
  metricsAfter: MetricsSnapshot;
}
