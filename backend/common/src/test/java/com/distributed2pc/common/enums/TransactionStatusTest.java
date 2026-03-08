package com.distributed2pc.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link TransactionStatus}.
 */
class TransactionStatusTest {

    @Test
    void values_containsFiveConstants() {
        assertThat(TransactionStatus.values()).hasSize(5);
    }

    @Test
    void values_containsAllExpectedConstants() {
        assertThat(TransactionStatus.values()).contains(
                TransactionStatus.INITIATED,
                TransactionStatus.PREPARING,
                TransactionStatus.COMMITTED,
                TransactionStatus.ABORTED,
                TransactionStatus.UNCERTAIN);
    }

    @ParameterizedTest
    @MethodSource("provideAllConstantNames")
    void valueOf_givenValidName_returnsConstant(String name) {
        assertThat(TransactionStatus.valueOf(name)).isNotNull();
    }

    static Stream<String> provideAllConstantNames() {
        return Stream.of(TransactionStatus.values()).map(Enum::name);
    }
}
