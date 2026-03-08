package com.distributed2pc.coordinator.feature.participant.infrastructure;

import com.distributed2pc.common.enums.NodeStatus;
import com.distributed2pc.coordinator.feature.participant.domain.RegisteredParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link InMemoryParticipantRepository}.
 */
class InMemoryParticipantRepositoryTest {

    InMemoryParticipantRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParticipantRepository();
    }

    @Test
    void save_givenNewParticipant_storesIt() {
        RegisteredParticipant p = new RegisteredParticipant("server-1", "localhost", 9001, NodeStatus.ONLINE);

        repository.save(p);

        assertThat(repository.findById("server-1")).contains(p);
    }

    @Test
    void save_givenExistingId_overwritesPrevious() {
        repository.save(new RegisteredParticipant("server-1", "localhost", 9001, NodeStatus.ONLINE));
        RegisteredParticipant updated = new RegisteredParticipant("server-1", "host2", 9002, NodeStatus.CRASHED);

        repository.save(updated);

        assertThat(repository.findById("server-1")).contains(updated);
    }

    @Test
    void findAll_givenMultipleParticipants_returnsAll() {
        RegisteredParticipant p1 = new RegisteredParticipant("server-1", "localhost", 9001, NodeStatus.ONLINE);
        RegisteredParticipant p2 = new RegisteredParticipant("server-2", "localhost", 9002, NodeStatus.ONLINE);
        repository.save(p1);
        repository.save(p2);

        List<RegisteredParticipant> all = repository.findAll();

        assertThat(all).containsExactlyInAnyOrder(p1, p2);
    }

    @Test
    void findAll_givenEmptyRepository_returnsEmptyList() {
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void findById_givenUnknownId_returnsEmpty() {
        assertThat(repository.findById("nonexistent")).isEmpty();
    }

    @Test
    void updateStatus_givenExistingParticipant_changesStatus() {
        repository.save(new RegisteredParticipant("server-1", "localhost", 9001, NodeStatus.ONLINE));

        repository.updateStatus("server-1", NodeStatus.CRASHED);

        Optional<RegisteredParticipant> result = repository.findById("server-1");
        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(NodeStatus.CRASHED);
    }

    @Test
    void updateStatus_givenUnknownId_doesNothing() {
        repository.updateStatus("ghost", NodeStatus.CRASHED);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void updateStatus_givenExistingParticipant_preservesOtherFields() {
        repository.save(new RegisteredParticipant("server-1", "my-host", 9001, NodeStatus.ONLINE));

        repository.updateStatus("server-1", NodeStatus.CRASHED);

        RegisteredParticipant result = repository.findById("server-1").orElseThrow();
        assertThat(result.host()).isEqualTo("my-host");
        assertThat(result.port()).isEqualTo(9001);
    }
}
