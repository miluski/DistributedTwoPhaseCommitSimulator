package com.distributed2pc.participant.feature.fault.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TransientFaultStrategy}.
 */
class TransientFaultStrategyTest {

    TransientFaultStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new TransientFaultStrategy();
    }

    @Test
    void isActive_givenInitialState_returnsFalse() {
        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void activate_givenactivation_becomesActive() {
        strategy.activate(Map.of("durationMs", 60_000L));

        assertThat(strategy.isActive()).isTrue();
    }

    @Test
    void deactivate_givenActiveState_becomesInactive() {
        strategy.activate(Map.of("durationMs", 60_000L));

        strategy.deactivate();

        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void activate_givenNoParams_usesDefaultDuration() {
        strategy.activate(Map.of());

        assertThat(strategy.isActive()).isTrue();

        strategy.deactivate();
    }

    @Test
    void activate_givenVeryShortDuration_deactivatesAfterDelay() {
        strategy.activate(Map.of("durationMs", 50L));

        await().atMost(500, TimeUnit.MILLISECONDS).until(() -> !strategy.isActive());

        assertThat(strategy.isActive()).isFalse();
    }
}
