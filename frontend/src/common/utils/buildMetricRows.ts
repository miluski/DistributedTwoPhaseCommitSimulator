import type { MetricRowDef, MetricsSnapshot } from '@common/types';

/**
 * Builds the standard four-row metrics comparison from two snapshots.
 *
 * @param before metrics snapshot taken before the scenario ran.
 * @param after metrics snapshot taken after the scenario ran.
 * @returns array of {@link MetricRowDef} for display in the delta table.
 */
export function buildMetricRows(before: MetricsSnapshot, after: MetricsSnapshot): MetricRowDef[] {
  return [
    { label: 'Zatwierdzone', before: before.committed, after: after.committed, unit: '' },
    { label: 'Przerwane', before: before.aborted, after: after.aborted, unit: '' },
    { label: 'Niepewne', before: before.uncertain, after: after.uncertain, unit: '' },
    { label: 'Śr. decyzja', before: before.avgDecisionMs, after: after.avgDecisionMs, unit: 'ms' },
  ];
}
