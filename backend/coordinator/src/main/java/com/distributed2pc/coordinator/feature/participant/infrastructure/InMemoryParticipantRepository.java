package com.distributed2pc.coordinator.feature.participant.infrastructure;

import com.distributed2pc.common.enums.NodeStatus;
import com.distributed2pc.coordinator.feature.participant.domain.ParticipantRepository;
import com.distributed2pc.coordinator.feature.participant.domain.RegisteredParticipant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * Thread-safe in-memory implementation of {@link ParticipantRepository}.
 *
 * <p>Participants register themselves at startup. The map key is the stable {@code serverId}.
 */
@Repository
public class InMemoryParticipantRepository implements ParticipantRepository {

 private final Map<String, RegisteredParticipant> store = new ConcurrentHashMap<>();

 @Override
 public void save(RegisteredParticipant participant) {
  store.put(participant.serverId(), participant);
 }

 @Override
 public List<RegisteredParticipant> findAll() {
  return List.copyOf(store.values());
 }

 @Override
 public Optional<RegisteredParticipant> findById(String serverId) {
  return Optional.ofNullable(store.get(serverId));
 }

 @Override
 public void updateStatus(String serverId, NodeStatus status) {
  store.computeIfPresent(serverId,
    (id, existing) -> existing.withStatus(status));
 }
}
