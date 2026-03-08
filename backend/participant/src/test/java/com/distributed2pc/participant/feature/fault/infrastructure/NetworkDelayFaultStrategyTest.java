package com.distributed2pc.participant.feature.fault.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NetworkDelayFaultStrategy}.
 */
class NetworkDelayFaultStrategyTest {

    NetworkDelayFaultStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new NetworkDelayFaultStrategy();
    }

    @Test
    void isActive_givenNewInstance_returnsFalse() {
        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void activate_givenNoParameters_usesDefaultDelay() {
        strategy.activate(Map.of());

        assertThat(strategy.isActive()).isTrue();
        assertThat(strategy.getDelayMs()).isEqualTo(1000L);
    }

    @Test
    void activate_givenCustomDelayMs_usesProvidedValue() {
        strategy.activate(Map.of("delayMs", 2500L));

        assertThat(strategy.getDelayMs()).isEqualTo(2500L);
    }

    @Test
    void deactivate_givenActiveFault_setsInactive() {
        strategy.activate(Map.of("delayMs", 500L));

        strategy.deactivate();

        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void getDelayMs_givenActiveFault_returnsCurrentDelay() {
        strategy.activate(Map.of("delayMs", 800L));

        assertThat(strategy.getDelayMs()).isEqualTo(800L);
    }

    @Test
    void activate_givenNonNumberDelayMs_usesDefaultDelay() {
        strategy.activate(Map.of());

        assertThat(strategy.getDelayMs()).isEqualTo(1000L);
    }

    @ParameterizedTest
    @MethodSource("delayVariants")
    void activate_givenVariousNumberTypes_extractsDelayCorrectly(Object delayValue) {
        strategy.activate(Map.of("delayMs", delayValue));

        assertThat(strategy.getDelayMs()).isEqualTo(500L);
    }

    static Stream<Object> delayVariants() {
        return Stream.of(500, 500L, 500.0f, 500.0);
    }
}
