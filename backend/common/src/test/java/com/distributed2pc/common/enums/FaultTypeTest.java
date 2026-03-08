package com.distributed2pc.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link FaultType}.
 */
class FaultTypeTest {

    @Test
    void values_containsSixConstants() {
        assertThat(FaultType.values()).hasSize(6);
    }

    @Test
    void values_containsAllExpectedConstants() {
        assertThat(FaultType.values()).contains(
                FaultType.CRASH,
                FaultType.NETWORK_DELAY,
                FaultType.FORCE_ABORT_VOTE,
                FaultType.MESSAGE_LOSS,
                FaultType.TRANSIENT,
                FaultType.INTERMITTENT);
    }

    @ParameterizedTest
    @MethodSource("provideAllConstantNames")
    void valueOf_givenValidName_returnsConstant(String name) {
        assertThat(FaultType.valueOf(name)).isNotNull();
    }

    static Stream<String> provideAllConstantNames() {
        return Stream.of(FaultType.values()).map(Enum::name);
    }
}
