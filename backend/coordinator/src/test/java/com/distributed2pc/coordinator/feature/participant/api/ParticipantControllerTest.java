package com.distributed2pc.coordinator.feature.participant.api;

import com.distributed2pc.common.dto.ParticipantRegistrationDto;
import com.distributed2pc.common.enums.NodeStatus;
import com.distributed2pc.coordinator.feature.participant.application.ParticipantRegistrationService;
import com.distributed2pc.coordinator.feature.participant.domain.RegisteredParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ParticipantController}.
 */
@ExtendWith(MockitoExtension.class)
class ParticipantControllerTest {

    @Mock
    ParticipantRegistrationService registrationService;

    ParticipantController controller;

    @BeforeEach
    void setUp() {
        controller = new ParticipantController(registrationService);
    }

    @Test
    void register_givenValidDto_delegatesToRegistrationService() {
        ParticipantRegistrationDto dto = new ParticipantRegistrationDto("server-1", "localhost", 9001);

        controller.register(dto);

        verify(registrationService).register(dto);
    }

    @Test
    void listAll_givenRegisteredParticipants_returnsDelegatedList() {
        RegisteredParticipant p1 = new RegisteredParticipant("server-1", "localhost", 9001, NodeStatus.ONLINE);
        RegisteredParticipant p2 = new RegisteredParticipant("server-2", "localhost", 9002, NodeStatus.CRASHED);
        when(registrationService.listAll()).thenReturn(List.of(p1, p2));

        List<RegisteredParticipant> result = controller.listAll();

        assertThat(result).containsExactly(p1, p2);
    }

    @Test
    void listAll_givenNoParticipants_returnsEmptyList() {
        when(registrationService.listAll()).thenReturn(List.of());

        List<RegisteredParticipant> result = controller.listAll();

        assertThat(result).isEmpty();
    }
}
