package com.distributed2pc.participant.feature.log.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.participant.feature.log.domain.LogEntry;

/**
 * Unit tests for {@link InMemoryTransactionLog}.
 */
class InMemoryTransactionLogTest {

    InMemoryTransactionLog log;

    @BeforeEach
    void setUp() {
        log = new InMemoryTransactionLog();
    }

    @Test
    void size_givenEmptyLog_returnsZero() {
        assertThat(log.size()).isZero();
    }

    @Test
    void write_givenEntry_storesIt() {
        UUID txId = UUID.randomUUID();
        LogEntry entry = new LogEntry(txId, TransactionStatus.PREPARING, "val", Instant.now());

        log.write(entry);

        assertThat(log.size()).isEqualTo(1);
        assertThat(log.read(txId)).contains(entry);
    }

    @Test
    void read_givenUnknownId_returnsEmpty() {
        Optional<LogEntry> result = log.read(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void write_givenSameIdTwice_overwritesPreviousEntry() {
        UUID txId = UUID.randomUUID();
        LogEntry first = new LogEntry(txId, TransactionStatus.PREPARING, "v1", Instant.now());
        LogEntry second = new LogEntry(txId, TransactionStatus.COMMITTED, "v1", Instant.now());

        log.write(first);
        log.write(second);

        assertThat(log.size()).isEqualTo(1);
        assertThat(log.read(txId).orElseThrow().phase()).isEqualTo(TransactionStatus.COMMITTED);
    }

    @Test
    void updatePhase_givenExistingEntry_changesPhase() {
        UUID txId = UUID.randomUUID();
        log.write(new LogEntry(txId, TransactionStatus.PREPARING, "payload", Instant.now()));

        log.updatePhase(txId, TransactionStatus.COMMITTED);

        assertThat(log.read(txId).orElseThrow().phase()).isEqualTo(TransactionStatus.COMMITTED);
    }

    @Test
    void updatePhase_givenExistingEntry_preservesValue() {
        UUID txId = UUID.randomUUID();
        log.write(new LogEntry(txId, TransactionStatus.PREPARING, "original-value", Instant.now()));

        log.updatePhase(txId, TransactionStatus.ABORTED);

        assertThat(log.read(txId).orElseThrow().value()).isEqualTo("original-value");
    }

    @Test
    void updatePhase_givenUnknownId_doesNothing() {
        log.updatePhase(UUID.randomUUID(), TransactionStatus.COMMITTED);

        assertThat(log.size()).isZero();
    }

    @Test
    void findAllWithPhase_givenEntriesOfMixedPhases_returnsOnlyMatchingPhase() {
        UUID preparingId = UUID.randomUUID();
        UUID committedId = UUID.randomUUID();
        log.write(new LogEntry(preparingId, TransactionStatus.PREPARING, "v1", Instant.now()));
        log.write(new LogEntry(committedId, TransactionStatus.COMMITTED, "v2", Instant.now()));

        assertThat(log.findAllWithPhase(TransactionStatus.PREPARING))
                .hasSize(1)
                .allMatch(e -> e.transactionId().equals(preparingId));
    }

    @Test
    void findAllWithPhase_givenNoMatchingEntries_returnsEmptyList() {
        log.write(new LogEntry(UUID.randomUUID(), TransactionStatus.COMMITTED, "v", Instant.now()));

        assertThat(log.findAllWithPhase(TransactionStatus.PREPARING)).isEmpty();
    }

    @Test
    void getLatestCommittedValue_givenNoCommittedEntries_returnsEmpty() {
        log.write(new LogEntry(UUID.randomUUID(), TransactionStatus.PREPARING, "v", Instant.now()));

        assertThat(log.getLatestCommittedValue()).isEmpty();
    }

    @Test
    void getLatestCommittedValue_givenOneCommittedEntry_returnsThatValue() {
        UUID txId = UUID.randomUUID();
        log.write(new LogEntry(txId, TransactionStatus.COMMITTED, "committed-val", Instant.now()));

        assertThat(log.getLatestCommittedValue()).contains("committed-val");
    }

    @Test
    void getLatestCommittedValue_givenMultipleCommittedEntries_returnsLatestValue() throws InterruptedException {
        log.write(new LogEntry(UUID.randomUUID(), TransactionStatus.COMMITTED, "old-val", Instant.now()));
        Thread.sleep(2);
        log.write(new LogEntry(UUID.randomUUID(), TransactionStatus.COMMITTED, "new-val", Instant.now()));

        assertThat(log.getLatestCommittedValue()).contains("new-val");
    }
}
