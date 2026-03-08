package com.distributed2pc.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link EventType}.
 */
class EventTypeTest {

    @Test
    void values_containsNineteenConstants() {
        assertThat(EventType.values()).hasSize(19);
    }

    @Test
    void values_containsTransactionRelatedConstants() {
        assertThat(EventType.values()).contains(
                EventType.TRANSACTION_STARTED,
                EventType.PREPARE_SENT,
                EventType.VOTE_RECEIVED,
                EventType.ALL_VOTES_COLLECTED,
                EventType.DECISION_MADE,
                EventType.COMMIT_SENT,
                EventType.ABORT_SENT,
                EventType.TRANSACTION_COMPLETED,
                EventType.TRANSACTION_TIMED_OUT);
    }

    @Test
    void values_containsNodeAndFaultRelatedConstants() {
        assertThat(EventType.values()).contains(
                EventType.COORDINATOR_CRASHED,
                EventType.COORDINATOR_RECOVERED,
                EventType.PARTICIPANT_CRASHED,
                EventType.PARTICIPANT_RECOVERED,
                EventType.ELECTION_STARTED,
                EventType.ELECTION_RESULT,
                EventType.PEER_CONSULTED,
                EventType.FAULT_INJECTED,
                EventType.FAULT_CLEARED,
                EventType.PARTICIPANT_REGISTERED);
    }

    @ParameterizedTest
    @MethodSource("provideAllConstantNames")
    void valueOf_givenValidName_returnsConstant(String name) {
        assertThat(EventType.valueOf(name)).isNotNull();
    }

    static Stream<String> provideAllConstantNames() {
        return Stream.of(EventType.values()).map(Enum::name);
    }
}
