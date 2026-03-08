package com.distributed2pc.coordinator.feature.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link ProtocolTimeout}.
 */
class ProtocolTimeoutTest {

    @Test
    void values_containsOneConstant() {
        assertThat(ProtocolTimeout.values()).hasSize(1);
    }

    @Test
    void participantRequest_givenGetDuration_returnsFiveSeconds() {
        assertThat(ProtocolTimeout.PARTICIPANT_REQUEST.getDuration())
                .isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void participantRequest_givenGetDuration_isPositive() {
        assertThat(ProtocolTimeout.PARTICIPANT_REQUEST.getDuration().isNegative()).isFalse();
        assertThat(ProtocolTimeout.PARTICIPANT_REQUEST.getDuration().isZero()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideAllConstantNames")
    void valueOf_givenValidName_returnsConstant(String name) {
        assertThat(ProtocolTimeout.valueOf(name)).isNotNull();
    }

    static Stream<String> provideAllConstantNames() {
        return Stream.of(ProtocolTimeout.values()).map(Enum::name);
    }
}
