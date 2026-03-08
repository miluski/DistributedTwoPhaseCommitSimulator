/** Row definition used by the metrics delta table. */
export interface MetricRowDef {
  label: string;
  before: number;
  after: number;
  unit: string;
}
