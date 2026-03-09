# Odporność na błędy – DistributedTwoPhaseCommitSimulator

W tym dokumencie opisano, które elementy systemu są **redundantne** (czyli duplikowane w celu zapewnienia odporności na awarie), jak działa mechanizm wykrywania awarii koordynatora oraz w jaki sposób uczestnicy mogą zakończyć transakcję samodzielnie po jego utracie.

---

## 1. Co jest redundantne?

### 1.1. Warstwa danych – 6 niezależnych uczestników

System uruchamia **sześć niezależnych procesów uczestniczących** (participant-1 … participant-6), z których każdy:

- przechowuje własną kopię logu transakcji w pamięci (`InMemoryTransactionLog`),
- jest dostępny pod osobnym adresem sieciowym i portem (8444–8449),
- jest w stanie samodzielnie odpowiedzieć na pytanie o wynik dowolnej transakcji, którą przetwarzał.

Każdy uczestnik posiada tym samym **pełny, niezależny widok** swojego stanu transakcyjnego. Redundancja tych węzłów jest kluczowa dla mechanizmu termination protocol (zob. sekcja 3).

### 1.2. Log transakcji

Przed wysłaniem głosu (`YES`/`NO`) każdy uczestnik zapisuje decyzję do swojego logu (`LogEntry`). Nawet jeśli uczestnik zostanie następnie wyłączony i uruchomiony ponownie, jego log zachowuje status transakcji:

| Status      | Znaczenie                                           |
| ----------- | --------------------------------------------------- |
| `PREPARING` | Otrzymano PREPARE; głos oddany; oczekuję na decyzję |
| `VOTED_YES` | Zagłosowałem za zatwierdzeniem                      |
| `VOTED_NO`  | Zagłosowałem za anulowaniem                         |
| `COMMITTED` | Transakcja zatwierdzona                             |
| `ABORTED`   | Transakcja anulowana                                |
| `UNCERTAIN` | Wynik nadal nieznany (trwa election)                |

### 1.3. Konsultacja rówieśnicza (peer consultation)

Każdy uczestnik zna adresy wszystkich pozostałych uczestników (konfiguracja `participant.peers`). Jeśli koordynator jest nieosiągalny, uczestnik może odpytać pozostałych rówieśników w poszukiwaniu rozstrzygającego wpisu:

- Jeśli którykolwiek rówieśnik ma status `COMMITTED` lub `ABORTED` → uczestnik przyjmuje tę samą decyzję.
- Jeśli wszyscy rówieśnicy są w stanie `PREPARING`/`UNCERTAIN` → wynik pozostaje `UNCERTAIN`; election jest powtarzana.

Mechanizm ten implementuje **kooperatywny protokół terminacji** (cooperative termination protocol), który jest odporny na awarię koordynatora, ale nie na jednoczesną awarię wszystkich uczestników.

---

## 2. Co NIE jest redundantne?

### 2.1. Koordynator

W obecnej konfiguracji istnieje **jeden koordynator** (port 8443). Pełni on rolę centralnego punktu sterowania protokołem 2PC:

- przydziela identyfikatory transakcji,
- rozsyła PREPARE, COMMIT i ABORT do uczestników,
- utrzymuje połączenia WebSocket do interfejsu użytkownika.

Awaria koordynatora **po** wysłaniu PREPARE, ale **przed** wysłaniem decyzji (COMMIT/ABORT) prowadzi do stanu niepewności uczestników. System radzi sobie z tym scenariuszem przez mechanizmy opisane w sekcji 3 i 4.

Awaria koordynatora **przed** wysłaniem PREPARE oznacza, że transakcja nigdy się nie rozpoczęła — uczestnicy nie mają żadnego wpisu w logu i nie blokują żadnych zasobów.

---

## 3. Jak działa wykrywanie awarii koordynatora?

System korzysta z dwóch uzupełniających się mechanizmów:

### 3.1. Pasywny timeout (UncertainTransactionRecoveryScheduler)

Co `participant.recovery.interval-ms` milisekund (domyślnie 5000 ms) każdy uczestnik skanuje swój log w poszukiwaniu wpisów w stanie `PREPARING`, których wiek przekracza `participant.recovery.timeout-ms` (domyślnie 5000 ms). Dla każdego takiego wpisu uruchamiany jest protokół elekcji (`ElectionService.elect`).

