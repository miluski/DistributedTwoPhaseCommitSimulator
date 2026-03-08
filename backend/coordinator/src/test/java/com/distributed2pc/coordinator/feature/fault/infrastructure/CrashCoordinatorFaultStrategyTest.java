package com.distributed2pc.coordinator.feature.fault.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CrashCoordinatorFaultStrategy}.
 */
class CrashCoordinatorFaultStrategyTest {

    CrashCoordinatorFaultStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new CrashCoordinatorFaultStrategy();
    }

    @Test
    void isActive_givenNewInstance_returnsFalse() {
        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void activate_givenCall_setsActiveFlagTrue() {
        strategy.activate(Map.of());

        assertThat(strategy.isActive()).isTrue();
    }

    @Test
    void deactivate_givenActiveFault_setsActiveFlagFalse() {
        strategy.activate(Map.of());

        strategy.deactivate();

        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void activate_givenParameters_ignoresParameters() {
        strategy.activate(Map.of("unexpected", "value"));

        assertThat(strategy.isActive()).isTrue();
    }
}
