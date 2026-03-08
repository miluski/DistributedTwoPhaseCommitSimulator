package com.distributed2pc.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link FaultDefault}.
 */
class FaultDefaultTest {

    @Test
    void values_containsFiveConstants() {
        assertThat(FaultDefault.values()).hasSize(5);
    }

    @Test
    void networkDelayMs_givenDefaultValue_returns1000() {
        assertThat(FaultDefault.NETWORK_DELAY_MS.getValue()).isEqualTo(1_000L);
    }

    @Test
    void delayedDecisionMs_givenDefaultValue_returns3000() {
        assertThat(FaultDefault.DELAYED_DECISION_MS.getValue()).isEqualTo(3_000L);
    }

    @Test
    void partialSendCount_givenDefaultValue_returns1() {
        assertThat(FaultDefault.PARTIAL_SEND_COUNT.getValue()).isEqualTo(1L);
        assertThat(FaultDefault.PARTIAL_SEND_COUNT.getIntValue()).isEqualTo(1);
    }

    @Test
    void intermittentChancePercent_givenDefaultValue_returns50() {
        assertThat(FaultDefault.INTERMITTENT_CHANCE_PERCENT.getValue()).isEqualTo(50L);
        assertThat(FaultDefault.INTERMITTENT_CHANCE_PERCENT.getIntValue()).isEqualTo(50);
    }

    @Test
    void transientDurationMs_givenDefaultValue_returns2000() {
        assertThat(FaultDefault.TRANSIENT_DURATION_MS.getValue()).isEqualTo(2_000L);
    }

    @ParameterizedTest
    @MethodSource("provideAllConstantNames")
    void valueOf_givenValidName_returnsConstant(String name) {
        assertThat(FaultDefault.valueOf(name)).isNotNull();
    }

    static Stream<String> provideAllConstantNames() {
        return Stream.of(FaultDefault.values()).map(Enum::name);
    }
}
