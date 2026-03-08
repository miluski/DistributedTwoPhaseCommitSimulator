import type { Scenario } from './Scenario';
import type { ScenarioCategory } from './ScenarioCategory';
import type { ScenarioRunResult } from './ScenarioRunResult';
import type { SuiteReport } from './SuiteReport';

/** Return type of {@link useScenarioBenchmark}. */
export interface UseScenarioBenchmarkReturn {
  scenarios: Scenario[];
  categories: ScenarioCategory[];
  running: boolean;
  currentResult: ScenarioRunResult | null;
  suiteRunning: boolean;
  suiteProgress: { current: number; total: number } | null;
  suiteResults: SuiteReport | null;
  runScenario: (id: string) => Promise<void>;
  runSuite: (category?: ScenarioCategory) => Promise<void>;
  clearResults: () => void;
}
