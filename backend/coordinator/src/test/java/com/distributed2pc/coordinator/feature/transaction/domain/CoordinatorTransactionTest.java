package com.distributed2pc.coordinator.feature.transaction.domain;

import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.common.enums.VoteResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CoordinatorTransaction}.
 */
class CoordinatorTransactionTest {

    @Test
    void constructor_givenValue_initializesWithInitiatedStatus() {
        CoordinatorTransaction tx = new CoordinatorTransaction("payload");

        assertThat(tx.getValue()).isEqualTo("payload");
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.INITIATED);
        assertThat(tx.getId()).isNotNull();
        assertThat(tx.getInitiatedAt()).isNotNull();
        assertThat(tx.getDecidedAt()).isNull();
        assertThat(tx.getVotes()).isEmpty();
    }

    @Test
    void recordVote_givenSingleVote_storesIt() {
        CoordinatorTransaction tx = new CoordinatorTransaction("v");

        tx.recordVote("server-1", VoteResult.YES);

        assertThat(tx.getVotes()).containsEntry("server-1", VoteResult.YES);
    }

    @Test
    void recordVote_givenMultipleParticipants_storesAll() {
        CoordinatorTransaction tx = new CoordinatorTransaction("v");

        tx.recordVote("server-1", VoteResult.YES);
        tx.recordVote("server-2", VoteResult.NO);

        assertThat(tx.getVotes()).hasSize(2);
    }

    @Test
    void getVotes_givenVotes_returnsImmutableSnapshot() {
        CoordinatorTransaction tx = new CoordinatorTransaction("v");
        tx.recordVote("server-1", VoteResult.YES);

        var votes = tx.getVotes();

        assertThat(votes).containsEntry("server-1", VoteResult.YES);
    }

    @Test
    void applyDecision_givenCommit_setsStatusAndDecidedAt() {
        CoordinatorTransaction tx = new CoordinatorTransaction("v");

        tx.applyDecision(TransactionStatus.COMMITTED);

        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.COMMITTED);
        assertThat(tx.getDecidedAt()).isNotNull();
    }

    @Test
    void applyDecision_givenAbort_setsStatusToAborted() {
        CoordinatorTransaction tx = new CoordinatorTransaction("v");

        tx.applyDecision(TransactionStatus.ABORTED);

        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.ABORTED);
    }

    @Test
    void setStatus_givenNewStatus_overridesWithoutTimestamp() {
        CoordinatorTransaction tx = new CoordinatorTransaction("v");

        tx.setStatus(TransactionStatus.PREPARING);

        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.PREPARING);
        assertThat(tx.getDecidedAt()).isNull();
    }

    @Test
    void constructor_givenTwoInstances_haveDistinctIds() {
        CoordinatorTransaction tx1 = new CoordinatorTransaction("a");
        CoordinatorTransaction tx2 = new CoordinatorTransaction("b");

        assertThat(tx1.getId()).isNotEqualTo(tx2.getId());
    }
}
