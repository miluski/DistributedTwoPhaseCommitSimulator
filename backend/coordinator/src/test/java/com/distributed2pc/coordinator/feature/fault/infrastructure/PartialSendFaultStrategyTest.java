package com.distributed2pc.coordinator.feature.fault.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PartialSendFaultStrategy}.
 */
class PartialSendFaultStrategyTest {

    PartialSendFaultStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PartialSendFaultStrategy();
    }

    @Test
    void isActive_givenNewInstance_returnsFalse() {
        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void activate_givenDefaultCount_allowsOneSend() {
        strategy.activate(Map.of());

        assertThat(strategy.consumeSend()).isTrue();
        assertThat(strategy.consumeSend()).isFalse();
    }

    @Test
    void activate_givenCountTwo_allowsTwoSends() {
        strategy.activate(Map.of("count", 2));

        assertThat(strategy.consumeSend()).isTrue();
        assertThat(strategy.consumeSend()).isTrue();
        assertThat(strategy.consumeSend()).isFalse();
    }

    @Test
    void isActive_givenActivatedWithCountOne_trueBeforeConsume() {
        strategy.activate(Map.of());

        assertThat(strategy.isActive()).isTrue();
    }

    @Test
    void isActive_givenAllSendsConsumed_returnsFalse() {
        strategy.activate(Map.of());
        strategy.consumeSend();

        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void deactivate_givenActiveFault_resetsToInactive() {
        strategy.activate(Map.of("count", 5));

        strategy.deactivate();

        assertThat(strategy.isActive()).isFalse();
        assertThat(strategy.consumeSend()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("countVariants")
    void activate_givenVariousCountTypes_extractsCorrectly(Object count) {
        strategy.activate(Map.of("count", count));

        assertThat(strategy.isActive()).isTrue();
    }

    static Stream<Object> countVariants() {
        return Stream.of(1, 1L, 1.0f, 1.0);
    }
}
