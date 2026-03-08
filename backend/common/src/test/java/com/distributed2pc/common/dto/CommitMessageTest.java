package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CommitMessage}.
 */
class CommitMessageTest {

    @Test
    void constructor_givenTransactionId_storesIt() {
        UUID id = UUID.randomUUID();

        CommitMessage msg = new CommitMessage(id);

        assertThat(msg.transactionId()).isEqualTo(id);
    }

    @Test
    void equals_givenSameTransactionId_returnsTrue() {
        UUID id = UUID.randomUUID();

        assertThat(new CommitMessage(id)).isEqualTo(new CommitMessage(id));
    }

    @Test
    void equals_givenDifferentTransactionId_returnsFalse() {
        assertThat(new CommitMessage(UUID.randomUUID()))
                .isNotEqualTo(new CommitMessage(UUID.randomUUID()));
    }

    @Test
    void hashCode_givenSameTransactionId_returnsSameValue() {
        UUID id = UUID.randomUUID();

        assertThat(new CommitMessage(id).hashCode())
                .hasSameHashCodeAs(new CommitMessage(id).hashCode());
    }

    @Test
    void toString_givenInstance_containsTransactionId() {
        UUID id = UUID.randomUUID();

        assertThat(new CommitMessage(id).toString()).contains(id.toString());
    }
}
