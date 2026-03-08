import ScenarioBenchmarkReport from '@features/scenario-benchmark/components/ScenarioBenchmarkReport';
import ScenarioSuiteReport from '@features/scenario-benchmark/components/ScenarioSuiteReport';
import { useScenarioBenchmark } from '@features/scenario-benchmark/hooks';
import {
  SCENARIOS as ALL_SCENARIOS,
  CATEGORIES,
  CATEGORY_LABELS,
} from '@features/scenario-benchmark/model';
import type { ScenarioCategory, ScenarioPanelProps } from '@features/scenario-benchmark/types';
import { categoryTabStyle } from '@features/scenario-benchmark/utils';
import { useState } from 'react';

/**
 * Fault tolerance benchmark panel providing comprehensive scenario coverage
 * across baseline, single-fault, compound, extreme, and redundancy categories.
 *
 * <p>Scenarios produce structured {@link ScenarioResult} objects displayed via
 * {@link ScenarioBenchmarkReport}, enabling direct comparison and conclusions
 * about system fault tolerance. The suite runner executes all scenarios and
 * produces a copyable aggregate report.
 *
 * @param participants list of currently registered participant nodes.
 */
export default function ScenarioPanel({ participants }: ScenarioPanelProps) {
  const [selectedCategory, setSelectedCategory] = useState<ScenarioCategory>('baseline');
  const [selectedId, setSelectedId] = useState<string>(
    ALL_SCENARIOS.find((s) => s.category === 'baseline')?.id ?? ''
  );
  const {
    running,
    currentResult,
    suiteRunning,
    suiteProgress,
    suiteResults,
    runScenario,
    runSuite,
    clearResults,
  } = useScenarioBenchmark(participants);

  const categoryScenarios = ALL_SCENARIOS.filter((s) => s.category === selectedCategory);
  const selected = categoryScenarios.find((s) => s.id === selectedId) ?? categoryScenarios[0];
  const anyBusy = running || suiteRunning;

  function handleCategoryChange(cat: ScenarioCategory) {
    setSelectedCategory(cat);
    const first = ALL_SCENARIOS.find((s) => s.category === cat);
    if (first) setSelectedId(first.id);
  }

  return (
    <div
      data-testid="scenario-panel"
      className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg flex flex-col gap-4"
    >
      <h2 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
        <span className="w-1.5 h-1.5 rounded-full bg-purple-500" />
        <span>Test odporności na błędy</span>
      </h2>

      <div data-testid="category-tabs" className="flex flex-wrap gap-1.5">
        {CATEGORIES.map((cat) => (
          <button
            key={cat}
            data-testid={`category-tab-${cat}`}
            onClick={() => handleCategoryChange(cat)}
            className={categoryTabStyle(selectedCategory === cat)}
          >
            {CATEGORY_LABELS[cat]}
          </button>
        ))}
      </div>

      <div className="flex flex-col gap-1.5">
        <label
          htmlFor="scenario-select"
          className="text-[11px] font-medium text-slate-500 uppercase tracking-wider"
        >
          Scenariusz
        </label>
        <select
          id="scenario-select"
          data-testid="scenario-select"
          className="w-full bg-slate-800 border border-slate-700 text-slate-100 rounded-xl px-3 py-2 text-sm focus:outline-none focus:border-purple-500/50 transition-colors"
          value={selectedId}
          onChange={(e) => setSelectedId(e.target.value)}
        >
          {categoryScenarios.map((s) => (
            <option key={s.id} value={s.id}>
              {s.label}
            </option>
          ))}
        </select>
      </div>

      {selected && (
        <div className="text-xs text-slate-500 space-y-1 border-l-2 border-slate-700 pl-3">
          <p className="text-slate-400 leading-relaxed">{selected.description}</p>
          <p>
            <span className="text-slate-600">Oczekiwane: </span>
            <span className="text-slate-400">{selected.expectedBehaviour}</span>
          </p>
        </div>
      )}

      <div className="flex flex-wrap gap-2">
        <button
          data-testid="run-scenario-btn"
          onClick={() => runScenario(selectedId)}
          disabled={anyBusy}
          className="bg-purple-600 hover:bg-purple-500 disabled:opacity-40 text-white text-xs font-semibold px-3 py-1.5 rounded-xl transition-colors"
        >
          {running ? 'Trwa…' : 'Uruchom scenariusz'}
        </button>
        <button
          data-testid="run-suite-btn"
          onClick={() => runSuite(selectedCategory)}
          disabled={anyBusy}
          className="bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 text-white text-xs font-semibold px-3 py-1.5 rounded-xl transition-colors"
        >
          {suiteRunning
            ? `Trwa ${suiteProgress?.current ?? 0}/${suiteProgress?.total ?? 0}…`
            : `Uruchom serię „${CATEGORY_LABELS[selectedCategory]}”`}
        </button>
        <button
          data-testid="run-all-btn"
          onClick={() => runSuite()}
          disabled={anyBusy}
          className="bg-teal-700 hover:bg-teal-600 disabled:opacity-40 text-white text-xs font-semibold px-3 py-1.5 rounded-xl transition-colors"
        >
          Uruchom wszystkie ({ALL_SCENARIOS.length})
        </button>
        {(currentResult !== null || suiteResults !== null) && (
          <button
            data-testid="clear-results-btn"
            onClick={clearResults}
            disabled={anyBusy}
            className="bg-slate-700 hover:bg-slate-600 disabled:opacity-40 text-slate-200 text-xs font-semibold px-3 py-1.5 rounded-xl transition-colors"
          >
            Wyczyść
          </button>
        )}
      </div>

      {suiteProgress && (
        <p data-testid="suite-progress" className="text-xs text-sky-400 font-medium">
          Uruchamianie scenariusza {suiteProgress.current} z {suiteProgress.total}…
        </p>
      )}

      {currentResult && (
        <ScenarioBenchmarkReport
          result={currentResult.result}
          label={currentResult.scenario.label}
        />
      )}

      {suiteResults && <ScenarioSuiteReport report={suiteResults} />}
    </div>
  );
}
