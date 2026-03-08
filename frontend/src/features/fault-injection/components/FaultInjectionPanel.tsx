import { useFaultInjection } from '@features/fault-injection/hooks';
import {
  COORDINATOR_FAULT_TYPES,
  FAULT_LABELS,
  FAULT_TYPES,
} from '@features/fault-injection/model';
import type { FaultInjectionPanelProps, FaultType } from '@features/fault-injection/types';

/**
 * Panel that lets the user select a node and inject (or clear) a fault type
 * with optional parameters.
 */
export default function FaultInjectionPanel({ participants }: FaultInjectionPanelProps) {
  const { target, setTarget, faultType, setFaultType, delayMs, setDelayMs, status, error, send } =
    useFaultInjection(participants);

  const availableFaultTypes = target === 'coordinator' ? COORDINATOR_FAULT_TYPES : FAULT_TYPES;

  function handleTargetChange(newTarget: string) {
    setTarget(newTarget);
    if (newTarget === 'coordinator' && !COORDINATOR_FAULT_TYPES.includes(faultType)) {
      setFaultType(COORDINATOR_FAULT_TYPES[0]);
    }
  }

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg">
      <h2 className="text-sm font-semibold text-slate-200 mb-4 flex items-center gap-2">
        <span className="w-1.5 h-1.5 rounded-full bg-rose-500" />
        <span>Iniekcja błędów</span>
      </h2>

      <div className="grid grid-cols-2 gap-3 mb-4">
        <div className="flex flex-col gap-1.5">
          <label htmlFor="fault-target" className="text-xs font-semibold text-slate-400">
            Węzeł docelowy
          </label>
          <select
            id="fault-target"
            className="w-full bg-slate-800 border border-slate-700 text-slate-100 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:border-rose-500/50 transition-colors"
            value={target}
            onChange={(e) => handleTargetChange(e.target.value)}
          >
            <option value="coordinator">Koordynator</option>
            {participants.map((p) => (
              <option key={p.serverId} value={p.serverId}>
                {p.serverId} :{p.port}
              </option>
            ))}
          </select>
        </div>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="fault-type" className="text-xs font-semibold text-slate-400">
            Typ błędu
          </label>
          <select
            id="fault-type"
            className="w-full bg-slate-800 border border-slate-700 text-slate-100 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:border-rose-500/50 transition-colors"
            value={faultType}
            onChange={(e) => setFaultType(e.target.value as FaultType)}
          >
            {availableFaultTypes.map((f) => (
              <option key={f} value={f}>
                {FAULT_LABELS[f]}
              </option>
            ))}
          </select>
        </div>

        {faultType === 'NETWORK_DELAY' && (
          <div className="col-span-2 flex flex-col gap-1.5">
            <label htmlFor="fault-delay-ms" className="text-xs font-semibold text-slate-400">
              Opóźnienie (ms)
            </label>
            <input
              id="fault-delay-ms"
              type="number"
              min={0}
              className="w-full bg-slate-800 border border-slate-700 text-slate-100 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:border-rose-500/50 transition-colors"
              value={delayMs}
              onChange={(e) => setDelayMs(e.target.value)}
            />
          </div>
        )}
      </div>

      <div className="flex gap-2">
        <button
          onClick={() => send(true)}
          className="flex-1 bg-rose-600 hover:bg-rose-500 text-white text-sm font-semibold px-4 py-2.5 rounded-xl transition-colors"
        >
          Wstrzyknij błąd
        </button>
        <button
          onClick={() => send(false)}
          className="flex-1 bg-slate-700 hover:bg-slate-600 text-slate-200 text-sm font-semibold px-4 py-2.5 rounded-xl transition-colors"
        >
          Wyczyść błędy
        </button>
      </div>

      {status && <p className="mt-3 text-emerald-400 text-xs font-medium">{status}</p>}
      {error && <p className="mt-3 text-rose-400 text-xs font-medium">{error}</p>}
    </div>
  );
}
