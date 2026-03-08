# Architecture – DistributedTwoPhaseCommitSimulator

## 1. System Overview

The system simulates the **Two-Phase Commit (2PC)** distributed transaction protocol under various
fault scenarios, including coordinator crashes, participant crashes, network partitions, and vote
manipulation. The primary educational goal is to demonstrate:

- the blocking nature of classic 2PC when the coordinator fails mid-protocol,
- how a fault-tolerant variant using **participant peer election** and **persistent transaction logs**
  allows the system to recover and make progress.

---

## 2. Node Inventory

| Node            | Port      | Role                                                          |
| --------------- | --------- | ------------------------------------------------------------- |
| Coordinator     | 8080      | Drives the 2PC protocol; WebSocket hub for UI                 |
| Participant 1–6 | 8081–8086 | Independent 2PC participants; each is a separate JVM process  |
| React UI        | 3000      | Real-time dashboard (reads from coordinator WebSocket + REST) |

---

## 3. Two-Phase Commit Protocol Flow

```
Client           Coordinator          Participants (1-6)
  │                    │                       │
  │──POST /transactions►│                       │
  │                    │──── PREPARE(txId) ────►│ (all 6, parallel)
  │                    │◄─── VOTE: YES/NO ──────│
  │                    │                       │
  │         [all YES?] │                       │
  │              YES ──├──── COMMIT(txId) ────►│
  │              NO  ──├──── ABORT(txId) ─────►│
  │                    │◄─── ACK ──────────────│
  │◄────────────────── │  TransactionResult    │
```

### Phase 1 – PREPARE (Voting)

1. Coordinator assigns a unique `transactionId` (UUID v4).
2. Coordinator HTTP-POSTs `PrepareMessage` to all participants **in parallel**.
3. Each participant:
   - Writes `(txId, VOTED_YES)` or `(txId, VOTED_NO)` to its local log **before** responding.
   - Responds `VoteMessage{vote: YES|NO}`.
4. Coordinator waits at most `prepare.timeout.ms` (default 3 000 ms) for all votes.

### Phase 2 – COMMIT / ABORT

1. If all votes are YES → Coordinator sends `CommitMessage`; else sends `AbortMessage`.
2. Coordinator writes decision to its own log before broadcasting.
3. Each participant applies the decision, writes `(txId, COMMITTED|ABORTED)` to log, returns ACK.

---

## 4. Fault Tolerance: Coordinator Crash Recovery

Classic 2PC is **blocking**: if the coordinator crashes after Phase 1 but before Phase 2,
participants holding a YES vote are stuck (they cannot unilaterally abort or commit without
violating atomicity).

This simulator implements a recovery protocol:

### 4.1 Participant State Machine

```
INIT ──PREPARE──► PREPARED ──COMMIT──► COMMITTED
                       │
                       └──ABORT──►  ABORTED
                       │
                  [coordinator
                   timeout]
                       │
                       ▼
                  UNCERTAIN ──[peer consultation]──► COMMITTED | ABORTED
```

### 4.2 Coordinator Election

When a participant detects coordinator timeout:

1. It broadcasts `ElectionMessage` to all other participants.
2. The participant with the highest `serverId` that has a PREPARED or COMMITTED state for the
   transaction takes over as **temporary coordinator**.
3. The elected participant queries all peers for their logged state:
   - If **any** peer has `COMMITTED` → the elected coordinator re-sends COMMIT to all.
   - If **any** peer has `ABORTED` → the elected coordinator re-sends ABORT to all.
   - If **all** are still `PREPARED` (none committed or aborted yet) → elect to ABORT (safe).
4. The real coordinator, if it recovers, defers to the decision already persisted in participant logs.

---

## 5. Communication

| Channel                    | Protocol           | Purpose                                              |
| -------------------------- | ------------------ | ---------------------------------------------------- |
| Coordinator ↔ Participants | HTTP/REST (TCP/IP) | PREPARE, COMMIT, ABORT, election messages            |
| Participants ↔ Each other  | HTTP/REST (TCP/IP) | Election, peer state consultation                    |
| Coordinator → UI           | WebSocket (STOMP)  | Real-time event stream                               |
| UI → Coordinator           | HTTP/REST          | Transaction initiation, fault injection, status poll |

All REST communication happens over TCP/IP, satisfying the requirement for TCP/IP-based IPC.

