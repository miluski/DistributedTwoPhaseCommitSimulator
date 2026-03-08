package com.distributed2pc.participant.feature.fault.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link IntermittentFaultStrategy}.
 */
class IntermittentFaultStrategyTest {

    IntermittentFaultStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new IntermittentFaultStrategy();
    }

    @Test
    void isActive_givenInitialState_returnsFalse() {
        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void isActive_givenDisarmed_alwaysReturnsFalse() {
        strategy.activate(Map.of("chancePercent", 100));
        strategy.deactivate();

        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void isActive_givenZeroChance_neverActivates() {
        strategy.activate(Map.of("chancePercent", 0));

        for (int i = 0; i < 100; i++) {
            assertThat(strategy.isActive()).isFalse();
        }
    }

    @RepeatedTest(50)
    void isActive_givenHundredPercent_alwaysActivates() {
        strategy.activate(Map.of("chancePercent", 100));

        assertThat(strategy.isActive()).isTrue();
    }

    @Test
    void activate_givenNoChanceParam_usesDefaultFiftyPercent() {
        strategy.activate(Map.of());

        long trueCount = 0;
        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            if (strategy.isActive()) trueCount++;
        }

        double rate = (double) trueCount / iterations;
        assertThat(rate).isBetween(0.3, 0.7);
    }

    @Test
    void deactivate_givenArmedWithFullChance_resetsChanceToDefault() {
        strategy.activate(Map.of("chancePercent", 100));
        strategy.deactivate();
        strategy.activate(Map.of());

        long trueCount = 0;
        for (int i = 0; i < 1000; i++) {
            if (strategy.isActive()) trueCount++;
        }
        assertThat(trueCount).isGreaterThan(0).isLessThan(1000);
    }
}