**Wada**: w najgorszym przypadku wykrycie awarii zajmuje `timeout-ms + interval-ms` ≈ 10 sekund.

### 3.2. Aktywny heartbeat (CoordinatorHeartbeatMonitor) ← nowy

Co `participant.heartbeat.interval-ms` milisekund (domyślnie 3000 ms) każdy uczestnik wysyła żądanie `GET /api/coordinator/status` do koordynatora. Przy pierwszym niepowodzeniu:

1. Flaga `coordinatorReachable` jest ustawiana na `false`.
2. Natychmiast wywoływane jest `UncertainTransactionRecoveryScheduler.recoverAllPreparingNow()`, które odpytuje rówieśników dla **wszystkich** wpisów `PREPARING` (bez filtru czasowego).
3. Kolejne nieudane próby są ignorowane (flaga jest już ustawiona), aby nie zalać rówieśników zbędnymi żądaniami.
4. Gdy koordynator wróci do sieci (odpowiedź HTTP OK), flaga zostaje zresetowana.

**Korzyść**: czas wykrycia awarii skraca się do ≤ 3 sekund (jeden interwał heartbeat) zamiast ≈ 10 sekund.

```
Uczestnik                                    Koordynator
    │                                             │
    │──GET /api/coordinator/status ──────────────►│  (co 3 s)
    │◄──── 200 OK ────────────────────────────────│
    │                                             │
    │──GET /api/coordinator/status ──────────────►│  (koordynator nie odpowiada)
    │◄──── TIMEOUT / ConnectRefused ──────────────│
    │  coordinatorReachable = false               │
    │  recoverAllPreparingNow()                   │
    │      │                                      │
    │      ├──GET /api/peers/log/{txId} ──────────►participant-2
    │      ├──GET /api/peers/log/{txId} ──────────►participant-3
    │      └── ...                                │
    │  [wynik znany] → updatePhase(txId, COMMITTED/ABORTED)
```

---

## 4. Protokół elekcji krok po kroku

Każdorazowo gdy `ElectionService.elect(txId)` jest wywołany:

1. **Sprawdź lokalny log**: jeśli jest `COMMITTED` lub `ABORTED` → przerwij i zwróć tę wartość.
2. **Zapytaj rówieśników**: wyślij `GET /api/peers/log/{txId}` do wszystkich znanych uczestników równolegle (`Flux.fromIterable(...)`).
3. **Analiza odpowiedzi**:
   - Każda odpowiedź `COMMITTED` → cały wynik = `COMMITTED`.
   - Każda odpowiedź `ABORTED` → cały wynik = `ABORTED`.
   - Jeśli wszyscy odpowiedzieli `PREPARING`/`UNCERTAIN`/brak odpowiedzi → wynik = `UNCERTAIN`.
4. Jeśli wynik jest `COMMITTED` lub `ABORTED`, uczestnik aktualizuje swój log i może bezpiecznie odpowiedzieć na żądania.
5. Jeśli wynik jest `UNCERTAIN`, wpis pozostaje w stanie `PREPARING` i elekcja zostanie powtórzona przy następnym ticku schedulera.

> **Warunek działania**: protokół elekcji rozstrzyga transakcję tylko wtedy, gdy **przynajmniej jeden** uczestnik otrzymał już decyzję COMMIT lub ABORT od koordynatora. Jeśli awaria nastąpiła przed wysłaniem jakiejkolwiek decyzji, wszyscy uczestnicy pozostają w stanie `UNCERTAIN` aż do przywrócenia koordynatora.

---

## 5. Flaga redundancyEnabled

W interfejsie użytkownika dostępny jest przełącznik **„Redundancja"** (zarządzany przez `SimulationModeService`). Kontroluje on, czy `ElectionService.elect` faktycznie konsultuje rówieśników:

| Wartość | Zachowanie                                                                                     |
| ------- | ---------------------------------------------------------------------------------------------- |
| `true`  | Protokół elekcji działa normalnie (peer consultation aktywna). **Domyślnie.**                  |
| `false` | `elect()` natychmiast zwraca `UNCERTAIN` bez pytania rówieśników — symulacja braku redundancji |

