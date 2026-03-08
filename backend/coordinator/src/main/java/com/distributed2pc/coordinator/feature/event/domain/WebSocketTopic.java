package com.distributed2pc.coordinator.feature.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * STOMP WebSocket topic paths to which the coordinator publishes events.
 *
 * <p>
 * All connected React UI clients subscribe to these topics to receive
 * real-time system-event notifications.
 */
@Getter
@RequiredArgsConstructor
public enum WebSocketTopic {

    /**
     * Topic for all protocol and status events consumed by the React UI.
     */
    EVENTS("/topic/events");

    private final String path;
}
