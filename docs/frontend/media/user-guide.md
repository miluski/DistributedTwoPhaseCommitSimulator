# Przewodnik użytkownika — DistributedTwoPhaseCommitSimulator

Ten dokument opisuje:

1. Jak uruchomić symulator.
2. Jak korzystać z każdego panelu interfejsu graficznego.
3. Jak interpretować wyniki testów benchmarkowych i scenariuszy.
4. Jak działa mechanizm odporności na błędy (redundancja).
5. Jak wyeksportować wyniki do formatu tekstowego.

---

## 1. Uruchomienie symylatora

### 1.1. Docker Compose (zalecane)

```bash
docker compose up --build
```

Po uruchomieniu usługi są dostępne pod adresami:

| Usługa      | Adres                  |
| ----------- | ---------------------- |
| Frontend    | https://localhost:3000 |
| Koordynator | https://localhost:8443 |
| Uczestnik 1 | https://localhost:8444 |
| Uczestnik 2 | https://localhost:8445 |
| Uczestnik 3 | https://localhost:8446 |
| Uczestnik 4 | https://localhost:8447 |
| Uczestnik 5 | https://localhost:8448 |
| Uczestnik 6 | https://localhost:8449 |

> Wszystkie usługi działają przez HTTPS z certyfikatem samopodpisanym.
> Przy pierwszym otwarciu przeglądarka może wyświetlić ostrzeżenie — kliknij „Zaawansowane" → „Przejdź do localhost".

### 1.2. Tryb deweloperski (frontend osobno)

```bash
cd frontend
npm install
npm run dev
```

Frontend nasłuchuje na https://localhost:5173.
Backend musi być uruchomiony oddzielnie lub przez Docker.

---

## 2. Przegląd interfejsu

Strona główna składa się z następujących sekcji (od góry do dołu):

```
┌─────────────────────────────────────────────────────────┐
│  MetricsBar — statystyki + przycisk Redundancja         │
├─────────────────────────────────────────────────────────┤
│  Siatka węzłów: CoordinatorCard + 6 × NodeCard         │
├─────────────────────────────────────────────────────────┤
│  TransactionTimeline: Faza 1 | Faza 2                   │
├─────────────────────────────────────────────────────────┤
│  TransactionPanel                                        │
├─────────────────────────────────────────────────────────┤
│  FaultInjectionPanel                                     │
├─────────────────────────────────────────────────────────┤
│  EventLog                                                │
├─────────────────────────────────────────────────────────┤
│  VoteMatrix                                              │
├─────────────────────────────────────────────────────────┤
│  ScenarioPanel                                           │
└─────────────────────────────────────────────────────────┘
```

---

## 3. MetricsBar — pasek statystyk

Poziomy pasek u góry strony wyświetla łączne statystyki wszystkich transakcji od momentu uruchomienia systemu.

| Pole         | Kolor     | Znaczenie                                               |
| ------------ | --------- | ------------------------------------------------------- |
| Łącznie      | szary     | Całkowita liczba zainicjowanych transakcji              |
| Zatwierdzone | zielony   | Transakcje zakończone sukcesem (COMMITTED), z procentem |
| Przerwane    | czerwony  | Transakcje przerwane (ABORTED), z procentem             |
| Niepewne     | żółty     | Transakcje w stanie UNCERTAIN (oczekujące na elekcję)   |
| W toku       | niebieski | Transakcje aktualnie przetwarzane                       |
| Śr. decyzja  | szary     | Średni czas decyzji koordynatora w milisekundach        |

### 3.1. Przycisk Redundancja

W prawym rogu paska znajduje się przycisk trybu redundancji:

- **Redundancja WŁ** (zielony) — protokół elekcji rówieśniczej jest aktywny. Uczestnicy mogą samodzielnie rozwiązać niepewne transakcje po awarii koordynatora.
- **Redundancja WYŁ** (czerwony) — protokół elekcji jest wyłączony. Niepewne transakcje pozostają zablokowane, dopóki koordynator nie wróci do sieci.

Kliknięcie przycisku zmienia tryb dla wszystkich węzłów jednocześnie i jest używane w scenariuszach benchmarkowych kategorii `redundancja`.

---

