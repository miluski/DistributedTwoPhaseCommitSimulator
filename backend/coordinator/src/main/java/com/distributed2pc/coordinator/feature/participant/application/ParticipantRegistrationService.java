package com.distributed2pc.coordinator.feature.participant.application;

import com.distributed2pc.common.dto.ParticipantRegistrationDto;
import com.distributed2pc.common.enums.NodeStatus;
import com.distributed2pc.coordinator.feature.participant.domain.ParticipantRepository;
import com.distributed2pc.coordinator.feature.participant.domain.RegisteredParticipant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handles participant registration and status management in the coordinator.
 *
 * <p>Participants call the registration endpoint at startup. This service persists
 * their address data so the coordinator can reach them during 2PC protocol rounds.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ParticipantRegistrationService {

 private final ParticipantRepository repository;

 /**
  * Registers or updates a participant's connection information.
  *
  * @param dto registration payload sent by the participant at startup.
  */
 public void register(ParticipantRegistrationDto dto) {
  RegisteredParticipant participant = new RegisteredParticipant(
    dto.serverId(), dto.host(), dto.port(), NodeStatus.ONLINE);
  repository.save(participant);
  log.info("Participant registered: {} at {}:{}", dto.serverId(), dto.host(), dto.port());
 }

 /**
  * Returns all currently registered participants.
  *
  * @return immutable list of registered participants.
  */
 public List<RegisteredParticipant> listAll() {
  return repository.findAll();
 }

 /**
  * Marks a participant as crashed (HTTP 503 received from it).
  *
  * @param serverId the server that appears to be down.
  */
 public void markCrashed(String serverId) {
  repository.updateStatus(serverId, NodeStatus.CRASHED);
  log.warn("Participant {} marked as CRASHED", serverId);
 }

 /**
  * Marks a participant as online (successfully responded after being considered crashed).
  *
  * @param serverId the server that has recovered.
  */
 public void markOnline(String serverId) {
  repository.updateStatus(serverId, NodeStatus.ONLINE);
  log.info("Participant {} marked as ONLINE", serverId);
 }
}
