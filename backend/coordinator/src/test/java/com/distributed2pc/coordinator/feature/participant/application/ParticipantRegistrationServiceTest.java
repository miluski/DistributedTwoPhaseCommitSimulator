package com.distributed2pc.coordinator.feature.participant.application;

import com.distributed2pc.common.dto.ParticipantRegistrationDto;
import com.distributed2pc.common.enums.NodeStatus;
import com.distributed2pc.coordinator.feature.participant.domain.ParticipantRepository;
import com.distributed2pc.coordinator.feature.participant.domain.RegisteredParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ParticipantRegistrationService}.
 */
@ExtendWith(MockitoExtension.class)
class ParticipantRegistrationServiceTest {

    @Mock
    ParticipantRepository repository;

    ParticipantRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new ParticipantRegistrationService(repository);
    }

    @Test
    void register_givenValidDto_savesParticipantWithOnlineStatus() {
        ParticipantRegistrationDto dto = new ParticipantRegistrationDto("server-1", "localhost", 9001);

        service.register(dto);

        ArgumentCaptor<RegisteredParticipant> captor = ArgumentCaptor.forClass(RegisteredParticipant.class);
        verify(repository).save(captor.capture());
        RegisteredParticipant saved = captor.getValue();
        assertThat(saved.serverId()).isEqualTo("server-1");
        assertThat(saved.host()).isEqualTo("localhost");
        assertThat(saved.port()).isEqualTo(9001);
        assertThat(saved.status()).isEqualTo(NodeStatus.ONLINE);
    }

    @Test
    void listAll_givenRegisteredParticipants_returnsDelegatedList() {
        RegisteredParticipant p1 = new RegisteredParticipant("server-1", "localhost", 9001, NodeStatus.ONLINE);
        RegisteredParticipant p2 = new RegisteredParticipant("server-2", "localhost", 9002, NodeStatus.CRASHED);
        when(repository.findAll()).thenReturn(List.of(p1, p2));

        List<RegisteredParticipant> result = service.listAll();

        assertThat(result).containsExactly(p1, p2);
    }

    @Test
    void markCrashed_givenServerId_updatesStatusToCrashed() {
        service.markCrashed("server-1");

        verify(repository).updateStatus("server-1", NodeStatus.CRASHED);
    }

    @Test
    void markOnline_givenServerId_updatesStatusToOnline() {
        service.markOnline("server-1");

        verify(repository).updateStatus("server-1", NodeStatus.ONLINE);
    }
}