## 4. Siatka węzłów

### 4.1. CoordinatorCard — karta koordynatora

Wyświetla bieżący stan koordynatora:

- Zielona kropka przy nazwie — koordynator działa normalnie.
- Czerwona kropka / komunikat o błędzie — aktywna awaria (CRASH lub opóźnienie) lub brak odpowiedzi.
- `port: 8443` — port, pod którym koordynator jest dostępny z zewnątrz.

Koordynator jest **jedynym centralnym punktem sterowania** protokołem 2PC: przydziela identyfikatory transakcji, rozsyła PREPARE, COMMIT i ABORT oraz utrzymuje połączenie WebSocket do frontendu.

### 4.2. NodeCard — karty uczestników

Sześć kart (participant-1 … participant-6) wyświetla stan każdego węzła:

- Nazwa węzła i port (8444–8449).
- Aktywne błędy wstrzyknięte przez panel Iniekcji błędów (np. `CRASH`, `NETWORK_DELAY 800ms`).
- Status osiągalności (wysyłany przez heartbeat koordynatora).

---

## 5. TransactionTimeline — oś czasu protokołu

Oś czasu dzieli zdarzenia protokołu 2PC na dwie kolumny wyświetlane obok siebie.

### 5.1. Faza 1 — Przygotowanie

Zdarzenia w tej kolumnie:

| Typ zdarzenia         | Znaczenie                                             |
| --------------------- | ----------------------------------------------------- |
| `TRANSACTION_STARTED` | Koordynator zarejestrował nową transakcję             |
| `PREPARE_SENT`        | Koordynator rozesłał komunikat PREPARE do uczestników |
| `VOTE_RECEIVED`       | Koordynator otrzymał głos TAK lub NIE od uczestnika   |
| `ALL_VOTES_COLLECTED` | Zebranie wszystkich głosów zakończone                 |

### 5.2. Faza 2 — Decyzja

Zdarzenia w tej kolumnie:

| Typ zdarzenia           | Znaczenie                                              |
| ----------------------- | ------------------------------------------------------ |
| `COMMIT_SENT`           | Rozsłanie decyzji COMMIT do wszystkich uczestników     |
| `ABORT_SENT`            | Rozsłanie decyzji ABORT do wszystkich uczestników      |
| `TRANSACTION_COMMITTED` | Koordynator zatwierdził transakcję                     |
| `TRANSACTION_ABORTED`   | Koordynator przerwał transakcję                        |
| Inne                    | Zdarzenia odtwarzania / heartbeat / status uczestników |

Gdy nie ma aktywnej transakcji, obie kolumny wyświetlają tekst „Brak zdarzeń w tej fazie."

### 5.3. Wybór transakcji

Na osi czasu można kliknąć identyfikator transakcji z panelu **Zainicjuj transakcję** lub z **Macierzy głosów**, aby zobaczyć zdarzenia wyłącznie dla wybranej transakcji. Panel `TransactionPanel` wysyła identyfikator do osi czasu przy każdej nowej transakcji.

---

## 6. TransactionPanel — inicjowanie transakcji

Panel pozwala ręcznie zainicjować transakcję w systemie.

### 6.1. Jak zainicjować transakcję

1. Kliknij przycisk **Zatwierdź transakcję**.
2. Pole wartości jest generowane automatycznie (losowy ciąg znaków lub wartość testowa).
3. Po zakończeniu protokołu panel wyświetla wynik:
   - `COMMITTED` (zielony) — transakcja zatwierdzona.
   - `ABORTED` (czerwony) — transakcja przerwana (co najmniej jeden głos NIE lub awaria).
   - `UNCERTAIN` / inny — transakcja w toku lub nierozstrzygnięta.
4. Identyfikator transakcji jest automatycznie przesyłany do osi czasu.

### 6.2. Przykładowe przepływy

**Normalny przebieg:**

```
Koordynator → PREPARE → wszyscy głosują TAK → COMMIT → COMMITTED
```

**Przerwanie przez uczestnika:**

```
Koordynator → PREPARE → jeden uczestnik głosuje NIE → ABORT → ABORTED
```

**Awaria koordynatora po PREPARE:**

