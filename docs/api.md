# REST API Reference – DistributedTwoPhaseCommitSimulator

## Base URLs

| Service       | Base URL                       |
| ------------- | ------------------------------ |
| Coordinator   | `https://localhost:8443/api`   |
| Participant N | `https://localhost:844{N}/api` |

Swagger UI (interactive): `https://localhost:<port>/swagger-ui.html`
OpenAPI JSON: `https://localhost:<port>/v3/api-docs`

---

## Coordinator Endpoints

### Transactions

#### `POST /api/transactions`

Initiate a new 2PC transaction.

**Request body:**

```json
{
  "value": "update:account=42,amount=100"
}
```

**Response `201 Created`:**

```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "INITIATED",
  "value": "update:account=42,amount=100",
  "initiatedAt": "2026-03-06T10:00:00Z"
}
```

---

#### `GET /api/transactions`

List all transactions.

**Response `200 OK`:** Array of `TransactionResponse`

---

#### `GET /api/transactions/{transactionId}`

Get full transaction details including all votes.

**Response `200 OK`:**

```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMMITTED",
  "value": "update:account=42,amount=100",
  "initiatedAt": "2026-03-06T10:00:00Z",
  "decidedAt": "2026-03-06T10:00:01.245Z",
  "votes": {
    "server-1": "YES",
    "server-2": "YES",
    "server-3": "YES",
    "server-4": "YES",
    "server-5": "YES",
    "server-6": "YES"
  }
}
```

---

### Participants (registry)

#### `GET /api/participants`

List all registered participants.

**Response `200 OK`:**

```json
[
  { "serverId": "server-1", "host": "participant-1", "port": 8081, "status": "ONLINE" },
  { "serverId": "server-2", "host": "participant-2", "port": 8082, "status": "ONLINE" }
]
```

#### `POST /api/participants/register`

Register a participant (called automatically at participant startup).

**Request body:**

```json
{ "serverId": "server-1", "host": "participant-1", "port": 8081 }
```

---

### Coordinator fault injection

#### `POST /api/coordinator/fault`

Inject or update a fault on the coordinator.

**Request body:**

```json
{
  "type": "CRASH",
  "enabled": true,
  "parameters": {}
}
```

Supported types: `CRASH`, `PARTIAL_SEND`, `DELAYED_DECISION`

For `PARTIAL_SEND`:

```json
{
  "type": "PARTIAL_SEND",
  "enabled": true,
  "parameters": { "sendToCount": 3 }
}
```

For `DELAYED_DECISION`:

```json
{
  "type": "DELAYED_DECISION",
  "enabled": true,
  "parameters": { "delayMs": 5000 }
}
```

**Response `200 OK`:** `{ "message": "Fault CRASH injected on coordinator" }`

---

#### `GET /api/coordinator/status`

Get coordinator status and active faults.

**Response `200 OK`:**

```json
{
  "status": "ONLINE",
  "activeFaults": ["CRASH"],
  "transactionCount": 42,
  "lastTransactionId": "550e8400-..."
}
```

---

## Participant Endpoints

### 2PC protocol (called by coordinator)

#### `POST /api/2pc/prepare`

**Request:** `PrepareMessage { transactionId, value }`
**Response:** `VoteMessage { transactionId, vote: YES|NO, serverId }`

#### `POST /api/2pc/commit`

**Request:** `CommitMessage { transactionId }`
**Response:** `204 No Content`

#### `POST /api/2pc/abort`

**Request:** `AbortMessage { transactionId }`
**Response:** `204 No Content`

---

### Peer protocol (called by other participants during election)

#### `GET /api/peers/log/{transactionId}`

Returns participant's persisted log entry for the given transaction.

**Response `200 OK`:**

```json
{
  "transactionId": "550e8400-...",
  "phase": "COMMITTED",
  "serverId": "server-3",
  "timestamp": "2026-03-06T10:00:01Z"
}
```

#### `POST /api/peers/elect`

Trigger election for a stuck transaction.

**Request:** `ElectionMessage { transactionId, initiatorId }`
**Response `200 OK`:** `{ "elected": "server-6" }`

---

### Fault injection

#### `POST /api/faults`

Inject a fault.

**Request body:**

```json
{
  "type": "NETWORK_DELAY",
  "enabled": true,
  "parameters": { "delayMs": 2000 }
}
```

**Supported types:**

| Type               | Parameter keys            |
| ------------------ | ------------------------- |
| `CRASH`            | _(none)_                  |
| `NETWORK_DELAY`    | `delayMs` (integer)       |
| `FORCE_ABORT_VOTE` | `forNextN` (integer)      |
| `MESSAGE_LOSS`     | `probability` (0.0–1.0)   |
| `TRANSIENT`        | `durationMs` (integer)    |
| `INTERMITTENT`     | `onMs`, `offMs` (integer) |

**Response `200 OK`:** `{ "message": "Fault NETWORK_DELAY injected" }`

---

#### `DELETE /api/faults/{type}`

Clear a specific fault.

**Response `200 OK`:** `{ "message": "Fault NETWORK_DELAY cleared" }`

---

#### `GET /api/faults`

List all active faults on this participant.

**Response `200 OK`:**

```json
[{ "type": "NETWORK_DELAY", "parameters": { "delayMs": 2000 } }]
```

---

### Status

#### `GET /api/status`

Get current participant state.

**Response `200 OK`:**

```json
{
  "serverId": "server-3",
  "port": 8083,
  "status": "ONLINE",
  "activeFaults": [],
  "logEntries": 12,
  "lastTransactionId": "550e8400-..."
}
```

---

## WebSocket

**Endpoint:** `wss://localhost:8443/ws`
**Protocol:** STOMP over SockJS
**Subscribe:** `/topic/events`

**Message format:**

```json
{
  "eventType": "VOTE_RECEIVED",
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "sourceNodeId": "server-3",
  "targetNodeId": "coordinator",
  "timestamp": "2026-03-06T10:00:00.456Z",
  "payload": { "vote": "YES" }
}
```

---

## Coordinator Simulation Config

### `GET /api/simulation/config`

Get the current simulation mode.

**Response `200 OK`:**

```json
{ "redundancyEnabled": true }
```

### `POST /api/simulation/config`

Toggle redundancy (fault-tolerant recovery) on or off.

**Request body:**

```json
{ "redundancyEnabled": false }
```

**Response `200 OK`:** `{ "message": "Simulation mode updated" }`

When `redundancyEnabled` is `false`, the `UncertainTransactionRecoveryScheduler` is disabled,
so any transaction stuck in `PREPARING` after a coordinator crash will remain unresolved
indefinitely. This is the "no fault tolerance" baseline used in the Redundancy Comparison
scenario benchmark.

---

## Coordinator Metrics

### `GET /api/metrics`

Get aggregate transaction metrics from the coordinator.

**Response `200 OK`:**

```json
{
  "committed": 42,
  "aborted": 7,
  "total": 49,
  "avgDecisionMs": 312.5,
  "activeTransactions": 0
}
```

---

## Participant Simulation Config

### `GET /api/simulation/config`

Get the current simulation mode for this participant.

**Response `200 OK`:**

```json
{ "redundancyEnabled": true }
```

### `POST /api/simulation/config`

Enable or disable the peer-election recovery path on this participant.

**Request body:**

```json
{ "redundancyEnabled": false }
```

**Response `200 OK`:** `{ "message": "Simulation mode updated" }`
