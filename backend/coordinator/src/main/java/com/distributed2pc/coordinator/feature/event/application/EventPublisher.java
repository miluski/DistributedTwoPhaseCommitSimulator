package com.distributed2pc.coordinator.feature.event.application;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.distributed2pc.common.dto.SystemEventDto;
import com.distributed2pc.coordinator.feature.event.domain.WebSocketTopic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes {@link SystemEventDto} events to all connected WebSocket
 * subscribers.
 *
 * <p>
 * All participants and coordinator logic call this service to push real-time
 * updates to the React UI. The event is serialised to JSON and broadcast to
 * {@code /topic/events}.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcasts a system event to all UI subscribers.
     *
     * @param event the event to broadcast; must not be null.
     */
    public void publish(SystemEventDto event) {
        log.debug("Publishing event: {} txId={}", event.eventType(), event.transactionId());
        messagingTemplate.convertAndSend(WebSocketTopic.EVENTS.getPath(), event);
    }
}