```
Koordynator → PREPARE → koordynator ulega awarii
→ heartbeat uczestnicy wykrywają awarię w ≤ 3 s
→ elekcja rówieśnicza → stan rozwiązany
```

---

## 7. FaultInjectionPanel — iniekcja błędów

Panel służy do wstrzykiwania błędów w wybrane węzły w celu obserwacji zachowania systemu.

### 7.1. Jak wstrzyknąć błąd

1. Wybierz **węzeł docelowy** (Koordynator lub jeden z uczestników participant-1…6).
2. Wybierz **typ błędu** z listy.
3. Jeśli wybrany typ to `Opóźnienie sieci` — uzupełnij pole **Opóźnienie (ms)**.
4. Kliknij **Wstrzyknij błąd**, aby błąd stał się aktywny.
5. Kliknij **Wyczyść błędy**, aby usunąć wszystkie aktywne błędy z wybranego węzła.

### 7.2. Typy błędów dla uczestników

| Typ błędu          | Etykieta polska    | Działanie                                                                                        |
| ------------------ | ------------------ | ------------------------------------------------------------------------------------------------ |
| `CRASH`            | Awaria węzła       | Uczestnik odrzuca wszystkie przychodzące żądania (symulacja całkowitej awarii węzła).            |
| `NETWORK_DELAY`    | Opóźnienie sieci   | Każda odpowiedź uczestnika jest opóźniana o podaną liczbę milisekund.                            |
| `FORCE_ABORT_VOTE` | Wymuszony głos NIE | Uczestnik zawsze głosuje NIE w Fazie 1, niezależnie od wartości transakcji.                      |
| `MESSAGE_LOSS`     | Utrata wiadomości  | Uczestnik gubi przychodzące wiadomości z określonym prawdopodobieństwem lub liczebnością.        |
| `TRANSIENT`        | Błąd chwilowy      | Uczestnik jest niedostępny przez podany czas (ms), po którym automatycznie przywraca dostępność. |
| `INTERMITTENT`     | Błąd przerywany    | Uczestnik odrzuca określony procent wiadomości losowo (parametr `chancePercent`).                |

### 7.3. Typy błędów dla koordynatora

Koordynator obsługuje podzbiór typów błędów:

| Typ błędu       | Działanie                                                                                                                                   |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `CRASH`         | Koordynator odrzuca wszystkie żądania (zwraca HTTP 503).                                                                                    |
| `NETWORK_DELAY` | Odpowiedzi koordynatora są opóźniane.                                                                                                       |
| `MESSAGE_LOSS`  | Koordynator wysyła decyzję Fazy 2 tylko do wskazanej liczby uczestników (parametr `count`). `count=0` oznacza, że nikt nie dostaje decyzji. |

> Iniekcja błędu na koordynatorze z typem `MESSAGE_LOSS` i `count=0` symuluje najgroźniejszy scenariusz w protokole 2PC: koordynator zna wynik, ale uczestnicy go nie dostają. Zaobserwuj, czy tryb redundancji pozwala im się odtworzyć.

---

## 8. EventLog — dziennik zdarzeń

Dziennik zdarzeń na bieżąco wyświetla wszystkie zdarzenia systemowe odebrane przez WebSocket.

- Zdarzenia są posortowane chronologicznie (najnowsze na dole).
- Każdy wpis zawiera: sygnaturę czasową, typ zdarzenia oraz identyfikator transakcji lub węzła.
- Dziennik nadaje się do śledzenia kolejności zdarzeń podczas ręcznych eksperymentów.

Dziennik wyświetla **wszystkie** zdarzenia z systemu. Oś czasu (sekcja 5) filtruje zdarzenia według wybranej transakcji.

---

## 9. VoteMatrix — macierz głosów

Macierz głosów prezentuje głosy uczestników dla ostatnich do 8 transakcji.

### 9.1. Układ tabeli

- **Wiersze** — uczestnicy (participant-1 … participant-6), posortowani alfabetycznie.
- **Kolumny** — ostatnie transakcje, najnowsza po prawej.

Każda kolumna zawiera trzy wiersze nagłówka:

