package com.distributed2pc.participant.feature.fault.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CrashFaultStrategy}.
 */
class CrashFaultStrategyTest {

    CrashFaultStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new CrashFaultStrategy();
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
    void activate_givenArbitraryParameters_ignoresThem() {
        strategy.activate(Map.of("someKey", "someValue"));

        assertThat(strategy.isActive()).isTrue();
    }
}
