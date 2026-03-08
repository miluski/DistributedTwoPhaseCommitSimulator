# GitHub Copilot Instructions – DistributedTwoPhaseCommitSimulator

These rules apply to **all** code suggestions in this repository.
Copilot must adhere to every rule listed here without exception.

---

## 1. Architecture & Package Structure

- Organise code by **feature**, not by layer.
  - `coordinator/feature/transaction/`, `coordinator/feature/fault/`, `participant/feature/protocol/`, etc.
  - Never create top-level `service/`, `repository/`, `controller/` packages.
- Every feature package must contain clear sub-packages: `api` (controllers/REST), `domain` (models, ports), `application` (use-case services), `infrastructure` (adapters, persistence).
- **No DDD** – do not use `Aggregate`, `AggregateRoot`, `Domain Event` patterns or DDD terminology. Use plain Java objects and service classes.
- A class belongs in exactly one feature package. Shared code lives in `common`.

---

## 2. SOLID Principles

- **S – Single Responsibility**: Every class has one reason to change. A controller handles HTTP only; a service handles business logic only.
- **O – Open/Closed**: Extend behaviour via new strategy implementations, not by modifying existing classes.
- **L – Liskov Substitution**: Every subtype must be substitutable for its supertype. Avoid overriding methods in ways that weaken contracts.
- **I – Interface Segregation**: Define narrow interfaces. Never force a class to implement methods it does not use.
- **D – Dependency Inversion**: Depend on abstractions (interfaces), never on concrete classes in application code. Use constructor injection exclusively.

---

## 3. Method Length

- **Hard limit: 20 lines per method** (blank lines and closing braces count).
- If a method exceeds 20 lines, extract a private helper or a collaborator class.
- Long switch/case or complex if conditions must be replaced by a **Strategy** or **Command** pattern.

---

## 4. No If-Else Chains — Use Strategy Pattern

- Never write `if-else if-else` chains that branch on an enum, type, or string.
- Map enum values to `Strategy` implementations via a `Map<EnumType, Strategy>` populated in a `@Configuration` class or constructor.
- Example: `FaultType` to `FaultHandler` strategy map instead of `switch(faultType)`.

---

## 5. Comments & JavaDoc

- **Absolute zero `//` inline comments** — this includes section separators (`// ── foo ──`), explanatory notes, and trailing remarks. Code is made readable through naming and structure alone.
- Comments are permitted **only** as JavaDoc (`/** … */`) on `public` classes, interfaces, records, enums, and methods.
- **JavaDoc on every** `public` class, interface, record, enum, and method.
- JavaDoc must be updated whenever the code it documents changes.
- Use `@param`, `@return`, `@throws` tags on all non-trivial public methods.
- For TypeScript/TSDoc: the same rule applies — no `//` comments; document with `/** … */` JSDoc only.

---

## 6. Testing

- **Write tests when the class is created** — not after. No class may be merged without tests.
- Coverage is enforced by JaCoCo (`check` goal, bound to `verify` phase). The build **fails** if any minimum below is not met:

  | Counter     | Minimum | Target |
  | ----------- | ------- | ------ |
  | LINE        | 75%     | 90%    |
  | BRANCH      | 75%     | 90%    |
  | INSTRUCTION | 75%     | 90%    |
  | METHOD      | 75%     | 90%    |

- One test class per production class, in the matching package under `src/test/`.
- Use `@ParameterizedTest` with `@MethodSource` for boundary and edge cases.
- Mock all external collaborators with Mockito. Do not use `@SpringBootTest` for unit tests.
- Integration tests (using `@SpringBootTest`) live in a separate `it/` source set or are suffixed `IT`.
- Test method names follow: `methodName_givenCondition_expectedBehaviour()`.

---

## 7. Optimisation

- Prefer `ConcurrentHashMap` and `CopyOnWriteArrayList` for shared mutable state.
- Use `CompletableFuture` / Project Reactor for non-blocking I/O. Never block a reactive thread with `Thread.sleep` in production code.
- Avoid premature optimisation but always consider thread-safety on any field accessed from multiple threads.
- Use `final` on fields wherever possible.

---

## 8. Code Style

- All fields `private final` unless mutation is required.
- Use Java **records** for immutable data transfer objects.
- Use `Optional` instead of returning `null`.
- No raw types. No unchecked casts.
- Maximum class size: **200 lines** (excluding JavaDoc). Split larger classes.

---

## 9. Imports & Naming

- **No fully qualified class names** in code — always use a proper `import` statement.
- Every `import` must reference a type actually used in the file; remove all unused imports.
- Wildcard imports (`import foo.bar.*`) are forbidden.
- Static imports are allowed only for constants and test assertions (`Assertions.*`, `Mockito.*`).

---

## 10. Lombok