1. **Wartość transakcji** (`tx.value`) — ciąg znaków przekazany przy inicjowaniu.
2. **Skrócony identyfikator transakcji** — pierwsze i ostatnie znaki UUID.
3. **Status** — `COMMITTED` (zielony), `ABORTED` (czerwony) lub inny (żółty).

### 9.2. Zawartość komórek

| Zawartość komórki | Znaczenie                                    |
| ----------------- | -------------------------------------------- |
| `TAK`             | Uczestnik zagłosował za zatwierdzeniem (YES) |
| `NIE`             | Uczestnik zagłosował za przerwaniem (NO)     |
| `—`               | Brak głosu (uczestnik nie wziął udziału)     |

---

## 10. ScenarioPanel — testy odporności na błędy

Panel scenariuszy pozwala uruchamiać gotowe testy odporności na błędy i porównywać wyniki.

### 10.1. Jak uruchomić pojedynczy scenariusz

1. Wybierz **kategorię** ze zakładek (Linia bazowa, Pojedyncza awaria, Złożona awaria, Ekstremalna, Redundancja).
2. Wybierz **scenariusz** z listy rozwijanej.
3. Przeczytaj opis i oczekiwane zachowanie wyświetlone poniżej.
4. Kliknij **Uruchom scenariusz**.
5. Po zakończeniu — przeczytaj wynik w sekcji **Raport**.

### 10.2. Jak uruchomić całą serię

1. Kliknij **Uruchom wszystkie scenariusze** (przycisk poniżej zakładek kategorii).
2. Pasek postępu pokazuje aktualnie uruchamiany scenariusz.
3. Po zakończeniu pojawia się **Raport serii** z tabelą wszystkich wyników.

### 10.3. Kopiowanie raportu

- Każdy pojedynczy raport ma przycisk **Kopiuj raport** — kopiuje wynik jako sformatowany tekst do schowka.
- Raport serii ma przycisk **Kopiuj raport** — kopiuje zbiorczą tabelę wszystkich wyników.

Skopiowany tekst można wkleić do dowolnego edytora tekstowego (Notepad, VS Code, Word) lub zapisać jako plik `.txt`.

---

## 11. Interpretacja wyników scenariuszy

### 11.1. Werdykty

| Werdykt    | Ikona          | Znaczenie                                                                                         |
| ---------- | -------------- | ------------------------------------------------------------------------------------------------- |
| `PASS`     | ✓ Zaliczone    | Zachowanie systemu jest zgodne z oczekiwaniami protokołu 2PC.                                     |
| `FAIL`     | ✗ Niezaliczone | System nie zachował się zgodnie z oczekiwaniami — wynik wskazuje na błąd lub nieoczekiwany stan.  |
| `DEGRADED` | ⚠ Częściowo    | System zachował się poprawnie, lecz osiągnął cel częściowo lub alternatywną ścieżką (patrz opis). |

> Werdykt `DEGRADED` nie oznacza błędu. Kilka scenariuszy jest **celowo zaprojektowanych** tak, aby kończyć się wynikiem `DEGRADED` — bo właśnie taka jest prawidłowa odpowiedź systemu (np. scenariusz z milczącym koordynatorem).

### 11.2. Tabela metryk delta

Każdy raport scenariusza zawiera tabelę ze zmianą metryk przed i po teście:

| Kolumna | Znaczenie                                    |
| ------- | -------------------------------------------- |
| Metryka | Nazwa licznika (np. Zatwierdzone, Przerwane) |
| Przed   | Wartość metryki przed uruchomieniem testu    |
| Po      | Wartość metryki po zakończeniu testu         |
| Δ       | Różnica: Po − Przed (ze znakiem + lub −)     |

Kolumna Δ jest pokolorowana:

- Zielony `+1` przy Zatwierdzone → transakcja zatwierdzona zgodnie z oczekiwaniami.
- Czerwony `+1` przy Przerwane → transakcja przerwana.
- Żółty / szary → zmiana neutralna lub metryka czasu.

### 11.3. Log kroków (Steps)

Sekcja kroków (lista numerowana poniżej podsumowania) prezentuje dokładną sekwencję działań wykonanych przez scenariusz: które błędy wstrzyknięto, jaka był wynik transakcji, kiedy błędy zostały wyczyszczone. Służy do debugowania nieoczekiwanych wyników.

