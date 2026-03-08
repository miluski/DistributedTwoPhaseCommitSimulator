package com.distributed2pc.coordinator.feature.event.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link WebSocketTopic}.
 */
class WebSocketTopicTest {

    @Test
    void values_containsOneConstant() {
        assertThat(WebSocketTopic.values()).hasSize(1);
    }

    @Test
    void events_givenGetPath_returnsTopicPath() {
        assertThat(WebSocketTopic.EVENTS.getPath()).isEqualTo("/topic/events");
    }

    @Test
    void events_givenGetPath_startsWithSlashTopic() {
        assertThat(WebSocketTopic.EVENTS.getPath()).startsWith("/topic/");
    }

    @ParameterizedTest
    @MethodSource("provideAllConstantNames")
    void valueOf_givenValidName_returnsConstant(String name) {
        assertThat(WebSocketTopic.valueOf(name)).isNotNull();
    }

    static Stream<String> provideAllConstantNames() {
        return Stream.of(WebSocketTopic.values()).map(Enum::name);
    }
}
