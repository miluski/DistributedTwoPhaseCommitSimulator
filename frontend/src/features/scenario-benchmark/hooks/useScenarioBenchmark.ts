import type { ParticipantInfo } from '@common/types';
import { CATEGORIES, SCENARIOS } from '@features/scenario-benchmark/model';
import type {
  ScenarioCategory,
  ScenarioRunResult,
  SuiteReport,
  UseScenarioBenchmarkReturn,
} from '@features/scenario-benchmark/types';
import { executeOne, runAll } from '@features/scenario-benchmark/utils';
import { useCallback, useState } from 'react';

/**
 * Hook encapsulating all fault scenario logic and state for the benchmark
 * runner. Provides individual scenario execution, full suite execution, and
 * structured results for display.
 *
 * @param participants list of registered participant nodes used by scenario runners.
 * @returns state and action functions for the scenario benchmark UI.
 */
export function useScenarioBenchmark(
  participants: readonly ParticipantInfo[]
): UseScenarioBenchmarkReturn {
  const [running, setRunning] = useState(false);
  const [currentResult, setCurrentResult] = useState<ScenarioRunResult | null>(null);
  const [suiteRunning, setSuiteRunning] = useState(false);
  const [suiteProgress, setSuiteProgress] = useState<{ current: number; total: number } | null>(
    null
  );
  const [suiteResults, setSuiteResults] = useState<SuiteReport | null>(null);

  const runScenario = useCallback(
    async (id: string) => {
      const scenario = SCENARIOS.find((s) => s.id === id);
      if (!scenario || running) return;
      setRunning(true);
      try {
        const res = await executeOne(scenario, participants);
        setCurrentResult(res);
      } finally {
        setRunning(false);
      }
    },
    [participants, running]
  );

  const runSuite = useCallback(
    async (category?: ScenarioCategory) => {
      if (suiteRunning) return;
      const toRun = category ? SCENARIOS.filter((s) => s.category === category) : [...SCENARIOS];
      setSuiteRunning(true);
      setSuiteResults(null);
      try {
        const report = await runAll(toRun, participants, (c, t) =>
          setSuiteProgress({ current: c, total: t })
        );
        setSuiteResults(report);
      } finally {
        setSuiteProgress(null);
        setSuiteRunning(false);
      }
    },
    [participants, suiteRunning]
  );

  const clearResults = useCallback(() => {
    setCurrentResult(null);
    setSuiteResults(null);
  }, []);

  return {
    scenarios: SCENARIOS,
    categories: CATEGORIES,
    running,
    currentResult,
    suiteRunning,
    suiteProgress,
    suiteResults,
    runScenario,
    runSuite,
    clearResults,
  };
}