---

## 12. Katalog scenariuszy

### 12.1. Linia bazowa (`baseline`)

Scenariusze weryfikujące poprawność systemu bez wstrzykiwania błędów.

| ID                      | Nazwa                                     | Oczekiwany wynik                                |
| ----------------------- | ----------------------------------------- | ----------------------------------------------- |
| `baseline-happy-path`   | Ścieżka idealna — wszystkie węzły sprawne | PASS — transakcja zatwierdzona, +1 Zatwierdzone |
| `baseline-forced-abort` | Jednogłośny głos NIE                      | PASS — transakcja przerwana, +1 Przerwane       |

### 12.2. Pojedyncza awaria (`single-fault`)

Wpływ jednej usterki na poprawność protokołu.

| ID                          | Nazwa                                  | Oczekiwany wynik                                                                    |
| --------------------------- | -------------------------------------- | ----------------------------------------------------------------------------------- |
| `single-crash-participant`  | Awaria jednego uczestnika              | PASS — brak głosu TAK skutkuje ABORT; izolacja awarii działa                        |
| `single-network-delay`      | Opóźnienie sieciowe jednego uczestnika | PASS — transakcja zatwierdzona, avgDecisionMs rośnie                                |
| `single-force-abort-vote`   | Pojedynczy wymuszony głos przerwania   | PASS — jeden głos NIE wystarczy do ABORT                                            |
| `single-transient-fault`    | Przejściowa awaria uczestnika (1,5 s)  | PASS — system toleruje krótkie przerwy; wynik COMMITTED lub ABORTED                 |
| `single-intermittent-fault` | Przerywany błąd (80% wskaźnik utraty)  | PASS lub DEGRADED — probabilistyczny; DEGRADED, jeśli transakcja przeszła           |
| `single-partial-send`       | Częściowe wysyłanie Fazy 2 (1 z N)     | DEGRADED — koordynator COMMITTED; pozostali uczestnicy w PREPARING (normalny wynik) |

### 12.3. Złożona awaria (`compound`)

Kombinacje wielu jednoczesnych usterek.

| ID                                             | Nazwa                                        | Oczekiwany wynik                                      |
| ---------------------------------------------- | -------------------------------------------- | ----------------------------------------------------- |
| `compound-multi-crash`                         | Awaria połowy uczestników                    | PASS — wiele głosów NIE obsłużonych, brak split-brain |
| `compound-crash-delay-mix`                     | Awaria jednego + opóźnienie pozostałych      | PASS — złożona awaria rozwiązana, transakcja ABORTED  |
| `compound-coordinator-delay-participant-crash` | Opóźnienie koordynatora + awaria uczestnika  | PASS — dwuwarstwowa awaria; ABORT po opóźnieniu       |
| `compound-all-delays`                          | Wszyscy uczestnicy opóźnieni (600 ms)        | PASS — zatwierdzone, avgDecisionMs znacznie wyższe    |
| `compound-intermittent-force-abort`            | Przerywany + wymuszony błąd na dwóch węzłach | PASS — co najmniej jeden decydujący głos NIE          |

### 12.4. Skrajne przypadki (`extreme`)

Najgorsze możliwe scenariusze awarii.

| ID                                  | Nazwa                                                 | Oczekiwany wynik                                                                                       |
| ----------------------------------- | ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| `extreme-all-crash`                 | Całkowita awaria uczestników                          | PASS — koordynator ABORTED; brak split-brain                                                           |
| `extreme-coordinator-crash`         | Awaria koordynatora (test SPOF)                       | PASS — żądania odrzucone (503), koordynator odtwarza się i zatwierdza po wyczyszczeniu                 |
| `extreme-coordinator-silent-phase2` | Koordynator milczący po Fazie 1 (najgorszy przypadek) | DEGRADED — celowy wynik; koordynator COMMITTED, uczestnicy w PREPARING; harmonogram odtwarza po ≤ 10 s |

### 12.5. Redundancja (`redundancy`)

Bezpośrednie porównanie zachowania systemu z włączoną i wyłączoną redundancją.

