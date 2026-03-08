package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.common.enums.VoteResult;

/**
 * Unit tests for {@link TransactionResponse}.
 */
class TransactionResponseTest {

    @Test
    void constructor_givenAllFields_storesThemCorrectly() {
        UUID id = UUID.randomUUID();
        Instant initiated = Instant.now();
        Instant decided = initiated.plusSeconds(1);
        Map<String, VoteResult> votes = Map.of("server-1", VoteResult.YES);

        TransactionResponse response = new TransactionResponse(
                id, TransactionStatus.COMMITTED, "v", initiated, decided, votes);

        assertThat(response.transactionId()).isEqualTo(id);
        assertThat(response.status()).isEqualTo(TransactionStatus.COMMITTED);
        assertThat(response.value()).isEqualTo("v");
        assertThat(response.initiatedAt()).isEqualTo(initiated);
        assertThat(response.decidedAt()).isEqualTo(decided);
        assertThat(response.votes()).containsEntry("server-1", VoteResult.YES);
    }

    @Test
    void constructor_givenNullDecidedAt_allowsNullDecidedAt() {
        UUID id = UUID.randomUUID();

        TransactionResponse response = new TransactionResponse(
                id, TransactionStatus.INITIATED, "v", Instant.now(), null, Map.of());

        assertThat(response.decidedAt()).isNull();
    }

    @Test
    void equals_givenSameValues_returnsTrue() {
        UUID id = UUID.randomUUID();
        Instant ts = Instant.now();

        assertThat(new TransactionResponse(
                id, TransactionStatus.COMMITTED, "v", ts, null, Map.of()))
                .isEqualTo(new TransactionResponse(
                        id, TransactionStatus.COMMITTED, "v", ts, null, Map.of()));
    }

    @Test
    void equals_givenDifferentStatus_returnsFalse() {
        UUID id = UUID.randomUUID();
        Instant ts = Instant.now();

        assertThat(new TransactionResponse(
                id, TransactionStatus.COMMITTED, "v", ts, null, Map.of()))
                .isNotEqualTo(new TransactionResponse(
                        id, TransactionStatus.ABORTED, "v", ts, null, Map.of()));
    }

    @Test
    void hashCode_givenSameValues_returnsSameHashCode() {
        UUID id = UUID.randomUUID();
        Instant ts = Instant.now();

        assertThat(new TransactionResponse(
                id, TransactionStatus.COMMITTED, "v", ts, null, Map.of()).hashCode())
                .hasSameHashCodeAs(new TransactionResponse(
                        id, TransactionStatus.COMMITTED, "v", ts, null, Map.of()).hashCode());
    }
}