Flaga ta jest narzędziem **edukacyjnym**: pozwala zademonstrować, że bez peer consultation transakcje nie zostaną nigdy rozstrzygnięte po awarii koordynatora.

### 5.1. redundancyEnabled = true (domyślnie)

Uczestnik, który utknął w stanie `PREPARING`, może się z tego stanu wydostać:

1. `CoordinatorHeartbeatMonitor` wykrywa awarię koordynatora w ciągu ≤ 3 s.
2. `recoverAllPreparingNow()` wywołuje `ElectionService.elect(txId)` dla każdego wpisu `PREPARING`.
3. `ElectionService` odpytuje wszystkich znanych rówieśników (`GET /api/peers/log/{txId}`).
4. Jeśli którykolwiek rówieśnik zna wynik (`COMMITTED`/`ABORTED`), uczestnik przyjmuje tę decyzję i aktualizuje swój log.
5. Transakcja zostaje rozstrzygnięta — zasoby są zwolnione, a UI otrzymuje aktualizację stanu.

```
Uczestnik A (PREPARING)          Uczestnik B (COMMITTED)
        │                                 │
        │──GET /api/peers/log/{txId} ────►│
        │◄── { phase: COMMITTED } ────────│
        │  updatePhase(txId, COMMITTED)   │
        │  transakcja rozstrzygnięta ✓    │
```

**Skutek**: system jest odporny na awarię koordynatora. Transakcje są zawsze finalnie rozstrzygane, o ile przynajmniej jeden uczestnik zdążył otrzymać decyzję przed awarią.

### 5.2. redundancyEnabled = false

`ElectionService.elect()` natychmiast zwraca `UNCERTAIN` bez wykonania żadnego zapytania do rówieśników:

1. `CoordinatorHeartbeatMonitor` nadal wykrywa awarię i wywołuje `recoverAllPreparingNow()`.
2. `ElectionService.elect(txId)` zwraca `UNCERTAIN` — bez jakichkolwiek zapytań do rówieśników.
3. `UncertainTransactionRecoveryScheduler` nie aktualizuje logu (wynik `UNCERTAIN` jest ignorowany).
4. Przy kolejnym ticku schedulera (co 5 s) historia się powtarza — w nieskończoność.
5. Transakcja pozostaje w stanie `PREPARING` na zawsze, dopóki koordynator nie wróci do sieci.

```
Uczestnik A (PREPARING)
        │
        │  elect(txId) → UNCERTAIN  (bez zapytań do rówieśników)
        │  log pozostaje PREPARING
        │  ... (retry co 5 s, wynik zawsze UNCERTAIN)
        │  transakcja nigdy nie zostaje rozstrzygnięta ✗
```

**Skutek**: klasyczny problem blokowania 2PC — awaria koordynatora powoduje permanentne zablokowanie uczestników. Jest to celowa demonstracja tego, dlaczego redundancja (peer consultation) jest niezbędna.

---

## 6. Podsumowanie odporności

| Scenariusz awarii                             | Zachowanie systemu                                                 |
| --------------------------------------------- | ------------------------------------------------------------------ |
| Awaria uczestnika przed głosowaniem           | Koordynator traktuje brak odpowiedzi jako NO → ABORT               |
| Awaria uczestnika po głosowaniu YES           | Pozostałe kopie logu wystarczą do elekcji                          |
| Awaria koordynatora przed PREPARE             | Transakcja nie zaczęta; brak blokad                                |
| Awaria koordynatora po PREPARE, przed decyzją | Heartbeat wykrywa w ≤ 3 s; elekcja między uczestnikami             |
| Awaria koordynatora po wysłaniu decyzji       | Przynajmniej jeden uczestnik zna wynik; elekcja go odtworzy        |
| Awaria wszystkich uczestników jednocześnie    | Dane transakcji są tracone (log jest in-memory, brak persystencji) |
| Symulacja awarii (CrashFault / NetworkFault)  | Wstrzykiwane przez UI; heartbeat i scheduler działają tak samo     |
