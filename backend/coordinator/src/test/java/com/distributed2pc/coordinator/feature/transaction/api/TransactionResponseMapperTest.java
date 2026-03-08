package com.distributed2pc.coordinator.feature.transaction.api;

import com.distributed2pc.common.dto.TransactionResponse;
import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.common.enums.VoteResult;
import com.distributed2pc.coordinator.feature.transaction.domain.CoordinatorTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TransactionResponseMapper}.
 */
class TransactionResponseMapperTest {

    TransactionResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionResponseMapper();
    }

    @Test
    void toResponse_givenInitiatedTransaction_mapsAllFields() {
        CoordinatorTransaction tx = new CoordinatorTransaction("payload-value");

        TransactionResponse response = mapper.toResponse(tx);

        assertThat(response.transactionId()).isEqualTo(tx.getId());
        assertThat(response.value()).isEqualTo("payload-value");
        assertThat(response.status()).isEqualTo(TransactionStatus.INITIATED);
        assertThat(response.initiatedAt()).isEqualTo(tx.getInitiatedAt());
        assertThat(response.decidedAt()).isNull();
        assertThat(response.votes()).isEmpty();
    }

    @Test
    void toResponse_givenCommittedTransaction_mapsDecidedAtAndStatus() {
        CoordinatorTransaction tx = new CoordinatorTransaction("value");
        tx.applyDecision(TransactionStatus.COMMITTED);

        TransactionResponse response = mapper.toResponse(tx);

        assertThat(response.status()).isEqualTo(TransactionStatus.COMMITTED);
        assertThat(response.decidedAt()).isNotNull();
    }

    @Test
    void toResponse_givenTransactionWithVotes_includesVoteMap() {
        CoordinatorTransaction tx = new CoordinatorTransaction("value");
        tx.recordVote("server-1", VoteResult.YES);
        tx.recordVote("server-2", VoteResult.NO);

        TransactionResponse response = mapper.toResponse(tx);

        assertThat(response.votes()).containsEntry("server-1", VoteResult.YES);
        assertThat(response.votes()).containsEntry("server-2", VoteResult.NO);
    }
}
