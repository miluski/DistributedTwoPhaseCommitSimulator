package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PrepareMessage}.
 */
class PrepareMessageTest {

    @Test
    void constructor_givenFields_storesThemCorrectly() {
        UUID id = UUID.randomUUID();

        PrepareMessage msg = new PrepareMessage(id, "payload");

        assertThat(msg.transactionId()).isEqualTo(id);
        assertThat(msg.value()).isEqualTo("payload");
    }

    @Test
    void equals_givenSameValues_returnsTrue() {
        UUID id = UUID.randomUUID();

        assertThat(new PrepareMessage(id, "value"))
                .isEqualTo(new PrepareMessage(id, "value"));
    }

    @Test
    void equals_givenDifferentValue_returnsFalse() {
        UUID id = UUID.randomUUID();

        assertThat(new PrepareMessage(id, "a"))
                .isNotEqualTo(new PrepareMessage(id, "b"));
    }

    @Test
    void equals_givenDifferentTransactionId_returnsFalse() {
        assertThat(new PrepareMessage(UUID.randomUUID(), "v"))
                .isNotEqualTo(new PrepareMessage(UUID.randomUUID(), "v"));
    }

    @Test
    void hashCode_givenSameValues_returnsSameHashCode() {
        UUID id = UUID.randomUUID();

        assertThat(new PrepareMessage(id, "v").hashCode())
                .hasSameHashCodeAs(new PrepareMessage(id, "v").hashCode());
    }

    @Test
    void toString_givenInstance_containsValue() {
        UUID id = UUID.randomUUID();

        assertThat(new PrepareMessage(id, "payload").toString()).contains("payload");
    }
}