| ID                            | Nazwa                                                     | Oczekiwany wynik                                                            |
| ----------------------------- | --------------------------------------------------------- | --------------------------------------------------------------------------- |
| `redundancy-off-partial-send` | Redundancja WYŁĄCZONA — Faza 2 milcząca                   | DEGRADED — celowy w ynik; uczestnicy utknięci w PREPARING na zawsze         |
| `redundancy-on-partial-send`  | Redundancja WŁĄCZONA — Faza 2 milcząca (auto-odtwarzanie) | PASS — harmonogram odtwarzania wykrywa PREPARING i rozwiązuje przez elekcję |

---

## 13. Odporność na błędy — mechanizm redundancji

> Poniższa sekcja pochodzi z dokumentu technicznego `REDUNDANCY.md` i opisuje wewnętrzne działanie mechanizmów odporności.

### 13.1. Co jest redundantne?

#### Warstwa danych — 6 niezależnych uczestników

System uruchamia **sześć niezależnych procesów uczestniczących** (participant-1 … participant-6), z których każdy:

- przechowuje własną kopię logu transakcji w pamięci (`InMemoryTransactionLog`),
- jest dostępny pod osobnym adresem sieciowym i portem (8444–8449),
- jest w stanie samodzielnie odpowiedzieć na pytanie o wynik dowolnej transakcji, którą przetwarzał.

Każdy uczestnik posiada **pełny, niezależny widok** swojego stanu transakcyjnego. Redundancja tych węzłów jest kluczowa dla protokołu terminacji (sekcja 13.4).

#### Log transakcji

Przed wysłaniem głosu (`YES`/`NO`) każdy uczestnik zapisuje decyzję do swojego logu (`LogEntry`). Nawet jeśli uczestnik zostanie wyłączony i uruchomiony ponownie, jego log zachowuje status transakcji:

| Status      | Znaczenie                                           |
| ----------- | --------------------------------------------------- |
| `PREPARING` | Otrzymano PREPARE; głos oddany; oczekuję na decyzję |
| `VOTED_YES` | Zagłosowałem za zatwierdzeniem                      |
| `VOTED_NO`  | Zagłosowałem za anulowaniem                         |
| `COMMITTED` | Transakcja zatwierdzona                             |
| `ABORTED`   | Transakcja anulowana                                |
| `UNCERTAIN` | Wynik nadal nieznany (trwa election)                |

#### Konsultacja rówieśnicza (peer consultation)

Każdy uczestnik zna adresy wszystkich pozostałych uczestników (konfiguracja `participant.peers`). Jeśli koordynator jest nieosiągalny, uczestnik może odpytać pozostałych rówieśników w poszukiwaniu rozstrzygającego wpisu:

- Jeśli którykolwiek rówieśnik ma status `COMMITTED` lub `ABORTED` → uczestnik przyjmuje tę samą decyzję.
- Jeśli wszyscy rówieśnicy są w stanie `PREPARING`/`UNCERTAIN` → wynik pozostaje `UNCERTAIN`; elekcja jest powtarzana.

Mechanizm ten implementuje **kooperatywny protokół terminacji** (cooperative termination protocol), który jest odporny na awarię koordynatora, ale nie na jednoczesną awarię wszystkich uczestników.

---

### 13.2. Co NIE jest redundantne?

W obecnej konfiguracji istnieje **jeden koordynator** (port 8443). Pełni on rolę centralnego punktu sterowania protokołem 2PC. Awaria koordynatora po wysłaniu PREPARE, ale przed wysłaniem decyzji (COMMIT/ABORT) prowadzi do stanu niepewności uczestników. System radzi sobie z tym scenariuszem przez mechanizmy opisane poniżej.

---

### 13.3. Wykrywanie awarii koordynatora

System korzysta z dwóch uzupełniających się mechanizmów:

#### Pasywny timeout (`UncertainTransactionRecoveryScheduler`)

Co `participant.recovery.interval-ms` milisekund (domyślnie 5000 ms) każdy uczestnik skanuje swój log w poszukiwaniu wpisów w stanie `PREPARING`, których wiek przekracza `participant.recovery.timeout-ms` (domyślnie 5000 ms). Dla każdego takiego wpisu uruchamiany jest protokół elekcji (`ElectionService.elect`).

