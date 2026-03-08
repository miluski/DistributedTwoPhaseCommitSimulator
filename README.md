# DistributedTwoPhaseCommitSimulator

A fault-tolerant distributed system simulator implementing the **Two-Phase Commit (2PC)** protocol
with real-time fault injection, monitoring, and automated recovery mechanisms.

Built with **Java 21 / Spring Boot 4.0.1** (backend) and **React 19 + TypeScript** (frontend),
containerised with Docker Compose, analysed with SonarQube on a self-hosted Raspberry Pi instance.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [TLS Certificates](#tls-certificates)
- [Running Individual Services](#running-individual-services)
- [Fault Injection](#fault-injection)
- [API Reference](#api-reference)
- [Testing](#testing)
- [Local SonarQube Analysis](#local-sonarqube-analysis)
- [CI/CD Pipeline](#cicd-pipeline)
- [Documentation](#documentation)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     React UI  (:3000 HTTPS)                      │
│   Dashboard │ Transaction Panel │ Fault Injection │ Event Log   │
└──────────────────────┬──────────────────────────────────────────┘
                       │  HTTPS REST + WebSocket/TLS (STOMP)
┌──────────────────────▼──────────────────────────────────────────┐
│               Coordinator  (:8443 HTTPS)                         │
│   - Initiates 2PC transactions                                   │
│   - Phase 1: PREPARE → collects votes                           │
│   - Phase 2: COMMIT / ABORT → broadcast decision                │
│   - Can be simulated as crashed (coordinator fault tolerance)    │
│   - Broadcasts system events via WebSocket                       │
└──────┬────────┬────────┬────────┬────────┬────────┬────────────┘
       │        │        │        │        │        │
 HTTPS │  HTTPS │  HTTPS │  HTTPS │  HTTPS │  HTTPS │
       ▼        ▼        ▼        ▼        ▼        ▼
  ┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
  │Server 1││Server 2││Server 3││Server 4││Server 5││Server 6│
  │ :8444  ││ :8445  ││ :8446  ││ :8447  ││ :8448  ││ :8449  │
  └────────┘└────────┘└────────┘└────────┘└────────┘└────────┘
```

The system consists of:

| Component       | Description                                                            |
| --------------- | ---------------------------------------------------------------------- |
| **Coordinator** | Manages the 2PC protocol; single point that can be simulated as failed |
| **Server 1–6**  | 2PC participants; each is an independent Spring Boot process           |
| **React UI**    | Real-time dashboard connecting via WebSocket + REST                    |

### Coordinator Fault Tolerance

When the coordinator crashes mid-transaction (after Phase 1 but before Phase 2), participants
enter an _uncertain_ state. The system handles this via:

1. **Election** – Participants elect a new coordinator using a simple ring-based algorithm.
2. **Log-based recovery** – Each participant persists its vote to a local log before responding.
3. **Peer consultation** – Participants query each other to determine if any has already received
   a COMMIT or ABORT decision and replicate it.

---

## Tech Stack

| Layer       | Technology                                                     |
| ----------- | -------------------------------------------------------------- |
| Backend     | Java 21, Spring Boot 4.0.1, Spring Web, WebSocket, Reactor     |
| API Docs    | SpringDoc OpenAPI 2.8.5 (Swagger UI at `/swagger-ui.html`)     |
| Persistence | In-memory store per participant (ConcurrentHashMap)            |
| Frontend    | React 19, TypeScript strict, Vite, SockJS + STOMP, TailwindCSS |
| Build       | Maven 3.9 (multi-module: common / coordinator / participant)   |
| Container   | Docker, Docker Compose (8 services)                            |
| CI/CD       | GitHub Actions + GitLab CI                                     |
| Quality     | SonarQube, Checkstyle (Google Java Style), JUnit 5, Mockito    |
| Coverage    | JaCoCo (≥ 75 % enforced; actual > 99 %), Vitest                |
| Docs        | Javadoc, TypeDoc, SpringDoc OpenAPI, Markdown                  |

---

## Project Structure

```
DistributedTwoPhaseCommitSimulator/
├── .github/
│   └── workflows/
│       ├── ci.yml              # Build, test, Checkstyle on every push/PR
│       └── sonarqube.yml       # SonarQube analysis (GitHub-hosted runner)
├── .gitlab/
│   └── ci.yml                  # Equivalent GitLab CI pipeline
├── backend/
│   ├── pom.xml                 # Parent Maven POM (multi-module)
│   ├── common/                 # Shared DTOs, enums, Spring Web layer
│   ├── coordinator/            # 2PC coordinator service  (:8443 HTTPS)
│   └── participant/            # 2PC participant template  (:8444–:8449 HTTPS)
├── frontend/                   # React 19 + Vite application  (:5173 HTTPS)
├── docs/
│   ├── architecture.md         # Detailed architecture and fault-tolerance design
│   ├── schedule.md             # Project schedule and milestone tracking
│   ├── api.md                  # Full REST API and WebSocket reference
│   ├── redundancy.md           # Redundancy and fault-tolerance analysis
│   └── user-guide.md           # End-user guide (Polish)
├── scripts/
│   ├── init-dev.sh             # Generate TLS keystores and .env (run once)
│   └── sonar.sh                # Run full SonarQube analysis locally
├── compose.yaml                # Full stack: coordinator + 6 participants + UI
├── sonar-project.properties    # SonarQube project configuration
└── README.md
```

---

## Quick Start

### Prerequisites

- Java 21+
- Node.js 20+
- Docker & Docker Compose
- Maven 3.9+

### Start everything with Docker Compose

```bash
git clone https://github.com/<your-org>/distributed-2pc-simulator.git
cd distributed-2pc-simulator

# Generate TLS keystores and create .env (run once; safe to re-run)
bash scripts/init-dev.sh

# Build and start all 8 containers
docker compose up --build
```

> The `init-dev.sh` script generates PKCS12 keystores for the coordinator and participants
> under `secrets/` and writes matching passwords to `.env`. Re-running it preserves existing
> keystores and reuses the password already stored in `.env`.

| URL                         | Service                                    |
| --------------------------- | ------------------------------------------ |
| https://localhost:3000      | React UI (HTTPS, nginx + self-signed cert) |
| https://localhost:8443/api  | Coordinator REST API                       |
| https://localhost:8444–8449 | Participant APIs                           |

All endpoints use self-signed certificates. Accept the browser security warning on first visit,
or import `secrets/coordinator-keystore.p12` / participant keystores into your system trust store.

---

## TLS Certificates

Every service uses PKCS12 self-signed certificates generated by `scripts/init-dev.sh`.
All certificates are stored in the `secrets/` directory and are **never committed to git**.

| File                               | Common Name   | Used by                         |
| ---------------------------------- | ------------- | ------------------------------- |
| `secrets/coordinator-keystore.p12` | `coordinator` | Coordinator HTTPS               |
| `secrets/participant-keystore.p12` | `participant` | All six participants HTTPS      |
| `secrets/trust-store.p12`          | —             | JVM trust store (both services) |

### Regenerate certificates

Delete the old files and re-run the init script. The script is idempotent — it skips keystores
that already exist, so delete only what you want to regenerate:

```bash
# Remove all secrets and generate fresh ones
rm -f secrets/coordinator-keystore.p12 secrets/participant-keystore.p12 secrets/trust-store.p12
bash scripts/init-dev.sh
```

The script automatically:

1. Generates a new random password and writes it to `.env`.
2. Creates `coordinator-keystore.p12` with a self-signed cert (`CN=coordinator`).
3. Creates `participant-keystore.p12` with a self-signed cert (`CN=participant`).
4. Exports both certificates and bundles them into `trust-store.p12` so that Spring
   services can verify each other's TLS connections at runtime.

### Trust the certificates in your browser

All services use self-signed certificates, so browsers will show a security warning.

**Option A — accept the warning per tab (fastest for development)**

Visit each URL once and click through the "Your connection is not private" warning:

```
https://localhost:3000   (React UI)
https://localhost:8443   (Coordinator)
https://localhost:8444 … https://localhost:8449   (Participants)
```

**Option B — add as trusted CA (recommended for sustained use)**

_macOS / Safari / Chrome on macOS:_

```bash
# Extract the DER certificate from the PKCS12 keystore
KEYSTORE_PASS=$(grep COORDINATOR_KEYSTORE_PASSWORD .env | cut -d= -f2)
keytool -export -alias coordinator -keystore secrets/coordinator-keystore.p12 \
  -storepass "$KEYSTORE_PASS" -file /tmp/coordinator.crt -rfc

# Add to the macOS System Keychain and mark as trusted
sudo security add-trusted-cert -d -r trustRoot \
  -k /Library/Keychains/System.keychain /tmp/coordinator.crt
```

Repeat with `participant-keystore.p12` (alias `participant`) if you access participant endpoints
directly in the browser.

_Windows (PowerShell, run as Administrator):_

```powershell
$pass = (Get-Content .env | Select-String 'COORDINATOR_KEYSTORE_PASSWORD').Line.Split('=')[1]

# Export the PEM certificate
& keytool -export -alias coordinator -keystore secrets\coordinator-keystore.p12 `
    -storepass $pass -file $env:TEMP\coordinator.crt -rfc

# Import into Windows Trusted Root Certification Authorities
Import-Certificate -FilePath "$env:TEMP\coordinator.crt" `
    -CertStoreLocation Cert:\LocalMachine\Root
```

_Linux (curl / system CA bundle):_

```bash
KEYSTORE_PASS=$(grep COORDINATOR_KEYSTORE_PASSWORD .env | cut -d= -f2)
keytool -export -alias coordinator -keystore secrets/coordinator-keystore.p12 \
  -storepass "$KEYSTORE_PASS" -file /usr/local/share/ca-certificates/coordinator.crt -rfc
sudo update-ca-certificates
```

### Use the trust store with external Java tools

If you call the API from any Java process outside Docker (e.g. a test script), point it at the
pre-built trust store:

```bash
KEYSTORE_PASS=$(grep COORDINATOR_KEYSTORE_PASSWORD .env | cut -d= -f2)
java \
  -Djavax.net.ssl.trustStore=secrets/trust-store.p12 \
  -Djavax.net.ssl.trustStorePassword="$KEYSTORE_PASS" \
  -Djavax.net.ssl.trustStoreType=PKCS12 \
  -jar your-client.jar
```

### Certificates in Docker Compose

The `JAVA_TOOL_OPTIONS` environment variable in `compose.yaml` passes the trust store path
to every JVM container automatically, so coordinator ↔ participant TLS validation works
without any additional steps when using `docker compose up`.

---

## Running Individual Services

### Backend (single machine / development)

```bash
cd backend

# Build all modules
mvn clean package -DskipTests

# Start coordinator (default port 8443, HTTPS)
java -jar coordinator/target/coordinator-*.jar

# Start participant instances (each with a different SERVER_ID and PORT)
SERVER_ID=server-1 SERVER_PORT=8444 java -jar participant/target/participant-*.jar
SERVER_ID=server-2 SERVER_PORT=8445 java -jar participant/target/participant-*.jar
# ... repeat for server-3 through server-6 on ports 8446–8449
```

### Backend on a real LAN (one process per physical machine)

Because participants self-register, the only requirement is that each machine runs Java 21
and can reach the coordinator over HTTPS.

**Coordinator machine** (e.g. `192.168.1.10`):

```bash
java -jar coordinator/target/coordinator-*.jar
# No extra flags needed — participants announce themselves at startup.
```

**Each participant machine** (replace IPs and IDs to match your network):

```bash
# Machine 192.168.1.11 — first participant
java \
  -DSERVER_ID=server-1 \
  -DSERVER_PORT=8444 \
  -DCOORDINATOR_URL=https://192.168.1.10:8443 \
  -Dparticipant.host=192.168.1.11 \
  "-DPARTICIPANT_PEERS=https://192.168.1.11:8444,https://192.168.1.12:8444,https://192.168.1.13:8444,https://192.168.1.14:8444,https://192.168.1.15:8444,https://192.168.1.16:8444" \
  -jar participant/target/participant-*.jar
```

> All six participant URLs must be listed in `PARTICIPANT_PEERS` as a single comma-separated
> string — one per physical machine, all on the same port (8444 by default). Spring's `@Value`
> binding splits the string on commas automatically.

> **`-Dparticipant.host`** must be set to the machine's own LAN IP (or hostname).
> This is the address the coordinator will use to send PREPARE / COMMIT / ABORT requests.
> Without it, participants register as `localhost` and the coordinator cannot reach them.

**Startup order:**

1. Start the coordinator first and wait until you see `Started CoordinatorApplication`.
2. Start each participant — each will log `Registered with coordinator as server-N on port XXXX`.
3. Open the React UI and confirm all nodes appear in the dashboard.

**SSL on LAN:** The embedded keystores (`classpath:ssl/participant-keystore.p12`) contain
self-signed certificates issued for the `participant` hostname. To use real IP addresses you
have two options:

- Set `-DSSL_KEYSTORE_PATH=/path/to/your.p12` to point to a keystore whose Subject Alternative
  Names (SANs) include your LAN IPs.
- Or generate new keystores with the correct SANs using `openssl` + `keytool`, then mount them at runtime.

### Frontend

```bash
cd frontend
npm install
npm run dev        # https://localhost:5173  (Vite self-signed cert, accept browser warning)
```

The production Docker image (`frontend/Dockerfile`) serves the built static assets via
**nginx with a self-signed TLS certificate** generated at image build time. The UI is
accessible at `https://localhost:3000` when running with Docker Compose.

---

## Fault Injection

The UI and REST API expose two independent fault injection surfaces: one for the coordinator and
one per participant. Every node supports at least six distinct fault types.

### Participant faults (all six participants independently configurable)

| Fault Type             | Code               | Description                                                  |
| ---------------------- | ------------------ | ------------------------------------------------------------ |
| **Node Crash**         | `CRASH`            | Node returns HTTP 503 to every incoming request              |
| **Network Delay**      | `NETWORK_DELAY`    | Adds configurable latency (ms) before sending any response   |
| **Vote Manipulation**  | `FORCE_ABORT_VOTE` | Forces the participant to vote NO on the next transaction(s) |
| **Message Loss**       | `MESSAGE_LOSS`     | Drops outbound messages with a configured probability        |
| **Transient Fault**    | `TRANSIENT`        | Fault active for a fixed duration, then auto-recovers        |
| **Intermittent Fault** | `INTERMITTENT`     | Fault toggles on/off at a configurable interval              |

### Coordinator faults

| Fault Type            | Code               | Description                                           |
| --------------------- | ------------------ | ----------------------------------------------------- |
| **Coordinator Crash** | `CRASH`            | Coordinator stops processing incoming requests        |
| **Partial Send**      | `PARTIAL_SEND`     | Phase-2 COMMIT sent to only N of M participants       |
| **Delayed Decision**  | `DELAYED_DECISION` | Artificially delays the Phase-2 broadcast             |
| **Message Loss**      | `MESSAGE_LOSS`     | Phase-2 sent to exactly N participants (configurable) |
| **Network Delay**     | `NETWORK_DELAY`    | Adds latency to all coordinator outbound HTTP calls   |

Inject a fault via REST:

```bash
# Crash participant 2
curl -X POST https://localhost:8445/api/faults \
  -H "Content-Type: application/json" \
  -d '{"type":"CRASH","enabled":true}'

# Restore participant 2
curl -X POST https://localhost:8445/api/faults \
  -H "Content-Type: application/json" \
  -d '{"type":"CRASH","enabled":false}'
```

---

## Scenario Benchmark

The frontend's **Scenario Benchmark** panel provides 16 automated fault-tolerance test scenarios
grouped into five categories. Each scenario runs end-to-end against the live system, compares
before/after metrics, and returns a `PASS`, `DEGRADED`, or `FAIL` verdict with a step log.

| Category                  | Scenarios                                                              |
| ------------------------- | ---------------------------------------------------------------------- |
| **Baseline**              | Happy path commit, unanimous NO vote                                   |
| **Single Fault**          | Participant crash, network delay, force-abort vote, transient fault,   |
|                           | intermittent fault (80% drop), partial Phase-2 send                    |
| **Compound (Cascading)**  | Half participants crash simultaneously; crash + delay cascade;         |
|                           | coordinator delay + participant crash; all participants delayed;       |
|                           | intermittent + force-abort on two nodes simultaneously                 |
| **Extreme**               | Coordinator silent after Phase 1 — worst-case split-brain scenario     |
| **Redundancy Comparison** | Identical Phase-2-silent scenario with redundancy OFF vs ON — directly |
|                           | demonstrates the value of the fault-tolerant recovery mechanism        |

The **Redundancy Comparison** category is the direct implementation of the requirement
_"compare system behaviour with and without redundancy under the same fault scenarios"_:

- **Redundancy OFF**: participants remain stuck in PREPARING indefinitely (no auto-recovery).
- **Redundancy ON**: the recovery scheduler resolves the uncertain state via peer election
  within the configured 5-second timeout.

---

## API Reference

See [docs/api.md](docs/api.md) for the full REST API reference.
Swagger UI is available at `https://localhost:<port>/swagger-ui.html` when any service is running.

### Key endpoints

| Method | URL                       | Description                             |
| ------ | ------------------------- | --------------------------------------- |
| POST   | `/api/transactions`       | Initiate a new 2PC transaction          |
| GET    | `/api/transactions`       | List all transactions                   |
| GET    | `/api/transactions/{id}`  | Get transaction details + vote matrix   |
| GET    | `/api/participants`       | List registered participants            |
| POST   | `/api/coordinator/fault`  | Inject fault into coordinator           |
| GET    | `/api/coordinator/status` | Get coordinator state                   |
| POST   | `/api/faults`             | Inject fault into participant           |
| GET    | `/api/status`             | Get participant state and active faults |
| GET    | `/api/metrics`            | Transaction throughput and timing       |
| POST   | `/api/simulation/config`  | Toggle redundancy ON/OFF                |

WebSocket endpoint: `wss://localhost:8443/ws`
Subscribe to topic: `/topic/events`

---

## Testing

```bash
# Run all tests with coverage report (enforces ≥ 75 % threshold)
cd backend
mvn verify -Dcheckstyle.skip=true

# Coverage HTML reports:
# backend/common/target/site/jacoco/index.html
# backend/coordinator/target/site/jacoco/index.html
# backend/participant/target/site/jacoco/index.html
```

Current coverage (all modules): **> 99 % line, instruction, and method coverage**.
JaCoCo checks are enforced at 75 % minimum; builds fail if coverage drops below this threshold.

---

## Local SonarQube Analysis

Run the full analysis (backend + frontend build, tests, coverage, and SonarQube scan) with a single script:

```bash
./scripts/sonar.sh
```

The script prompts for the SonarQube URL and token interactively (token input is hidden).
You can also pre-set them as environment variables to skip the prompts:

```bash
export SONAR_HOST_URL=https://<sonarqube-host>:9443
export SONAR_TOKEN=sqp_...
./scripts/sonar.sh
```

---

## CI/CD Pipeline

Every push and pull request triggers the `backend` and `frontend` jobs (compile, test, coverage
artifacts). On `main` push only:

1. **`ci.yml` `docs` job** (GitHub-hosted runner): generates Javadoc (backend) and TypeDoc
   (frontend) and commits them to `docs/backend/` and `docs/frontend/`
2. **`sonarqube.yml`** (GitHub-hosted runner): backend + frontend tests, then SonarQube scan
   with coverage import
3. **`.gitlab/ci.yml`**: equivalent pipeline for GitLab CI (build, test, SonarQube, docs)

See [`.github/workflows/`](.github/workflows/) and [`.gitlab/ci.yml`](.gitlab/ci.yml) for details.

---

## Documentation

| Document            | Location                                     |
| ------------------- | -------------------------------------------- |
| Architecture        | [docs/architecture.md](docs/architecture.md) |
| Project Schedule    | [docs/schedule.md](docs/schedule.md)         |
| API Reference       | [docs/api.md](docs/api.md)                   |
| Redundancy Analysis | [docs/redundancy.md](docs/redundancy.md)     |
| User Guide          | [docs/user-guide.md](docs/user-guide.md)     |
| Swagger UI (live)   | `https://localhost:<port>/swagger-ui.html`   |
| Javadoc (generated) | `docs/backend/` (committed on `main` push)   |
| TypeDoc (generated) | `docs/frontend/` (committed on `main` push)  |
