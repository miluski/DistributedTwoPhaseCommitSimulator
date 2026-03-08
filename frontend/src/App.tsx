import { fetchParticipantStatus, fetchParticipants, fetchTransactions } from '@common/api';
import type { ParticipantInfo, TransactionResponse } from '@common/types';
import { FaultInjectionPanel } from '@features/fault-injection';
import {
  CoordinatorCard,
  EventLog,
  MetricsBar,
  NodeCard,
  useCoordinatorStatus,
  useMetrics,
  useSimulationConfig,
  useSystemEvents,
} from '@features/node-monitoring';
import { ScenarioPanel } from '@features/scenario-benchmark';
import { TransactionPanel, TransactionTimeline, VoteMatrix } from '@features/transaction';
import { useCallback, useEffect, useRef, useState } from 'react';

const POLL_INTERVAL_MS = 5_000;

/**
 * Root application component.
 *
 * <p>Fetches participant list and transactions on mount and on each WebSocket
 * event. Renders the full monitoring dashboard including the metrics bar,
 * vote matrix, event timeline, fault injection panel, and scenario runner.
 */
export default function App() {
  const [participants, setParticipants] = useState<ParticipantInfo[]>([]);
  const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
  const [selectedTxId, setSelectedTxId] = useState<string | null>(null);
  const { events, connected } = useSystemEvents();
  const { metrics } = useMetrics();
  const { coordinatorStatus } = useCoordinatorStatus();
  const { redundancyEnabled, toggling, toggleRedundancy } = useSimulationConfig(participants);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const timelineRef = useRef<HTMLElement | null>(null);
  const eventsCountRef = useRef(0);

  const handleTransactionSelect = useCallback((txId: string) => {
    setSelectedTxId(txId);
    setTimeout(() => {
      timelineRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 50);
  }, []);

  async function loadParticipants() {
    try {
      const coordinatorList = await fetchParticipants();
      const enriched = await Promise.all(
        coordinatorList.map(async (p) => {
          const liveStatus = await fetchParticipantStatus(p.port);
          if (liveStatus) {
            return {
              ...p,
              status: liveStatus.status as (typeof p)['status'],
              activeFaults: liveStatus.activeFaults,
              committedValue: liveStatus.committedValue,
            };
          }
          return p;
        })
      );
      setParticipants(enriched);
    } catch {}
  }

  async function loadTransactions() {
    try {
      const data = await fetchTransactions();
      setTransactions(data);
    } catch {}
  }

  useEffect(() => {
    loadParticipants();
    loadTransactions();
    intervalRef.current = setInterval(() => {
      loadParticipants();
      loadTransactions();
    }, POLL_INTERVAL_MS);
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, []);

  useEffect(() => {
    if (events.length > eventsCountRef.current) {
      eventsCountRef.current = events.length;
      loadParticipants();
      loadTransactions();
    }
  }, [events]);

  return (
    <div className="min-h-screen bg-[#020817] text-slate-100 flex flex-col">
      <header className="sticky top-0 z-50 border-b border-slate-800/80 bg-[#020817]/95 backdrop-blur-md px-4 lg:px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-cyan-500 to-violet-600 flex items-center justify-center text-xs font-black tracking-tighter text-white select-none">
            2P
          </div>
          <h1 className="text-base font-bold tracking-tight text-slate-100">
            Symulator protokołu 2PC
          </h1>
        </div>
        <span
          className={`inline-flex items-center gap-1.5 text-xs font-medium px-3 py-1 rounded-full border ${
            connected
              ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
              : 'bg-rose-500/10 text-rose-400 border-rose-500/20'
          }`}
        >
          <span
            className={`w-1.5 h-1.5 rounded-full ${connected ? 'bg-emerald-400 animate-pulse' : 'bg-rose-500'}`}
          />
          {connected ? 'Na żywo' : 'Rozłączony'}
        </span>
      </header>

      <main className="flex-1 p-4 lg:p-6 space-y-6">
        <MetricsBar
          metrics={metrics}
          redundancyEnabled={redundancyEnabled}
          toggling={toggling}
          onToggle={toggleRedundancy}
        />

        <section>
          <p className="text-[11px] font-semibold uppercase tracking-widest text-slate-500 mb-3">
            Węzły sieci — {participants.length} uczestników
          </p>
          <div className="grid grid-cols-2 sm:grid-cols-4 xl:grid-cols-7 gap-3">
            <CoordinatorCard coordinatorStatus={coordinatorStatus} />
            {participants.map((p) => (
              <NodeCard
                key={p.serverId}
                serverId={p.serverId}
                port={p.port}
                status={p.status}
                activeFaults={p.activeFaults}
                lastVote={p.lastVote}
                committedValue={p.committedValue}
              />
            ))}
            {participants.length === 0 && (
              <p className="col-span-full text-slate-600 text-sm text-center py-6">
                Brak zarejestrowanych węzłów.
              </p>
            )}
          </div>
        </section>

        <section ref={timelineRef}>
          <TransactionTimeline
            events={events}
            selectedTxId={selectedTxId}
            onClearSelection={() => setSelectedTxId(null)}
          />
        </section>

        <section className="flex flex-col gap-5">
          <TransactionPanel onTransactionSelect={handleTransactionSelect} />
          <FaultInjectionPanel participants={participants} />
          <EventLog events={events.filter((e) => e.transactionId === null)} />
        </section>

        <VoteMatrix transactions={transactions} />

        <ScenarioPanel participants={participants} />
      </main>
    </div>
  );
}