**Participant registry**: At startup each participant POSTs its `{serverId, host, port}` to the
coordinator's `/api/participants/register` endpoint. The coordinator stores participant addresses
in memory.

---

## 6. Fault Injection Architecture

Each participant embeds a `FaultInjectionService` with a configurable fault state:

```java
// Active faults are stored as a ConcurrentHashMap<FaultType, FaultConfig>
// FaultConfig contains: enabled, parameter (e.g. delay ms, probability), expiresAt
```

Fault application points:

- **CRASH** – checked at the top of every controller handler; returns HTTP 503 if active.
- **NETWORK_DELAY** – `Thread.sleep(delayMs)` inserted before sending any HTTP response.
- **FORCE_ABORT_VOTE** – overrides the vote in `ParticipantService.handlePrepare()`.
- **MESSAGE_LOSS** – random check before sending HTTP response; drops with configured probability.
- **TRANSIENT** – auto-cleared by a scheduled task after `durationMs`.
- **INTERMITTENT** – scheduled task toggles the fault on/off at a configured interval.

The coordinator has its own fault injection (`CoordinatorFaultSimulatorService`):

- **CRASH** – coordinator stops processing incoming requests.
- **PARTIAL_SEND** – sends PREPARE to only a subset of participants.
- **DELAYED_DECISION** – delays the Phase 2 broadcast.

---

## 7. Real-Time Event Stream

The coordinator exposes a STOMP WebSocket at `/ws`.

Topic: `/topic/events`
Message type: `SystemEventDto`

```json
{
  "eventType": "VOTE_RECEIVED",
  "transactionId": "uuid",
  "sourceNodeId": "server-3",
  "timestamp": "2026-03-06T10:15:30.123Z",
  "payload": { "vote": "YES" }
}
```

Event types:

| Event Type              | Description                             |
| ----------------------- | --------------------------------------- |
| `TRANSACTION_STARTED`   | New transaction initiated               |
| `PREPARE_SENT`          | PREPARE broadcast to all participants   |
| `VOTE_RECEIVED`         | Single participant vote received        |
| `DECISION_MADE`         | COMMIT or ABORT decision taken          |
| `COMMIT_SENT`           | COMMIT broadcast                        |
| `ABORT_SENT`            | ABORT broadcast                         |
| `TRANSACTION_COMPLETED` | All ACKs received, transaction finished |
| `COORDINATOR_CRASHED`   | Coordinator crash fault injected        |
| `COORDINATOR_RECOVERED` | Coordinator crash fault cleared         |
| `PARTICIPANT_CRASHED`   | Participant crash fault injected        |
| `ELECTION_STARTED`      | Participant timeout triggered election  |
| `ELECTION_RESULT`       | New temporary coordinator elected       |
| `FAULT_INJECTED`        | Any fault injected                      |
| `FAULT_CLEARED`         | Any fault cleared                       |

---

## 8. Data Model

### Transaction (Coordinator)

```
Transaction
  id: UUID
  status: INITIATED | PREPARING | COMMITTED | ABORTED | UNCERTAIN
  value: String          (data being committed)
  prepareTimestamp: Instant
  decisionTimestamp: Instant
  votes: Map<String, VoteResult>   (serverId → YES/NO)
```

### LocalLog entry (Participant)

```
LogEntry
  transactionId: UUID
  phase: PREPARED | COMMITTED | ABORTED
  value: String
  timestamp: Instant
```

---

## 9. Technology Decisions

| Decision                     | Rationale                                                            |
| ---------------------------- | -------------------------------------------------------------------- |
| Spring Boot REST             | Standard, well-tested HTTP server; very easy to run as separate JVMs |
| WebSocket/STOMP              | Native push from coordinator to UI; avoids polling                   |
| In-memory H2                 | Lightweight; each participant has its own embedded store             |
| Docker Compose               | Reproducible multi-JVM deployment; easy port mapping                 |
| Separate JVM per participant | Satisfies "each element as a separate process" requirement           |
| GitHub Actions self-hosted   | SonarQube and Checkstyle running on Raspberry Pi 5 target unit       |

---

## 10. Frontend Architecture

The React dashboard (`frontend/`) is built with **React 19 + TypeScript strict mode**, **Vite**, and
**Vitest + React Testing Library**.

### 10.1 Package Structure

Code is organised by **feature**, mirroring the backend convention. Each feature is a
self-contained directory under `src/features/`. Code used by two or more features lives in
`src/common/`.

