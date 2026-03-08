package com.distributed2pc.coordinator.feature.transaction.domain;

import java.time.Duration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Timeout values used when the coordinator communicates with participants
 * over HTTP during the Two-Phase Commit protocol.
 */
@Getter
@RequiredArgsConstructor
public enum ProtocolTimeout {

    /**
     * Maximum wait time for a participant to respond to a PREPARE, COMMIT or
     * ABORT message (5 seconds).
     */
    PARTICIPANT_REQUEST(Duration.ofSeconds(5));

    private final Duration duration;
}
