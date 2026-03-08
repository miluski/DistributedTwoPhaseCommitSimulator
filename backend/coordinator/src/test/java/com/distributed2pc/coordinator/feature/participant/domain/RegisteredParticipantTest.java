package com.distributed2pc.coordinator.feature.participant.domain;

import com.distributed2pc.common.enums.NodeStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RegisteredParticipant}.
 */
class RegisteredParticipantTest {

    @Test
    void baseUrl_givenHostAndPort_returnsHttpsUrl() {
        RegisteredParticipant participant =
                new RegisteredParticipant("server-1", "participant-1", 8444, NodeStatus.ONLINE);

        assertThat(participant.baseUrl()).isEqualTo("https://participant-1:8444");
    }

    @Test
    void withStatus_givenNewStatus_returnsUpdatedCopy() {
        RegisteredParticipant participant =
                new RegisteredParticipant("server-1", "localhost", 9001, NodeStatus.ONLINE);

        RegisteredParticipant updated = participant.withStatus(NodeStatus.CRASHED);

        assertThat(updated.status()).isEqualTo(NodeStatus.CRASHED);
        assertThat(updated.serverId()).isEqualTo("server-1");
    }
}