```
src/
├── App.tsx                  ← root component; imports from @features/* and @common
├── main.tsx
├── index.css
│
├── test/
│   ├── setup.ts             ← global Vitest setup (configures @testing-library/jest-dom)
│   ├── App.test.tsx
│   ├── common/
│   │   └── api/
│   │       └── coordinatorApi.test.ts
│   └── features/
│       ├── transaction/
│       │   ├── components/  ← TransactionPanel.test.tsx, TransactionTimeline.test.tsx, VoteMatrix.test.tsx
│       │   └── hooks/       ← useTransactionSubmit.test.ts
│       ├── fault-injection/
│       │   ├── components/  ← FaultInjectionPanel.test.tsx
│       │   └── hooks/       ← useFaultInjection.test.ts
│       ├── node-monitoring/
│       │   ├── components/  ← EventLog.test.tsx, MetricsBar.test.tsx, NodeCard.test.tsx
│       │   └── hooks/       ← useMetrics.test.ts, useSimulationConfig.test.ts, useSystemEvents.test.ts
│       └── scenario-benchmark/
│           ├── components/  ← ScenarioBenchmarkReport.test.tsx, ScenarioPanel.test.tsx, ScenarioSuiteReport.test.tsx
│           └── hooks/       ← useScenarioBenchmark.test.ts
│
├── common/
│   ├── index.ts             ← common barrel
│   ├── api/
│   │   └── coordinatorApi.ts        ← all REST/WebSocket fetch functions
│   ├── utils/
│   │   └── metricsFormatters.ts
│   └── types/               ← one file per interface/type (10 files)
│       ├── EventType.ts
│       ├── MetricsResponse.ts
│       ├── MetricsSnapshot.ts
│       ├── NodeStatus.ts
│       ├── ParticipantInfo.ts
│       ├── SimulationConfig.ts
│       ├── SystemEvent.ts
│       ├── TransactionResponse.ts
│       ├── TransactionStatus.ts
│       └── VoteResult.ts
│
└── features/
    ├── transaction/
    │   ├── index.ts
    │   ├── components/      ← TransactionPanel, TransactionTimeline, VoteMatrix
    │   ├── hooks/           ← useTransactionSubmit
    │   ├── model/           ← eventStyles.ts
    │   ├── utils/           ← eventFormatters.ts, voteMatrixFormatters.ts
    │   └── types/           ← TransactionPanelProps.ts, TransactionTimelineProps.ts, VoteMatrixProps.ts
    │
    ├── fault-injection/
    │   ├── index.ts
    │   ├── components/      ← FaultInjectionPanel
    │   ├── hooks/           ← useFaultInjection
    │   ├── model/           ← faultTypes.ts
    │   └── types/           ← FaultInjectionPanelProps.ts, FaultType.ts
    │
    ├── node-monitoring/
    │   ├── index.ts
    │   ├── components/      ← EventLog, MetricsBar, NodeCard
    │   ├── hooks/           ← useMetrics, useSimulationConfig, useSystemEvents
    │   ├── model/           ← nodeStatusStyles.ts, eventColorRules.ts
    │   ├── utils/           ← eventColorResolver.ts
    │   └── types/           ← EventLogProps.ts, MetricsBarProps.ts, NodeCardProps.ts
    │
    └── scenario-benchmark/
        ├── index.ts
        ├── components/      ← ScenarioBenchmarkReport, ScenarioPanel, ScenarioSuiteReport
        ├── hooks/           ← useScenarioBenchmark
        ├── model/           ← scenarioCategories.ts, scenarios.ts, verdictStyles.ts
        ├── utils/           ← categoryTabStyle.ts, scenarioHelpers.ts, scenarioReportFormatter.ts
        └── types/           ← ScenarioBenchmarkReportProps.ts, ScenarioPanelProps.ts,
                                ScenarioSuiteReportProps.ts, and 5 domain type files
```

### 10.2 Test File Location

All test files live under the central `src/test/` directory, mirroring the production source
tree. No test file exists beside a production file (no co-location).

```
src/test/features/fault-injection/components/FaultInjectionPanel.test.tsx
src/test/features/fault-injection/hooks/useFaultInjection.test.ts
```

`src/test/setup.ts` is the **global Vitest setup file** – it configures
`@testing-library/jest-dom` matchers and is referenced from `vite.config.ts` via
`setupFiles`. It contains no test cases.

