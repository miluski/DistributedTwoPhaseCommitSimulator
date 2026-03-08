package com.distributed2pc.coordinator.feature.fault.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link DelayedDecisionFaultStrategy}.
 */
class DelayedDecisionFaultStrategyTest {

    DelayedDecisionFaultStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DelayedDecisionFaultStrategy();
    }

    @Test
    void isActive_givenNewInstance_returnsFalse() {
        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void getDelayMs_givenInactiveFault_returnsZero() {
        assertThat(strategy.getDelayMs()).isZero();
    }

    @Test
    void activate_givenNoParameters_usesDefaultDelay() {
        strategy.activate(Map.of());

        assertThat(strategy.isActive()).isTrue();
        assertThat(strategy.getDelayMs()).isEqualTo(3_000L);
    }

    @Test
    void activate_givenCustomDelayMs_usesProvidedValue() {
        strategy.activate(Map.of("delayMs", 1500L));

        assertThat(strategy.getDelayMs()).isEqualTo(1500L);
    }

    @Test
    void deactivate_givenActiveFault_setsInactive() {
        strategy.activate(Map.of("delayMs", 500L));

        strategy.deactivate();

        assertThat(strategy.isActive()).isFalse();
        assertThat(strategy.getDelayMs()).isZero();
    }

    @ParameterizedTest
    @MethodSource("delayVariants")
    void activate_givenVariousNumberTypes_extractsDelayCorrectly(Object delayValue) {
        strategy.activate(Map.of("delayMs", delayValue));

        assertThat(strategy.getDelayMs()).isEqualTo(1000L);
    }

    static Stream<Object> delayVariants() {
        return Stream.of(1000, 1000L, 1000.0f, 1000.0);
    }
}