- Use **Lombok** to eliminate Java boilerplate. Specifically:
  - `@RequiredArgsConstructor` instead of a hand-written constructor that only assigns `final` fields.
  - `@Getter` / `@Setter` instead of hand-written accessors (unless the accessor contains logic).
  - `@Builder` for objects with many optional fields.
  - `@Slf4j` instead of declaring a `Logger` field manually.
  - `@Value` (Lombok) for fully immutable classes when a `record` is inappropriate.
- Do **not** use `@Data` on JPA entities or classes with mutable identity.
- Lombok must not be used to suppress required JavaDoc — annotated methods still need JavaDoc when public.

---

## 11. Controller → Service Delegation

- A `@RestController` must contain **zero business logic**.
- Controllers are allowed to:
  - Parse/validate the HTTP request.
  - Call exactly one service method.
  - Map the service result to an HTTP response.
- Any condition, calculation, or data transformation belongs in the `application/` service class.
- If you find yourself writing an `if` inside a controller body (not for HTTP status mapping), extract it to the service.

---

## 12. HTTPS

- All Spring Boot services must run on HTTPS (`server.ssl.enabled=true`).
- Never disable SSL verification in production code.
- Keystore configuration must come from environment variables or a mounted secret, never hardcoded.

---

## 13. Frontend Rules

### 13a. Feature-Steered Package Structure

- Organise the frontend by **feature**, mirroring the backend convention. Each feature is a self-contained directory under `src/features/`.
  ```
  src/
    features/
      transaction/
        api/         Pure fetch functions for this feature — no React, no state
        components/  React components — one file per component, co-locate test
        hooks/       Custom hooks — one hook per file
        model/       Constants and enum-like lookup maps — no functions, no React
        utils/       Pure utility functions — no React, no state
        types/       TypeScript interfaces and type aliases — one interface per file
        index.ts     Feature barrel — re-exports the feature's public API
      fault-injection/
        ...          (same sub-structure)
        index.ts
      scenario-benchmark/
        ...          (same sub-structure)
        index.ts
      node-monitoring/
        ...          (same sub-structure)
        index.ts
    common/
      components/    Components shared across features
      hooks/         Hooks shared across features
      types/         Types shared across features — one interface per file
      index.ts       Common barrel
    test/
      setup.ts       Global Vitest setup only
  ```
- Code that belongs exclusively to one feature lives inside that feature directory. Code used by two or more features moves to `common/`.
- Top-level flat directories (`src/components/`, `src/hooks/`, etc.) are forbidden for new code.

### 13b. Barrel Files and Path Aliases

- Every feature directory and `common/` must have an `index.ts` barrel that re-exports the symbols that are part of the feature's public API.
- Every **sub-directory** (`hooks/`, `model/`, `utils/`, `types/`, `components/`) inside each feature must also have its own `index.ts` barrel that re-exports all its public symbols.
- Barrel files re-export **only** what other features or the app shell need to consume. Internal cross-file imports within the same sub-directory use relative `./` paths.
- Configure three path aliases in both `tsconfig.json` (`paths`) and `vite.config.ts` (`resolve.alias`):
  - `@` → `src/` (legacy, keep for App-level files)
  - `@features` → `src/features`
  - `@common` → `src/common`
- **ALL imports that cross a sub-directory boundary must use these aliases — no `../` relative paths anywhere in the codebase.**
- **Import from the barrel, never from an individual sub-file.** Use `@common/types`, not `@common/types/ParticipantInfo`. Use `@common/api`, not `@common/api/coordinatorApi`.
- Cross-feature consumption uses the **feature barrel** (no sub-directory suffix):
  ```ts
  import { FaultInjectionPanel } from '@features/fault-injection';
  import { TransactionPanel } from '@features/transaction';
  import { NodeStatusBadge } from '@common';
  ```
- Within-feature cross-sub-directory imports use the **sub-directory barrel** (to avoid circular dependencies with the feature barrel):
  ```ts
  import { useTransactionSubmit } from '@features/transaction/hooks';
  import type { EventStyle } from '@features/transaction/types';
  import { EVENT_STYLES } from '@features/transaction/model';
  import { formatTime } from '@features/transaction/utils';
  import type { ParticipantInfo, SystemEvent } from '@common/types';
  import { fetchParticipants } from '@common/api';
  import { buildMetricRows } from '@common/utils';
  ```
- `../` (parent-directory) relative imports are **forbidden** everywhere. Only `./` (same-directory) relative imports are permitted.

### 13c. Interface and Type Files

- Every TypeScript `interface` and named `type` alias that is used in more than one file must live in its **own `.ts` file** inside the feature's `types/` directory (or `common/types/` if shared).
- File name equals the type name in PascalCase: `ParticipantInfo.ts`, `ScenarioResult.ts`.
- One type or interface per file. Closely related companion types (e.g. a type and its sub-type used together) may share a file if they are always imported together.
- **`model/` files must contain only constants and lookup maps — never `interface` or `type` declarations.**
- **`utils/` files must contain only functions — never `interface` or `type` declarations.**
- If a constant (e.g. `EVENT_STYLES`) depends on an interface (e.g. `EventStyle`), the interface lives in `types/EventStyle.ts` and is imported by the model file via `@features/{feature}/types`.
- Never define `interface` or `type` inline inside a component, hook, or utility file (except for trivially local `Props` interfaces that are never exported).

