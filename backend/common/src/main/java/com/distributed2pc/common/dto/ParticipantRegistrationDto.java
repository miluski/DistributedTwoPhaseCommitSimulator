package com.distributed2pc.common.dto;

/**
 * Registration payload sent by a participant to the coordinator at startup.
 *
 * @param serverId Stable, unique identifier for this participant (e.g.
 *                 "server-1").
 * @param host     Hostname or IP the coordinator should use to reach this
 *                 participant.
 * @param port     HTTP port the participant is listening on.
 */
public record ParticipantRegistrationDto(String serverId, String host, int port) {
}
