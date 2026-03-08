package com.distributed2pc.coordinator.feature.event.application;

import com.distributed2pc.common.dto.SystemEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link EventPublisher}.
 */
@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    SimpMessagingTemplate messagingTemplate;

    EventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new EventPublisher(messagingTemplate);
    }

    @Test
    void publish_givenAnyEvent_sendsToEventsTopic() {
        SystemEventDto event = new SystemEventDto(
                com.distributed2pc.common.enums.EventType.TRANSACTION_STARTED,
                UUID.randomUUID(),
                "coordinator",
                null,
                Instant.now(),
                Map.of()
        );

        publisher.publish(event);

        verify(messagingTemplate).convertAndSend("/topic/events", event);
    }
}
