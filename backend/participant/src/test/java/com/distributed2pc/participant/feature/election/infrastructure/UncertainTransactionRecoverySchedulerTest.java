package com.distributed2pc.participant.feature.election.infrastructure;

import static com.distributed2pc.common.enums.TransactionStatus.ABORTED;
import static com.distributed2pc.common.enums.TransactionStatus.COMMITTED;
import static com.distributed2pc.common.enums.TransactionStatus.PREPARING;
import static com.distributed2pc.common.enums.TransactionStatus.UNCERTAIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.distributed2pc.participant.feature.election.application.ElectionService;
import com.distributed2pc.participant.feature.log.domain.LogEntry;
import com.distributed2pc.participant.feature.log.infrastructure.InMemoryTransactionLog;

/**
 * Unit tests for {@link UncertainTransactionRecoveryScheduler}.
 */
@ExtendWith(MockitoExtension.class)
class UncertainTransactionRecoverySchedulerTest {

    @Mock
    InMemoryTransactionLog transactionLog;

    @Mock
    ElectionService electionService;

    UncertainTransactionRecoveryScheduler scheduler;

    private static final long TIMEOUT_MS = 5_000L;

    @BeforeEach
    void setUp() {
        scheduler = new UncertainTransactionRecoveryScheduler(transactionLog, electionService, TIMEOUT_MS);
    }

    @Test
    void recoverUncertainTransactions_givenNoPreparingEntries_doesNotCallElection() {
        when(transactionLog.findAllWithPhase(PREPARING)).thenReturn(List.of());

        scheduler.recoverUncertainTransactions();

        verify(electionService, never()).elect(any());
    }

    @Test
    void recoverUncertainTransactions_givenFreshPreparingEntry_skipsIt() {
        UUID txId = UUID.randomUUID();
        LogEntry fresh = new LogEntry(txId, PREPARING, "value", Instant.now());
        when(transactionLog.findAllWithPhase(PREPARING)).thenReturn(List.of(fresh));

        scheduler.recoverUncertainTransactions();

        verify(electionService, never()).elect(any());
    }

    @Test
    void recoverUncertainTransactions_givenStalePreparingEntry_triggersElection() {
        UUID txId = UUID.randomUUID();
        LogEntry stale = new LogEntry(txId, PREPARING, "value", Instant.now().minusMillis(10_000));
        when(transactionLog.findAllWithPhase(PREPARING)).thenReturn(List.of(stale));
        when(electionService.elect(txId)).thenReturn(COMMITTED);

        scheduler.recoverUncertainTransactions();

        verify(electionService).elect(txId);
        verify(transactionLog).updatePhase(txId, COMMITTED);
    }

    @Test
    void recoverUncertainTransactions_givenElectionResolvesAborted_updatesLogToAborted() {
        UUID txId = UUID.randomUUID();
        LogEntry stale = new LogEntry(txId, PREPARING, "value", Instant.now().minusMillis(10_000));
        when(transactionLog.findAllWithPhase(PREPARING)).thenReturn(List.of(stale));
        when(electionService.elect(txId)).thenReturn(ABORTED);

        scheduler.recoverUncertainTransactions();

        verify(transactionLog).updatePhase(txId, ABORTED);
    }

    @Test
    void recoverUncertainTransactions_givenElectionReturnsUncertain_doesNotUpdateLog() {
        UUID txId = UUID.randomUUID();
        LogEntry stale = new LogEntry(txId, PREPARING, "value", Instant.now().minusMillis(10_000));
        when(transactionLog.findAllWithPhase(PREPARING)).thenReturn(List.of(stale));
        when(electionService.elect(txId)).thenReturn(UNCERTAIN);

        scheduler.recoverUncertainTransactions();

        verify(transactionLog, never()).updatePhase(any(), any());
    }

    @Test
    void recoverUncertainTransactions_givenMultipleStaleSomeRecent_onlyRecoversStalOnes() {
        UUID staleId = UUID.randomUUID();
        UUID freshId = UUID.randomUUID();
        LogEntry stale = new LogEntry(staleId, PREPARING, "v1", Instant.now().minusMillis(10_000));
        LogEntry fresh = new LogEntry(freshId, PREPARING, "v2", Instant.now());
        when(transactionLog.findAllWithPhase(PREPARING)).thenReturn(List.of(stale, fresh));
        when(electionService.elect(staleId)).thenReturn(COMMITTED);

        scheduler.recoverUncertainTransactions();

        verify(electionService).elect(staleId);
        verify(electionService, never()).elect(freshId);
        verify(transactionLog).updatePhase(staleId, COMMITTED);
    }

    @Test
    void recoverAllPreparingNow_givenFreshPreparingEntry_triggersElectionImmediately() {
        UUID txId = UUID.randomUUID();
        LogEntry fresh = new LogEntry(txId, PREPARING, "value", Instant.now());
        when(transactionLog.findAllWithPhase(PREPARING)).thenReturn(List.of(fresh));
        when(electionService.elect(txId)).thenReturn(COMMITTED);

        scheduler.recoverAllPreparingNow();

        verify(electionService).elect(txId);
        verify(transactionLog).updatePhase(txId, COMMITTED);
    }
}