### 10.3 Path Aliases

Three aliases are configured in both `tsconfig.json` (`paths`) and `vite.config.ts`
(`resolve.alias`):

| Alias       | Resolves to     |
| ----------- | --------------- |
| `@`         | `src/`          |
| `@features` | `src/features/` |
| `@common`   | `src/common/`   |

All imports that cross a sub-directory boundary must use these aliases. Parent-directory
(`../`) relative imports are forbidden everywhere. Only same-directory (`./`) relative
imports are permitted.

```ts
// Cross-feature — use the feature barrel
import { FaultInjectionPanel } from '@features/fault-injection';
import { TransactionPanel } from '@features/transaction';

// Within-feature cross-subdir — use the sub-directory barrel
import { useTransactionSubmit } from '@features/transaction/hooks';
import type { TransactionPanelProps } from '@features/transaction/types';
import { EVENT_STYLES } from '@features/transaction/model';

// Common
import type { ParticipantInfo, SystemEvent } from '@common/types';
import { fetchParticipants } from '@common/api';
```

### 10.4 Props Interface Files

Every component's `Props` interface lives in its own `.ts` file inside the feature's
`types/` directory. Inline `interface Props` declarations inside component files are
forbidden.

```ts
// src/features/transaction/types/TransactionPanelProps.ts
export interface TransactionPanelProps {
  onTransactionSelect?: (txId: string) => void;
}

// src/features/transaction/components/TransactionPanel.tsx
import type { TransactionPanelProps } from '@features/transaction/types';

export default function TransactionPanel({ onTransactionSelect }: TransactionPanelProps) { … }
```

### 10.5 Barrel Files

Every feature directory and `common/` export their public API through an `index.ts` barrel.
Every sub-directory (`components/`, `hooks/`, `types/`, `model/`, `utils/`) inside each
feature also has its own `index.ts` barrel. Barrel files re-export only the symbols that
other features or the application shell need to consume.

---

## 11. Scenario Benchmark System

The **Scenario Benchmark** panel provides 16 automated fault-tolerance test scenarios built
directly into the frontend. Each scenario:

1. Captures a before-snapshot of system metrics (`committed`, `aborted`, `avgDecisionMs`).
2. Applies one or more faults via the REST API.
3. Initiates a 2PC transaction.
4. Restores system to a clean state.
5. Captures an after-snapshot and returns a structured `ScenarioResult` with verdict
   (`PASS` | `DEGRADED` | `FAIL`) and a human-readable step log.

### 11.1 Scenario Categories

| Category                  | Description                                                                                                                                                                                                                  |
| ------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Baseline**              | Controls: happy-path commit and unanimous NO vote. Establish performance reference points.                                                                                                                                   |
| **Single Fault**          | One failure mode at a time: participant crash, network delay, force-abort vote, transient fault (1.5 s auto-recovery), intermittent fault (80% drop rate), partial Phase-2 send.                                             |
| **Compound**              | Two or more simultaneous faults: half-participants crash simultaneously; crash + delay cascade; coordinator delay combined with participant crash; all participants delayed 600 ms; intermittent + force-abort on two nodes. |
| **Extreme**               | Worst-case split-brain: coordinator collects all YES votes then sends Phase-2 to nobody (`MESSAGE_LOSS count=0`). Tests recovery scheduler detection and resolution.                                                         |
| **Redundancy Comparison** | Identical Phase-2-silent scenario run twice — first with redundancy disabled, then with redundancy enabled. Side-by-side comparison demonstrates the concrete benefit of the fault-tolerant recovery mechanism.              |

### 11.2 Redundancy Comparison — Key Design Intent

The Redundancy Comparison category directly satisfies the requirement to _"compare system
behaviour with and without redundancy under the same fault scenarios"_:

**Redundancy OFF** (`redundancy-off-partial-send`)

- Peer election immediately returns `UNCERTAIN` without consulting other nodes.
- Participants remain stuck in `PREPARING` permanently.
- No auto-recovery is possible.
- Verdict: always `DEGRADED`.

**Redundancy ON** (`redundancy-on-partial-send`)

- The `UncertainTransactionRecoveryScheduler` scans for `PREPARING` entries older than 5 s.
- It triggers peer election; participants query each other and the coordinator.
- If the coordinator recorded a commit, all participants converge to `COMMITTED`.
- Verdict: `PASS` once recovery resolves.

