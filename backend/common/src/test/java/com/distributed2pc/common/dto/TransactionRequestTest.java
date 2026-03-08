package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TransactionRequest}.
 */
class TransactionRequestTest {

    @Test
    void constructor_givenValue_storesIt() {
        TransactionRequest request = new TransactionRequest("my-payload");

        assertThat(request.value()).isEqualTo("my-payload");
    }

    @Test
    void random_returnsNonNullNonEmptyValue() {
        TransactionRequest request = TransactionRequest.random();

        assertThat(request.value()).isNotNull().isNotEmpty();
    }

    @Test
    void random_calledTwice_returnsDifferentValues() {
        assertThat(TransactionRequest.random().value())
                .isNotEqualTo(TransactionRequest.random().value());
    }

    @Test
    void equals_givenSameValue_returnsTrue() {
        assertThat(new TransactionRequest("v")).isEqualTo(new TransactionRequest("v"));
    }

    @Test
    void equals_givenDifferentValue_returnsFalse() {
        assertThat(new TransactionRequest("a")).isNotEqualTo(new TransactionRequest("b"));
    }

    @Test
    void hashCode_givenSameValue_returnsSameHashCode() {
        assertThat(new TransactionRequest("v").hashCode())
                .hasSameHashCodeAs(new TransactionRequest("v").hashCode());
    }

    @Test
    void toString_givenInstance_containsValue() {
        assertThat(new TransactionRequest("payload").toString()).contains("payload");
    }
}