**Czas wykrycia**: w najgorszym przypadku `timeout-ms + interval-ms` ≈ 10 sekund.

#### Aktywny heartbeat (`CoordinatorHeartbeatMonitor`)

Co `participant.heartbeat.interval-ms` milisekund (domyślnie 3000 ms) każdy uczestnik wysyła żądanie `GET /api/coordinator/status` do koordynatora. Przy pierwszym niepowodzeniu:

1. Flaga `coordinatorReachable` jest ustawiana na `false`.
2. Natychmiast wywoływane jest `UncertainTransactionRecoveryScheduler.recoverAllPreparingNow()`, które odpytuje rówieśników dla **wszystkich** wpisów `PREPARING` (bez filtru czasowego).
3. Kolejne nieudane próby są ignorowane, aby nie zalać rówieśników zbędnymi żądaniami.
4. Gdy koordynator wróci do sieci (odpowiedź HTTP OK), flaga zostaje zresetowana.

**Czas wykrycia**: ≤ 3 sekundy (jeden interwał heartbeat).

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

### 13.4. Protokół elekcji krok po kroku

Każdorazowo gdy `ElectionService.elect(txId)` jest wywołany:

1. **Sprawdź lokalny log**: jeśli jest `COMMITTED` lub `ABORTED` → przerwij i zwróć tę wartość.
2. **Zapytaj rówieśników**: wyślij `GET /api/peers/log/{txId}` do wszystkich znanych uczestników równolegle.
3. **Analiza odpowiedzi**:
   - Każda odpowiedź `COMMITTED` → cały wynik = `COMMITTED`.
   - Każda odpowiedź `ABORTED` → cały wynik = `ABORTED`.
   - Jeśli wszyscy odpowiedzieli `PREPARING`/`UNCERTAIN`/brak odpowiedzi → wynik = `UNCERTAIN`.
4. Jeśli wynik jest `COMMITTED` lub `ABORTED`, uczestnik aktualizuje swój log i odpowiada na żądania.
5. Jeśli wynik jest `UNCERTAIN`, wpis pozostaje w stanie `PREPARING` i elekcja zostanie powtórzona przy następnym ticku schedulera.

> **Warunek działania**: protokół elekcji rozstrzyga transakcję tylko wtedy, gdy **przynajmniej jeden** uczestnik otrzymał już decyzję COMMIT lub ABORT od koordynatora. Jeśli awaria nastąpiła przed wysłaniem jakiejkolwiek decyzji, wszyscy uczestnicy pozostają w stanie `UNCERTAIN` aż do przywrócenia koordynatora.

---

### 13.5. Flaga redundancyEnabled

| Wartość | Zachowanie                                                                                     |
| ------- | ---------------------------------------------------------------------------------------------- |
| `true`  | Protokół elekcji działa normalnie (peer consultation aktywna). **Domyślnie.**                  |
| `false` | `elect()` natychmiast zwraca `UNCERTAIN` bez pytania rówieśników — symulacja braku redundancji |

#### redundancyEnabled = true (domyślnie)

```
Uczestnik A (PREPARING)          Uczestnik B (COMMITTED)
        │                                 │
        │──GET /api/peers/log/{txId} ────►│
        │◄── { phase: COMMITTED } ────────│
        │  updatePhase(txId, COMMITTED)   │
        │  transakcja rozstrzygnięta ✓    │
```

**Skutek**: system jest odporny na awarię koordynatora. Transakcje są zawsze finalnie rozstrzygane, o ile przynajmniej jeden uczestnik zdążył otrzymać decyzję przed awarią.

#### redundancyEnabled = false

```
Uczestnik A (PREPARING)
        │
        │  elect(txId) → UNCERTAIN  (bez zapytań do rówieśników)
        │  log pozostaje PREPARING
        │  ... (retry co 5 s, wynik zawsze UNCERTAIN)
        │  transakcja nigdy nie zostaje rozstrzygnięta ✗
```

**Skutek**: klasyczny problem blokowania 2PC — awaria koordynatora powoduje permanentne zablokowanie uczestników.

---

### 13.6. Podsumowanie odporności

