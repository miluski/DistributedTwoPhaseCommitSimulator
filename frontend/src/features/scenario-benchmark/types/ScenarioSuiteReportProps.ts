import type { SuiteReport } from './SuiteReport';

/** Props for the {@link ScenarioSuiteReport} component. */
export interface ScenarioSuiteReportProps {
  /** The aggregated suite run report. */
  readonly report: SuiteReport;
}
