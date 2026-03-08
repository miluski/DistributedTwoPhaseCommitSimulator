package com.distributed2pc.coordinator.feature.transaction.infrastructure;

import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.coordinator.feature.transaction.domain.CoordinatorTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link InMemoryTransactionRepository}.
 */
class InMemoryTransactionRepositoryTest {

    InMemoryTransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
    }

    @Test
    void save_givenNewTransaction_storesIt() {
        CoordinatorTransaction tx = new CoordinatorTransaction("value-1");

        repository.save(tx);

        assertThat(repository.findById(tx.getId())).contains(tx);
    }

    @Test
    void findById_givenUnknownId_returnsEmpty() {
        Optional<CoordinatorTransaction> result = repository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_givenMultipleTransactions_returnsInInsertionOrder() {
        CoordinatorTransaction tx1 = new CoordinatorTransaction("val-1");
        CoordinatorTransaction tx2 = new CoordinatorTransaction("val-2");
        CoordinatorTransaction tx3 = new CoordinatorTransaction("val-3");
        repository.save(tx1);
        repository.save(tx2);
        repository.save(tx3);

        List<CoordinatorTransaction> all = repository.findAll();

        assertThat(all).containsExactly(tx1, tx2, tx3);
    }

    @Test
    void findAll_givenEmptyRepository_returnsEmptyList() {
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void updateStatus_givenExistingTransaction_changesStatus() {
        CoordinatorTransaction tx = new CoordinatorTransaction("value");
        repository.save(tx);

        repository.updateStatus(tx.getId(), TransactionStatus.COMMITTED);

        assertThat(repository.findById(tx.getId()).orElseThrow().getStatus())
                .isEqualTo(TransactionStatus.COMMITTED);
    }

    @Test
    void updateStatus_givenUnknownId_doesNothing() {
        repository.updateStatus(UUID.randomUUID(), TransactionStatus.ABORTED);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void save_givenTransactionSavedTwice_doesNotDuplicateInFindAll() {
        CoordinatorTransaction tx = new CoordinatorTransaction("value");
        repository.save(tx);
        repository.save(tx);

        assertThat(repository.findAll()).hasSize(2);
    }
}