| Scenariusz awarii                             | Zachowanie systemu                                                 |
| --------------------------------------------- | ------------------------------------------------------------------ |
| Awaria uczestnika przed głosowaniem           | Koordynator traktuje brak odpowiedzi jako NO → ABORT               |
| Awaria uczestnika po głosowaniu YES           | Pozostałe kopie logu wystarczą do elekcji                          |
| Awaria koordynatora przed PREPARE             | Transakcja nie zaczęta; brak blokad                                |
| Awaria koordynatora po PREPARE, przed decyzją | Heartbeat wykrywa w ≤ 3 s; elekcja między uczestnikami             |
| Awaria koordynatora po wysłaniu decyzji       | Przynajmniej jeden uczestnik zna wynik; elekcja go odtworzy        |
| Awaria wszystkich uczestników jednocześnie    | Dane transakcji są tracone (log jest in-memory, brak persystencji) |
| Symulacja awarii (CRASH / NETWORK_DELAY)      | Wstrzykiwane przez UI; heartbeat i scheduler działają tak samo     |

---

## 14. Eksportowanie wyników do pliku

GUI nie zawiera wbudowanego eksportu PDF. Poniżej opisano dostępne metody.

### 14.1. Kopiowanie raportu do schowka

1. Uruchom scenariusz lub całą serię w panelu **Test odporności na błędy**.
2. Kliknij przycisk **Kopiuj raport** (widoczny w raporcie każdego scenariusza) lub **Kopiuj raport** (w raporcie serii).
3. Wklej skopiowany tekst do dowolnego edytora:
   - Notion, Obsidian, Google Docs → wklejony jako sformatowany markdown.
   - VS Code → zapisz jako `results.md`.
   - Word / LibreOffice → wklej jako tekst zwykły.

### 14.2. Zapis jako PDF z przeglądarki

1. Otwórz stronę z wynikami w przeglądarce.
2. Naciśnij `Ctrl+P` (Windows/Linux) lub `Cmd+P` (macOS).
3. Jako drukarkę wybierz **Zapisz jako PDF**.
4. Kliknij **Zapisz**.

### 14.3. Eksport raportu serii jako markdown

Skopiowany przez przycisk **Kopiuj raport** tekst raportu serii jest sformatowany jako tabela Markdown. Gotowy plik możesz wkleić bezpośrednio do dokumentu sprawozdania lub README projektu.

---

## 15. Najczęstsze pytania

**Czy werdykt DEGRADED oznacza błąd systemu?**
Nie. Kilka scenariuszy jest celowo zaprojektowanych z oczekiwanym wynikiem `DEGRADED`. Scenariusz `single-partial-send` i oba scenariusze `extreme-coordinator-silent-phase2` kończą się wynikiem `DEGRADED`, ponieważ właśnie takie jest prawidłowe zachowanie systemu w tym stanie awarii.

**Ile scenariuszy kończy się DEGRADED w normalnym przebiegu?**
Cztery: `single-partial-send`, `extreme-coordinator-silent-phase2`, `redundancy-off-partial-send` oraz `single-intermittent-fault` (probabilistyczny — może być PASS).

**Co zrobić, jeśli scenariusz kończy się FAIL zamiast oczekiwanego PASS?**
Sprawdź, czy system jest w czystym stanie: uruchom scenariusz `baseline-happy-path` jako pierwszy. Upewnij się, że żadne błędy nie są aktywne (kliknij **Wyczyść błędy** dla każdego węzła), a metryki odpowiadają oczekiwanym wartościom.

**Czy można uruchamiać scenariusze równolegle?**
Nie. Scenariusze modyfikują stan systemu (wstrzykują i czyszczą błędy, inicjują transakcje) i muszą być uruchamiane sekwencyjnie. Przycisk **Uruchom wszystkie scenariusze** robi to automatycznie.

**Co oznacza wartość `Niepewne` w MetricsBar?**
Transakcje w stanie `UNCERTAIN` to takie, których uczestnicy nie otrzymali decyzji Fazy 2. Przy włączonej redundancji (`Redundancja WŁ`) wartość powinna wrócić do 0 po kilku sekundach. Przy wyłączonej redundancji wartość może rosnąć po uruchomieniu scenariuszy z milczącym koordynatorem.
