package com.distributed2pc.participant.feature.fault.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ForceAbortVoteFaultStrategy}.
 */
class ForceAbortVoteFaultStrategyTest {

    ForceAbortVoteFaultStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ForceAbortVoteFaultStrategy();
    }

    @Test
    void isActive_givenNewInstance_returnsFalse() {
        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void consumeNoVote_givenInactiveStrategy_returnsFalse() {
        assertThat(strategy.consumeNoVote()).isFalse();
    }

    @Test
    void activate_givenNoParameters_allowsOneNoVote() {
        strategy.activate(Map.of());

        assertThat(strategy.consumeNoVote()).isTrue();
        assertThat(strategy.consumeNoVote()).isFalse();
    }

    @Test
    void activate_givenForNextNTwo_allowsTwoNoVotes() {
        strategy.activate(Map.of("forNextN", 2));

        assertThat(strategy.consumeNoVote()).isTrue();
        assertThat(strategy.consumeNoVote()).isTrue();
        assertThat(strategy.consumeNoVote()).isFalse();
    }

    @Test
    void consumeNoVote_givenQuotaExhausted_deactivatesStrategy() {
        strategy.activate(Map.of());
        strategy.consumeNoVote();

        assertThat(strategy.isActive()).isFalse();
    }

    @Test
    void deactivate_givenActiveFault_resetsToInactive() {
        strategy.activate(Map.of("forNextN", 5));

        strategy.deactivate();

        assertThat(strategy.isActive()).isFalse();
        assertThat(strategy.consumeNoVote()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("countVariants")
    void activate_givenVariousNumberTypes_extractsCountCorrectly(Object countValue) {
        strategy.activate(Map.of("forNextN", countValue));

        assertThat(strategy.isActive()).isTrue();
        assertThat(strategy.consumeNoVote()).isTrue();
    }

    static Stream<Object> countVariants() {
        return Stream.of(1, 1L, 1.0f, 1.0);
    }
}