---

## 12. Fault Coverage Analysis

This section documents the system's fault coverage properties — which faults the 2PC protocol
detects, isolates, and recovers from, and which it can only report on.

### 12.1 Detection

| Fault Scenario                         | Detection Mechanism                             | Detection Latency                |
| -------------------------------------- | ----------------------------------------------- | -------------------------------- |
| Participant crash during Phase 1       | HTTP connection error / timeout                 | Up to `prepare.timeout.ms` (3 s) |
| Participant crash during Phase 2       | HTTP connection error from commit/abort call    | < 1 s                            |
| Coordinator crash after Phase 1        | Participant timeout on expected Phase-2 arrival | `election.timeout.ms` (5 s)      |
| Network delay exceeding threshold      | HTTP client read timeout                        | Configurable (default 3 s)       |
| Dishonest NO vote (`FORCE_ABORT_VOTE`) | Vote collected and counted by coordinator       | Immediate                        |
| Intermittent / transient failure       | Observable as periodic timeout or recovery      | Per fault config                 |

### 12.2 Recovery

| Fault Scenario                   | Recovery Mechanism                                                               | Recovery Outcome                                  |
| -------------------------------- | -------------------------------------------------------------------------------- | ------------------------------------------------- |
| Coordinator crash mid-2PC        | Participant peer election + log-based consensus                                  | All participants converge to COMMITTED or ABORTED |
| Participant crash                | Coordinator treats missing vote as NO; aborts                                    | Atomicity preserved — no partial commit           |
| Partial Phase-2 delivery         | Recovery scheduler detects PREPARING entries; peer election re-delivers decision | Participants converge within 5–10 s               |
| Transient fault                  | Auto-clear scheduler removes fault after `durationMs`                            | Node returns to service automatically             |
| Split-brain (coordinator-silent) | `UncertainTransactionRecoveryScheduler` + peer consultation                      | Resolved within recovery timeout                  |

### 12.3 Fault Coverage by Type (from project requirements)

| Fault Type Classification  | Example in Simulator                             | Covered                                      |
| -------------------------- | ------------------------------------------------ | -------------------------------------------- |
| Permanent fault (crash)    | `CRASH` on any node                              | ✅ Detected & recovered                      |
| Transient fault            | `TRANSIENT` with configurable duration           | ✅ Auto-recovered                            |
| Intermittent fault         | `INTERMITTENT` with configurable interval        | ✅ Detected; outcome probabilistic           |
| Cascade fault              | Compound scenarios (crash + delay + force-abort) | ✅ Handled                                   |
| Byzantine vote             | `FORCE_ABORT_VOTE`                               | ✅ Detected; transaction safely aborted      |
| Split-brain (Phase-2 loss) | `MESSAGE_LOSS count=0`                           | ✅ Detected & recovered (with redundancy ON) |

### 12.4 Known Limits

Classic 2PC is inherently **blocking**: if the coordinator crashes after all YES votes are
received but before any Phase-2 messages are delivered, _and_ the system is configured with
redundancy disabled, participants cannot make unilateral progress without risking inconsistency.
This is the documented blocking property of the 2PC protocol.

With redundancy enabled, this simulator resolves the blocking condition via peer election, but
the recovery window (~5 s) means some transactions are temporarily uncertain.

---

## 13. Real-Time Visualisation

The React dashboard provides four panels that together fulfil the advanced visualisation
requirements for grade 5.0:

| Panel                        | Visualisation Features                                                                                                                                                            |
| ---------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Transaction Panel**        | Live transaction list; per-transaction vote matrix (serverId × YES/NO); event timeline with colour-coded event types; decision latency display                                    |
| **Fault Injection Panel**    | Per-node fault selector; parameter sliders (delay ms, duration ms, probability); real-time active-fault badge on each node card                                                   |
| **Node Monitoring Panel**    | Live node status cards (ONLINE / CRASHED / UNCERTAIN); metrics bar (committed count, aborted count, average decision time); scrolling event log with timestamps and colour coding |
| **Scenario Benchmark Panel** | Category tabs; per-scenario run button; step-by-step execution log; before/after metrics delta; PASS / DEGRADED / FAIL verdict badge; cumulative suite report                     |

All panels receive updates via the STOMP WebSocket subscription to `/topic/events`, providing
sub-second real-time reflection of system state changes.

---