### 13d. Variable and Function Naming

- **No abbreviated names.** Every identifier must be fully spelled out so its meaning is unambiguous without context.
  - `percentage` not `pct`; `formatDelta` not `fmtDelta`; `abbreviateId` not `abbreviate`; `buildMetricRows` is fine because it is already descriptive.
  - Single-letter variables are forbidden except for loop indices (`i`, `j`) and well-known math variables.
  - Boolean variables and properties must start with `is`, `has`, `can`, or `should`: `isRunning`, `hasError`, `canSubmit`.
- Constants follow `UPPER_SNAKE_CASE`; functions and variables follow `camelCase`; React components and type names follow `PascalCase`.

### 13e. General Frontend Rules

- Each component, hook, utility function, and constant group lives in its **own file**. **One exported component, one exported hook, one exported function group, or one exported constant per file — never mix.**
- **Each component file must have exactly one `export default` component.** A second exported component (even a named export) is forbidden; extract it to its own file with its own test.
- **Constants and functions must not be defined inline inside component or hook files** unless trivially internal (used only once and not reusable):
  - Named constant objects, lookup maps, and enum-like values belong in `model/`.
  - Pure helper functions and transformations belong in `utils/`.
  - A single file in `model/` or `utils/` must export only one logical group.
- **Logic and presentation are separated**: extract data-fetching and state management into custom hooks; keep JSX lean.
- **No chains of `if/else if`** in render logic. Use a `Record<Key, ReactNode>` or strategy map instead.
- **Tests are mandatory** for every non-trivial component and every custom hook:
  - Use Vitest + React Testing Library.
  - Test files live beside the component/hook they test (`ComponentName.test.tsx`).
  - **One test file per production file** — do not test multiple components in a single test file.
  - Minimum 75% line coverage enforced by Vitest; target 90%.
  - Mock all API calls with `vi.fn()` or `msw`.
  - **`vi.mock` paths must match exactly the import path used in the source file** (e.g. `'@features/fault-injection/hooks'`, not `'../hooks/foo'`).
- **One export per file** — validated by the `lint:exports` script (`npm run lint:exports`) that runs as part of CI. Every violation must be fixed before merging:
  - `types/` file: only one exported `interface` or `type` alias.
  - `model/` file: only exported `const` or `enum` — no `interface`, `type`, `function`, or `class`.
  - `utils/` file: only exported `function` — no `interface`, `type`, `class`, or `const`/`enum`.
  - `components/` file: exactly one `export default` component.
  - Any file: never mix type/interface exports with value (function/const/class) exports.
- **TypeScript strict mode** — no `any`, no type assertions without a justification comment.
- Shared TypeScript types live exclusively in `types/` directories; never inline shared type definitions inside component files.

---

## 14. UI Language

- **All user-visible text in the frontend must be written in Polish.** This includes labels, button text, placeholders, headings, status messages, error messages, and tooltips.
- Technical identifiers that are part of the protocol (e.g. `COMMITTED`, `ABORTED`, `CRASH`, `YES`, `NO`) remain in English as they are domain constants, not display strings.
- Category labels, action labels, table headers, and all prose visible to the user must be Polish.

---

## 15. General

- No `System.out.println`. Use SLF4J `log.info/warn/error`.
- No `@SuppressWarnings` without a JavaDoc explanation of why it is safe.
- No `TODO` comments merged to `main` — create a GitHub issue instead.
- Update the `README.md` sections that describe changed behaviour.

---

## 16. Enum Constants — No `private static final` Value Fields

- **Never declare `private static final` primitive or String fields in a class.** Every named constant must live in a dedicated `enum` class instead.
- Each enum class encapsulates one logical group of constants (e.g. `FaultDefault`, `ResponseBodyKey`, `WebSocketTopic`, `NodeRole`, `ProtocolTimeout`).
- Enum constants with associated values carry a private `final` field and a public getter (`getValue()`, `getDuration()`, `getPath()`, etc.).
- Enum classes belong in the same sub-package as the concept they describe:
  - Domain constants → `domain/` (e.g. `ProtocolTimeout`, `WebSocketTopic`)
  - Shared constants used by multiple modules → `common/enums/` (e.g. `FaultDefault`, `NodeRole`)
  - API-layer response keys → `common/api/` (e.g. `ResponseBodyKey`)
- Every new enum class requires a corresponding unit-test class that verifies all constants and their values.
- The only exceptions are:
  - `private static final Logger` / `@Slf4j` fields (logging framework requirement)
  - `private static final SecureRandom` or other thread-safe singleton utilities that cannot be expressed as enum values
