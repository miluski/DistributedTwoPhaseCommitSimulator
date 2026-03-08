package com.distributed2pc.coordinator.feature.participant.domain;

import com.distributed2pc.common.enums.NodeStatus;

/**
 * Runtime representation of a participant node registered with the coordinator.
 *
 * @param serverId Stable, unique identifier (e.g. "server-1").
 * @param host     Hostname or IP used by the coordinator to reach this participant.
 * @param port     HTTPS port the participant listens on.
 * @param status   Current reachability status.
 */
public record RegisteredParticipant(String serverId, String host, int port, NodeStatus status) {

 /**
  * Returns the HTTPS base URL for this participant.
  *
  * @return e.g. {@code https://participant-1:8444}
  */
 public String baseUrl() {
  return "https://" + host + ":" + port;
 }

 /**
  * Returns a copy of this participant with an updated status.
  *
  * @param newStatus the new node status.
  * @return updated participant record.
  */
 public RegisteredParticipant withStatus(NodeStatus newStatus) {
  return new RegisteredParticipant(serverId, host, port, newStatus);
 }
}
