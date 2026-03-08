package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ParticipantRegistrationDto}.
 */
class ParticipantRegistrationDtoTest {

    @Test
    void constructor_givenFields_storesThemCorrectly() {
        ParticipantRegistrationDto dto = new ParticipantRegistrationDto("server-1", "localhost", 8081);

        assertThat(dto.serverId()).isEqualTo("server-1");
        assertThat(dto.host()).isEqualTo("localhost");
        assertThat(dto.port()).isEqualTo(8081);
    }

    @Test
    void equals_givenSameValues_returnsTrue() {
        assertThat(new ParticipantRegistrationDto("server-1", "localhost", 8081))
                .isEqualTo(new ParticipantRegistrationDto("server-1", "localhost", 8081));
    }

    @Test
    void equals_givenDifferentServerId_returnsFalse() {
        assertThat(new ParticipantRegistrationDto("server-1", "localhost", 8081))
                .isNotEqualTo(new ParticipantRegistrationDto("server-2", "localhost", 8081));
    }

    @Test
    void equals_givenDifferentPort_returnsFalse() {
        assertThat(new ParticipantRegistrationDto("s1", "host", 8080))
                .isNotEqualTo(new ParticipantRegistrationDto("s1", "host", 8081));
    }

    @Test
    void hashCode_givenSameValues_returnsSameHashCode() {
        assertThat(new ParticipantRegistrationDto("s1", "host", 80).hashCode())
                .hasSameHashCodeAs(new ParticipantRegistrationDto("s1", "host", 80).hashCode());
    }

    @Test
    void toString_givenInstance_containsServerId() {
        ParticipantRegistrationDto dto = new ParticipantRegistrationDto("server-1", "localhost", 8081);

        assertThat(dto.toString()).contains("server-1");
    }
}
