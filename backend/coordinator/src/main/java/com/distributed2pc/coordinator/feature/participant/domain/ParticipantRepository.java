package com.distributed2pc.coordinator.feature.participant.domain;

import com.distributed2pc.common.enums.NodeStatus;
import java.util.List;
import java.util.Optional;

/**
 * Port (interface) for reading and updating registered participant data.
 *
 * <p>Implementations live in the {@code infrastructure} sub-package.
 * Application services depend only on this interface.
 */
public interface ParticipantRepository {

 /**
  * Persist a newly registered participant.
  *
  * @param participant the participant to store.
  */
 void save(RegisteredParticipant participant);

 /**
  * Returns all currently registered participants.
  *
  * @return immutable snapshot of all participants.
  */
 List<RegisteredParticipant> findAll();

 /**
  * Look up a participant by its stable server identifier.
  *
  * @param serverId the stable server ID (e.g. "server-1").
  * @return the participant wrapped in an Optional, or empty if not found.
  */
 Optional<RegisteredParticipant> findById(String serverId);

 /**
  * Update the online/offline status of a participant.
  *
  * @param serverId the server whose status changes.
  * @param status   the new status.
  */
 void updateStatus(String serverId, NodeStatus status);
}
