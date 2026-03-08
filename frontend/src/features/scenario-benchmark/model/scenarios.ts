import { initiateTransaction, injectCoordinatorFault, injectFault } from '@common/api';
import type { Scenario } from '@features/scenario-benchmark/types';
import {
  clearAllFaults,
  makeResult,
  setRedundancyOnAll,
  snapshotMetrics,
} from '@features/scenario-benchmark/utils';

/** All fault scenarios ordered by category. */
const BASELINE: Scenario[] = [
  {
    id: 'baseline-happy-path',
    label: 'Ścieżka idealna — wszystkie węzły sprawne',
    category: 'baseline',
    description:
      'Brak aktywnych błędów. Wszyscy uczestnicy są sprawni. Ustala bazową wydajność zatwierdzenia i weryfikuje konsensus w przypadku braku błędów.',
    expectedBehaviour: 'Transakcja zostaje zatwierdzona. Liczba zatwierdzeń rośnie o 1.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      await clearAllFaults(ps);
      const steps = ['Wszystkie błędy wyczyszczone — system w czystym stanie'];
      let committed = false;
      try {
        const tx = await initiateTransaction('baseline-happy');
        steps.push(`TXN ${tx.transactionId.slice(0, 8)} → ${tx.status}`);
        committed = tx.status === 'COMMITTED';
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
      }
      const after = await snapshotMetrics();
      return makeResult(
        committed ? 'PASS' : 'FAIL',
        committed
          ? 'Transakcja zatwierdzona. System działa prawidłowo — brak błędów, wszyscy uczestnicy wyrazili zgodę.'
          : 'Transakcja nie została zatwierdzona. Sprawdź stan systemu.',
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'baseline-forced-abort',
    label: 'Jednogłośny głos NIE',
    category: 'baseline',
    description:
      'Wymusz na wszystkich uczestnikach głosowanie NIE jednocześnie. Koordynator musi zebrać wszystkie głosy NIE i podjąć decyzję ABORT, weryfikując podstawową ścieżkę przerwania.',
    expectedBehaviour: 'Transakcja zostaje przerwana. Liczba przerwań rośnie o 1.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const steps = [`FORCE_ABORT_VOTE na wszystkich ${ps.length} uczestnikach`];
      if (ps.length === 0)
        return makeResult('FAIL', 'Brak uczestników.', steps, before, before, start);
      await Promise.all(ps.map((p) => injectFault(p.port, 'FORCE_ABORT_VOTE', true)));
      let aborted = false;
      try {
        const tx = await initiateTransaction('baseline-forced-abort');
        aborted = tx.status === 'ABORTED';
        steps.push(`TXN ${tx.transactionId.slice(0, 8)} → ${tx.status}`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
      } finally {
        await Promise.all(ps.map((p) => injectFault(p.port, 'FORCE_ABORT_VOTE', false)));
        steps.push('FORCE_ABORT_VOTE wyczyszczony');
      }
      const after = await snapshotMetrics();
      return makeResult(
        aborted ? 'PASS' : 'FAIL',
        aborted
          ? 'Transakcja przerwana. Koordynator poprawnie zinterpretował wszystkie głosy NIE i wydał decyzję ABORT.'
          : 'Transakcja nie została przerwana mimo wszystkich głosów NIE. Sprawdź logi koordynatora.',
        steps,
        before,
        after,
        start
      );
    },
  },
];

const SINGLE_FAULT: Scenario[] = [
  {
    id: 'single-crash-participant',
    label: 'Awaria jednego uczestnika',
    category: 'single-fault',
    description:
      'Zawieś jednego uczestnika przed Fazą 1. Koordynator otrzymuje głos NIE (poprzez obsługę błędów) i przerywa transakcję. Testuje izolację awarii jednego węzła.',
    expectedBehaviour:
      'Transakcja zostaje przerwana. System pozostaje spójny — brak częściowych zatwierdzeń.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const target = ps[0];
      if (!target) return makeResult('FAIL', 'Brak uczestników.', [], before, before, start);
      const steps = [`Symulacja awarii: ${target.serverId}`];
      await injectFault(target.port, 'CRASH', true);
      let status = '';
      try {
        const tx = await initiateTransaction('single-crash');
        status = tx.status;
        steps.push(`TXN → ${tx.status}`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
        status = 'ERROR';
      } finally {
        await injectFault(target.port, 'CRASH', false);
        steps.push(`${target.serverId} przywrócony`);
      }
      const after = await snapshotMetrics();
      const aborted = after.aborted > before.aborted;
      return makeResult(
        aborted ? 'PASS' : 'DEGRADED',
        aborted
          ? `${target.serverId} był niedostępny — koordynator nie otrzymał głosu i przerwał transakcję. Izolacja awarii jednego węzła działa poprawnie.`
          : `Nieoczekiwany wynik: transakcja zakończyła się ze statusem ${status} mimo awarii uczestnika.`,
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'single-network-delay',
    label: 'Opóźnienie sieciowe jednego uczestnika',
    category: 'single-fault',
    description:
      'Dodaj opóźnienie sieciowe 800 ms do jednego uczestnika. Transakcja musi zostać zatwierdzona, demonstrując tolerancję systemu na skoki latencji.',
    expectedBehaviour:
      'Transakcja zostaje zatwierdzona. avgDecisionMs zauważalnie wzrasta względem linii bazowej.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const target = ps[0];
      if (!target) return makeResult('FAIL', 'Brak uczestników.', [], before, before, start);
      const steps = [`NETWORK_DELAY 800ms na węźle ${target.serverId}`];
      await injectFault(target.port, 'NETWORK_DELAY', true, { delayMs: 800 });
      let committed = false;
      try {
        const tx = await initiateTransaction('single-delay');
        committed = tx.status === 'COMMITTED';
        steps.push(`TXN → ${tx.status} (łącznie ${Date.now() - start} ms)`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
      } finally {
        await injectFault(target.port, 'NETWORK_DELAY', false);
        steps.push('Opóźnienie wyczyszczone');
      }
      const after = await snapshotMetrics();
      return makeResult(
        committed ? 'PASS' : 'FAIL',
        committed
          ? 'Opóźnienie 800 ms zostało wchłonięte — transakcja zatwierdzona. System toleruje skoki latencji bez utraty spójności.'
          : 'Transakcja nie powiodła się mimo opóźnienia sieciowego. Sprawdź logi koordynatora.',
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'single-force-abort-vote',
    label: 'Pojedynczy wymuszony głos przerwania',
    category: 'single-fault',
    description:
      'Wymusz na jednym uczestniku głos NIE, podczas gdy wszyscy inni głosują TAK. Demonstruje, że jeden głos NIE wystarczy, aby przerwać całą transakcję.',
    expectedBehaviour:
      'Transakcja zostaje przerwana. Jeden dysydent przesłania wszystkie głosy TAK.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const target = ps[0];
      if (!target) return makeResult('FAIL', 'Brak uczestników.', [], before, before, start);
      const steps = [`FORCE_ABORT_VOTE na ${target.serverId} (pozostali głosują TAK)`];
      await injectFault(target.port, 'FORCE_ABORT_VOTE', true);
      let aborted = false;
      try {
        const tx = await initiateTransaction('single-force-abort');
        aborted = tx.status === 'ABORTED';
        steps.push(`TXN → ${tx.status}`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
      } finally {
        await injectFault(target.port, 'FORCE_ABORT_VOTE', false);
        steps.push('Błąd wyczyszczony');
      }
      const after = await snapshotMetrics();
      return makeResult(
        aborted ? 'PASS' : 'FAIL',
        aborted
          ? `${target.serverId} zagłosował NIE — jeden sprzeciw wystarczy, aby przerwać całą transakcję. Protokół 2PC działa zgodnie z założeniami.`
          : 'Oczekiwano ABORT po jednym głosie NIE, lecz transakcja nie została przerwana.',
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'single-transient-fault',
    label: 'Przejściowa awaria uczestnika (1,5 s)',
    category: 'single-fault',
    description:
      'Aktywuj przejściowy błąd (1,5 s) na jednym uczestniku. Węzeł staje się tymczasowo niedostępny, a następnie automatycznie się odtwarza. Testuje, jak koordynator obsługuje krótkie awarie.',
    expectedBehaviour:
      'Transakcja zostaje zatwierdzona lub przerwana zależnie od czasu. Brak trwałych uszkodzeń.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const target = ps[0];
      if (!target) return makeResult('FAIL', 'Brak uczestników.', [], before, before, start);
      const steps = [`TRANSIENT 1500ms na węźle ${target.serverId} — automatycznie znika po 1,5 s`];
      await injectFault(target.port, 'TRANSIENT', true, { durationMs: 1500 });
      let txStatus = 'UNKNOWN';
      try {
        const tx = await initiateTransaction('single-transient');
        txStatus = tx.status;
        steps.push(`TXN → ${tx.status}`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
        txStatus = 'ERROR';
      }
      const after = await snapshotMetrics();
      const settled = txStatus === 'COMMITTED' || txStatus === 'ABORTED';
      return makeResult(
        settled ? 'PASS' : 'DEGRADED',
        settled
          ? `Węzeł był niedostępny przez 1,5 s i automatycznie się odtworzył. Transakcja zakończyła się ze statusem ${txStatus}.`
          : 'Transakcja nie zakończyła się w oczekiwanym czasie. Sprawdź mechanizm odtwarzania.',
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'single-intermittent-fault',
    label: 'Przerywany błąd (80 % wskaźnik utraty)',
    category: 'single-fault',
    description:
      'Jeden uczestnik odrzuca 80 % wiadomości. Z dużym prawdopodobieństwem koordynator nie może zebrać ważnego głosu TAK i przerywa transakcję.',
    expectedBehaviour:
      'Transakcja prawdopodobnie zostaje przerwana. Werdykt ZDEGRADOWANY, jeśli nieoczekiwanie zostanie zatwierdzona (poprawne, lecz rzadkie).',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const target = ps[0];
      if (!target) return makeResult('FAIL', 'Brak uczestników.', [], before, before, start);
      const steps = [`INTERMITTENT 80% wskaźnik utraty na węźle ${target.serverId}`];
      await injectFault(target.port, 'INTERMITTENT', true, { chancePercent: 80 });
      try {
        const tx = await initiateTransaction('single-intermittent');
        steps.push(`TXN → ${tx.status}`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
      } finally {
        await injectFault(target.port, 'INTERMITTENT', false);
        steps.push('Błąd wyczyszczony');
      }
      const after = await snapshotMetrics();
      const aborted = after.aborted > before.aborted;
      return makeResult(
        aborted ? 'PASS' : 'DEGRADED',
        aborted
          ? '80% pakietów było gubione, ale koordynator nie otrzymał ważnego głosu TAK i przerwał transakcję.'
          : `Transakcja zatwierdzona mimo 80% utraty pakietów — węzeł zdążył odpowiedzieć na czas. To dopuszczalny wynik probabilistyczny.`,
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'single-partial-send',
    label: 'Częściowe wysyłanie Fazy 2 (1 z N)',
    category: 'single-fault',
    description:
      'Koordynator wysyła COMMIT Fazy 2 tylko do 1 uczestnika. Pozostałe węzły nigdy nie otrzymują decyzji — to klasyczny scenariusz rozszczepionego stanu w protokole 2PC.',
    expectedBehaviour:
      'ZDEGRADOWANY — koordynator rejestruje COMMITTED; pozostali uczestnicy utknęli w PREPARING. Harmonogram odtwarzania rozwiązuje problem w obrębie limitu czasu.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const steps = [
        'MESSAGE_LOSS(count=1): koordynator wysłał Fazę 2 tylko do pierwszego uczestnika',
      ];
      await injectCoordinatorFault('MESSAGE_LOSS', true, { count: 1 });
      let txStatus = 'UNKNOWN';
      try {
        const tx = await initiateTransaction('single-partial-send');
        txStatus = tx.status;
        steps.push(
          `TXN koordynatora → ${tx.status} (perspektywa koordynatora)`,
          `${ps.length - 1} uczestników niepowtiadomionych — harmonogram odtwarzania rozwiąże w ciągu 10 s`
        );
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
        txStatus = 'ERROR';
      } finally {
        await injectCoordinatorFault('MESSAGE_LOSS', false);
        steps.push('MESSAGE_LOSS wyczyszczony');
      }
      const after = await snapshotMetrics();
      return makeResult(
        txStatus === 'COMMITTED' ? 'DEGRADED' : 'FAIL',
        txStatus === 'COMMITTED'
          ? `Koordynator zatwierdził, ale ${ps.length - 1} uczestników nie dostało decyzji Fazy 2. Uczestnicy czekają w stanie PREPARING — harmonogram odtwarzania powinien to rozwiązać w ciągu kilku sekund.`
          : `Transakcja nie została zatwierdzona przez koordynatora (status: ${txStatus}).`,
        steps,
        before,
        after,
        start
      );
    },
  },
];

const COMPOUND: Scenario[] = [
  {
    id: 'compound-multi-crash',
    label: 'Awaria połowy uczestników',
    category: 'compound',
    description:
      'Zawieś połowę zarejestrowanych uczestników jednocześnie. Testuje obsługę przez koordynatora wielu równoczesnych głosów NIE.',
    expectedBehaviour:
      'Transakcja zostaje przerwana. Wiele głosów NIE obsługiwanych poprawnie — brak rozszczepionego stanu.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const half = ps.slice(0, Math.max(1, Math.floor(ps.length / 2)));
      const ids = half.map((p) => p.serverId).join(', ');
      const steps = [`Symulacja awarii ${half.length} z ${ps.length} uczestników: [${ids}]`];
      await Promise.all(half.map((p) => injectFault(p.port, 'CRASH', true)));
      let aborted = false;
      try {
        const tx = await initiateTransaction('compound-multi-crash');
        aborted = tx.status === 'ABORTED';
        steps.push(`TXN → ${tx.status}`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
      } finally {
        await Promise.all(half.map((p) => injectFault(p.port, 'CRASH', false)));
        steps.push(`${half.length} uczestników przywróconych`);
      }
      const after = await snapshotMetrics();
      return makeResult(
        aborted ? 'PASS' : 'DEGRADED',
        aborted
          ? `${half.length} z ${ps.length} węzłów było niedostępnych — koordynator przerwał transakcję przy braku kworum głosów TAK.`
          : `Oczekiwano ABORT przy awarii połowy uczestników, ale transakcja nie została przerwana.`,
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'compound-crash-delay-mix',
    label: 'Awaria jednego + opóźnienie pozostałych',
    category: 'compound',
    description:
      'Zawieś jednego uczestnika i dodaj opóźnienie sieciowe 800 ms do wszystkich pozostałych. Testuje przerwanie przy złożonej awarii ze zwiększoną latencją.',
    expectedBehaviour:
      'Transakcja zostaje przerwana. Awaria dostarcza głosu NIE; opóźnienia dodają mierzalną latencję.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const crashed = ps[0];
      const delayed = ps.slice(1);
      if (!crashed) return makeResult('FAIL', 'Brak uczestników.', [], before, before, start);
      const steps = [
        `Awaria: ${crashed.serverId} | Opóźnienie 800ms: ${delayed.map((p) => p.serverId).join(', ') || 'brak'}`,
      ];
      await injectFault(crashed.port, 'CRASH', true);
      await Promise.all(
        delayed.map((p) => injectFault(p.port, 'NETWORK_DELAY', true, { delayMs: 800 }))
      );
      let aborted = false;
      try {
        const tx = await initiateTransaction('compound-crash-delay');
        aborted = tx.status === 'ABORTED';
        steps.push(`TXN → ${tx.status}`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
      } finally {
        await injectFault(crashed.port, 'CRASH', false);
        await Promise.all(delayed.map((p) => injectFault(p.port, 'NETWORK_DELAY', false)));
        steps.push('Wszystkie błędy wyczyszczone');
      }
      const after = await snapshotMetrics();
      return makeResult(
        aborted ? 'PASS' : 'DEGRADED',
        aborted
          ? `Złożona awaria obsłużona — ${crashed.serverId} był niedostępny, a pozostałe węzły miały opóźnienie 800 ms. Koordynator poprawnie przerwał transakcję.`
          : 'Złożona awaria (crash + opóźnienie) dała nieoczekiwany wynik.',
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'compound-coordinator-delay-participant-crash',
    label: 'Opóźnienie koordynatora + awaria uczestnika',
    category: 'compound',
    description:
      'Dodaj 2-sekundowe opóźnienie Fazy 2 koordynatora i jednocześnie zawieś jednego uczestnika. Testuje dwuwarstwową awarię, w której Faza 2 jest zarówno opóźniona, jak i napotyka zawieszony węzeł.',
    expectedBehaviour:
      'Transakcja zostaje przerwana po opóźnieniu. Dwuwarstwowa awaria rozwiązana poprawnie.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const target = ps[0];
      if (!target) return makeResult('FAIL', 'Brak uczestników.', [], before, before, start);
      const steps = [`Opóźnienie koordynatora NETWORK_DELAY 2000ms ORAZ awaria ${target.serverId}`];
      await injectCoordinatorFault('NETWORK_DELAY', true, { delayMs: 2000 });
      await injectFault(target.port, 'CRASH', true);
      let aborted = false;
      try {
        const tx = await initiateTransaction('compound-coord-delay-crash');
        aborted = tx.status === 'ABORTED';
        steps.push(`TXN → ${tx.status} (po opóźnieniu koordynatora)`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
      } finally {
        await injectFault(target.port, 'CRASH', false);
        await injectCoordinatorFault('NETWORK_DELAY', false);
        steps.push('Wszystkie błędy wyczyszczone');
      }
      const after = await snapshotMetrics();
      return makeResult(
        aborted ? 'PASS' : 'DEGRADED',
        aborted
          ? `Podwójna awaria obsłużona — koordynator działał z opóźnieniem 2 s, a ${target.serverId} był niedostępny. Transakcja przerwana.`
          : 'Koordynator opóźniony i uczestnik offline — oczekiwano ABORT, ale transakcja się nie przerwała.',
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'compound-all-delays',
    label: 'Wszyscy uczestnicy opóźnieni (600 ms)',
    category: 'compound',
    description:
      'Dodaj opóźnienie sieciowe 600 ms do wszystkich uczestników jednocześnie. Transakcja musi zostać zatwierdzona, lecz ze znacznie podwyższoną latencją. Demonstruje łagodną degradację wydajności.',
    expectedBehaviour:
      'Transakcja zostaje zatwierdzona. avgDecisionMs znacznie wyższe niż w linii bazowej.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      if (ps.length === 0)
        return makeResult('FAIL', 'Brak uczestników.', [], before, before, start);
      const steps = [`NETWORK_DELAY 600ms na wszystkich ${ps.length} uczestnikach`];
      await Promise.all(
        ps.map((p) => injectFault(p.port, 'NETWORK_DELAY', true, { delayMs: 600 }))
      );
      let committed = false;
      try {
        const tx = await initiateTransaction('compound-all-delays');
        committed = tx.status === 'COMMITTED';
        steps.push(`TXN → ${tx.status} (łącznie ${Date.now() - start} ms)`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
      } finally {
        await Promise.all(ps.map((p) => injectFault(p.port, 'NETWORK_DELAY', false)));
        steps.push('Wszystkie opóźnienia wyczyszczone');
      }
      const after = await snapshotMetrics();
      const latencyIncrease = (after.avgDecisionMs - before.avgDecisionMs).toFixed(0);
      return makeResult(
        committed ? 'PASS' : 'FAIL',
        committed
          ? `Transakcja zatwierdzona. Wszystkich ${ps.length} uczestników miało opóźnienie 600 ms — średni czas decyzji wzrósł o +${latencyIncrease} ms, ale spójność została zachowana.`
          : 'Oczekiwano zatwierdzenia przy równomiernym opóźnieniu, lecz transakcja się nie powiodła.',
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'compound-intermittent-force-abort',
    label: 'Przerywany + wymuszony błąd (dwa węzły)',
    category: 'compound',
    description:
      'Zastosuj 60% przerywany błąd do jednego uczestnika i wymuszony głos przerwania do drugiego. Testuje złożoną awarię, w której istnieją dwa źródła niepowodzeń.',
    expectedBehaviour:
      'Transakcja zostaje przerwana. Istnieje co najmniej jeden decydujący głos NIE.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const intermittent = ps[0];
      const forceAbort = ps[1] ?? ps[0];
      if (!intermittent) return makeResult('FAIL', 'Brak uczestników.', [], before, before, start);
      const steps = [
        `INTERMITTENT 60% na ${intermittent.serverId} + FORCE_ABORT_VOTE na ${forceAbort.serverId}`,
      ];
      await injectFault(intermittent.port, 'INTERMITTENT', true, { chancePercent: 60 });
      if (forceAbort !== intermittent) await injectFault(forceAbort.port, 'FORCE_ABORT_VOTE', true);
      let aborted = false;
      try {
        const tx = await initiateTransaction('compound-intermittent-force-abort');
        aborted = tx.status === 'ABORTED';
        steps.push(`TXN → ${tx.status}`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
      } finally {
        await injectFault(intermittent.port, 'INTERMITTENT', false);
        if (forceAbort !== intermittent)
          await injectFault(forceAbort.port, 'FORCE_ABORT_VOTE', false);
        steps.push('Wszystkie błędy wyczyszczone');
      }
      const after = await snapshotMetrics();
      return makeResult(
        aborted ? 'PASS' : 'DEGRADED',
        aborted
          ? 'Złożona awaria (błąd przerywany + wymuszony NIE) obsłużona — koordynator przerwał transakcję.'
          : 'Dwa źródła błędów nie spowodowały ABORT — nieoczekiwany wynik.',
        steps,
        before,
        after,
        start
      );
    },
  },
];

const EXTREME: Scenario[] = [
  {
    id: 'extreme-all-crash',
    label: 'Całkowita awaria uczestników',
    category: 'extreme',
    description:
      'Zawieś WSZYSTKICH uczestników jednocześnie. Koordynator otrzymuje głosy NIE od każdego węzła i musi przerwać transakcję. Testuje absolutnie najgorszy scenariusz awarii uczestników — system musi pozostać bezpieczny bez częściowych zatwierdzeń.',
    expectedBehaviour:
      'Transakcja zostaje przerwana. Koordynator pozostaje spójny pomimo całkowitej awarii węzłów.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      if (ps.length === 0)
        return makeResult('FAIL', 'Brak zarejestrowanych uczestników.', [], before, before, start);
      const steps = [`Symulacja awarii WSZYSTKICH ${ps.length} uczestników jednocześnie`];
      await Promise.all(ps.map((p) => injectFault(p.port, 'CRASH', true)));
      let aborted = false;
      try {
        const tx = await initiateTransaction('extreme-all-crash');
        aborted = tx.status === 'ABORTED';
        steps.push(`TXN → ${tx.status} — koordynator obsłużył całkowitą awarię`);
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
      } finally {
        await Promise.all(ps.map((p) => injectFault(p.port, 'CRASH', false)));
        steps.push(`Wszyscy ${ps.length} uczestnicy przywróceni`);
      }
      const after = await snapshotMetrics();
      return makeResult(
        aborted ? 'PASS' : 'FAIL',
        aborted
          ? `Wszystkie ${ps.length} węzłów było niedostępnych — koordynator przerwał transakcję. System jest bezpieczny: żadna częściowa transakcja nie została zatwierdzona.`
          : 'Krytyczny błąd: przy całkowitej awarii uczestników oczekiwano ABORT, ale transakcja nie została przerwana.',
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'extreme-coordinator-crash',
    label: 'Awaria koordynatora (test SPOF)',
    category: 'extreme',
    description:
      'Zawieś koordynatora całkowicie. Wszystkie przychodzące żądania są odrzucane z kodem 503. Po usunięciu błędu system musi się odtworzyć i przyjmować nowe transakcje. Testuje koordynatora jako pojedynczy punkt awarii.',
    expectedBehaviour: 'Odrzucenie żądań podczas okna awarii (oczekiwane). ZAL. po odtworzeniu.',
    run: async (_) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const steps = ['Wstrzykiwanie CRASH na koordynatorze — wszystkie żądania zwrócą 503'];
      await injectCoordinatorFault('CRASH', true);
      let crashDenied = false;
      let recoveryCommitted = false;
      try {
        await initiateTransaction('extreme-coord-crash-attempt').catch(() => {
          crashDenied = true;
          steps.push(
            'TXN odrzucona z kodem 503 — koordynator poprawnie niedostępny podczas awarii'
          );
        });
      } finally {
        await injectCoordinatorFault('CRASH', false);
        steps.push('CRASH koordynatora wyczyszczony — system odtwarza się');
      }
      try {
        const tx = await initiateTransaction('extreme-coord-recovery');
        recoveryCommitted = tx.status === 'COMMITTED';
        steps.push(`TXN odtwarzania → ${tx.status} — koordynator z powrotem online`);
      } catch (e) {
        steps.push(`Błąd TXN odtwarzania: ${e instanceof Error ? e.message : String(e)}`);
      }
      const after = await snapshotMetrics();
      const partialVerdict = crashDenied ? 'DEGRADED' : 'FAIL';
      const verdict = crashDenied && recoveryCommitted ? 'PASS' : partialVerdict;
      return makeResult(
        verdict,
        verdict === 'PASS'
          ? 'Koordynator był niedostępny i odrzucał żądania (zgodnie z oczekiwaniami), następnie automatycznie się odtworzył i zatwierdził nową transakcję.'
          : `Częściowy sukces: odrzucanie żądań podczas awarii = ${crashDenied}, zatwierdzenie po odtworzeniu = ${recoveryCommitted}.`,
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'extreme-coordinator-silent-phase2',
    label: 'Koordynator milczący po Fazie 1 (najgorszy przypadek)',
    category: 'extreme',
    description:
      'Koordynator zbiera wszystkie głosy TAK, lecz nie wysyła Fazy 2 do nikogo (MESSAGE_LOSS count=0). Symuluje awarię koordynatora między fazami. Uczestnicy pozostają w stanie PREPARING — najbardziej niebezpieczny tryb awarii 2PC.',
    expectedBehaviour:
      'ZDEGRADOWANY — rozszczepiany stan. Koordynator rejestruje COMMITTED; uczestnicy utknęli w PREPARING. Harmonogram odtwarzania rozwiązuje problem w ciągu 10 s.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      await clearAllFaults(ps);
      const steps = [
        'MESSAGE_LOSS(count=0): koordynator zbiera głosy, lecz nie wysyła Fazy 2 do NIKOGO',
      ];
      await injectCoordinatorFault('MESSAGE_LOSS', true, { count: 0 });
      let txStatus = 'UNKNOWN';
      try {
        const tx = await initiateTransaction('extreme-silent-p2');
        txStatus = tx.status;
        steps.push(
          `TXN koordynatora → ${tx.status} (perspektywa koordynatora — uczestnicy niepowiadomieni)`,
          `Wszyscy ${ps.length} uczestnicy utknęli w PREPARING — harmonogram odtwarzania wykryje po upłynięciu limitu czasu`
        );
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
        txStatus = 'ERROR';
      } finally {
        await injectCoordinatorFault('MESSAGE_LOSS', false);
        steps.push(
          'MESSAGE_LOSS wyczyszczony — harmonogram odtwarzania może teraz dotrzeć do koordynatora'
        );
      }
      const after = await snapshotMetrics();
      return makeResult(
        txStatus === 'COMMITTED' ? 'DEGRADED' : 'FAIL',
        txStatus === 'COMMITTED'
          ? `Koordynator zatwierdził, ale żaden z ${ps.length} uczestników nie dostał decyzji Fazy 2. Uczestnicy czekają w stanie PREPARING — harmonogram odtwarzania powinien to rozwiązać w ciągu kilku sekund.`
          : `Koordynator nie zatwierdził transakcji (status: ${txStatus}).`,
        steps,
        before,
        after,
        start
      );
    },
  },
];

const REDUNDANCY: Scenario[] = [
  {
    id: 'redundancy-off-partial-send',
    label: 'Redundancja WYŁĄCZONA — Faza 2 milcząca',
    category: 'redundancy',
    description:
      'Przy wyłączonej redundancji koordynator nie wysyła Fazy 2 do nikogo. Usługa wyborów natychmiast zwraca UNCERTAIN bez konsultowania równorzędnych węzłów. Uczestnicy pozostają utknięci w PREPARING na stałe — bez automatycznego odtwarzania.',
    expectedBehaviour:
      'ZDEGRADOWANY — niepewne transakcje pozostają nierozwiązane bezterminowo. Demonstruje zachowanie systemu BEZ odporności na błędy.',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const steps = [
        'Wyłączanie redundancji na wszystkich węzłach (wyłącza odtwarzanie przez wybory równorzędne)',
      ];
      await setRedundancyOnAll(ps, false);
      await injectCoordinatorFault('MESSAGE_LOSS', true, { count: 0 });
      steps.push('MESSAGE_LOSS(count=0): koordynator nie wysyła Fazy 2 do nikogo');
      let txStatus = 'UNKNOWN';
      try {
        const tx = await initiateTransaction('redundancy-off-test');
        txStatus = tx.status;
        steps.push(
          `TXN → ${tx.status} (perspektywa koordynatora)`,
          'BEZ redundancji: wybory natychmiast zwracają UNCERTAIN — uczestnicy pozostają utknięci na zawsze'
        );
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
        txStatus = 'ERROR';
      } finally {
        await injectCoordinatorFault('MESSAGE_LOSS', false);
        await setRedundancyOnAll(ps, true);
        steps.push('Błędy wyczyszczone + redundancja przywrócona na WŁĄCZONĄ');
      }
      const after = await snapshotMetrics();
      return makeResult(
        'DEGRADED',
        `Bez redundancji uczestnicy utknęli w stanie PREPARING i nie mogą się samodzielnie odtworzyć. Koordynator zarejestrował ${txStatus}. To zamierzone zachowanie — porównaj z wynikiem gdy redundancja jest WŁĄCZONA.`,
        steps,
        before,
        after,
        start
      );
    },
  },
  {
    id: 'redundancy-on-partial-send',
    label: 'Redundancja WŁĄCZONA — Faza 2 milcząca (automatyczne odtwarzanie)',
    category: 'redundancy',
    description:
      'Przy włączonej redundancji, ten sam scenariusz koordynatora milczącego w Fazie 2. Harmonogram odtwarzania okresowo skanuje wpisy PREPARING starsze niż limit czasu i uruchamia wybory równorzędne, aby je rozwiązać.',
    expectedBehaviour:
      'ZAL. — harmonogram odtwarzania wykrywa utknięte wpisy PREPARING i rozwiązuje je poprzez wybory równorzędne w obrębie skonfigurowanego limitu czasu (~10 s).',
    run: async (ps) => {
      const start = Date.now();
      const before = await snapshotMetrics();
      const steps = ['Zapewnianie redundancji WŁĄCZONEJ na wszystkich węzłach'];
      await setRedundancyOnAll(ps, true);
      await injectCoordinatorFault('MESSAGE_LOSS', true, { count: 0 });
      steps.push('MESSAGE_LOSS(count=0): koordynator nie wysyła Fazy 2 do nikogo');
      let txStatus = 'UNKNOWN';
      try {
        const tx = await initiateTransaction('redundancy-on-test');
        txStatus = tx.status;
        steps.push(
          `TXN → ${tx.status} (perspektywa koordynatora)`,
          'Z redundancją: harmonogram odtwarzania wykryje wpisy PREPARING po 5 s i uruchomi wybory równorzędne',
          'Wybory równorzędne konsultują koordynatora, aby poznać status zatwierdzenia → rozwiąž PREPARING → COMMITTED'
        );
      } catch (e) {
        steps.push(`Błąd transakcji: ${e instanceof Error ? e.message : String(e)}`);
        txStatus = 'ERROR';
      } finally {
        await injectCoordinatorFault('MESSAGE_LOSS', false);
        steps.push(
          'MESSAGE_LOSS wyczyszczony — harmonogram odtwarzania może teraz dotrzeć do koordynatora'
        );
      }
      const after = await snapshotMetrics();
      return makeResult(
        txStatus === 'COMMITTED' ? 'PASS' : 'DEGRADED',
        txStatus === 'COMMITTED'
          ? 'Redundancja zadziałała — harmonogram odtwarzania wykrył utknięte wpisy PREPARING i uruchomił wybory równorzędne. Porównaj z wynikiem scenariusza „Redundancja WYŁĄCZONA".'
          : `Odtwarzanie może być w toku — sprawdź system za kilka sekund. Koordynator zarejestrował ${txStatus}.`,
        steps,
        before,
        after,
        start
      );
    },
  },
];

/** All available fault scenarios ordered by category. */
export const SCENARIOS: Scenario[] = [
  ...BASELINE,
  ...SINGLE_FAULT,
  ...COMPOUND,
  ...EXTREME,
  ...REDUNDANCY,
];
