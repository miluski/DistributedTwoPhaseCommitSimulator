package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AbortMessage}.
 */
class AbortMessageTest {

    @Test
    void constructor_givenTransactionId_storesIt() {
        UUID id = UUID.randomUUID();

        AbortMessage msg = new AbortMessage(id);

        assertThat(msg.transactionId()).isEqualTo(id);
    }

    @Test
    void equals_givenSameTransactionId_returnsTrue() {
        UUID id = UUID.randomUUID();

        assertThat(new AbortMessage(id)).isEqualTo(new AbortMessage(id));
    }

    @Test
    void equals_givenDifferentTransactionId_returnsFalse() {
        assertThat(new AbortMessage(UUID.randomUUID()))
                .isNotEqualTo(new AbortMessage(UUID.randomUUID()));
    }

    @Test
    void hashCode_givenSameTransactionId_returnsSameValue() {
        UUID id = UUID.randomUUID();

        assertThat(new AbortMessage(id).hashCode())
                .hasSameHashCodeAs(new AbortMessage(id).hashCode());
    }

    @Test
    void toString_givenInstance_containsTransactionId() {
        UUID id = UUID.randomUUID();

        assertThat(new AbortMessage(id).toString()).contains(id.toString());
    }
}
