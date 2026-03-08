import type { ScenarioResult } from './ScenarioResult';

/** Props for the {@link ScenarioBenchmarkReport} component. */
export interface ScenarioBenchmarkReportProps {
  /** The structured scenario run result. */
  result: ScenarioResult;
  /** The scenario display label shown in the header. */
  label: string;
}
