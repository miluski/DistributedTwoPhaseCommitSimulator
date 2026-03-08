package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.distributed2pc.common.enums.EventType;

/**
 * Unit tests for {@link SystemEventDto}.
 */
class SystemEventDtoTest {

    @Test
    void constructor_givenAllFields_storesThemCorrectly() {
        UUID id = UUID.randomUUID();
        Instant ts = Instant.now();
        Map<String, Object> payload = Map.of("key", "val");

        SystemEventDto dto = new SystemEventDto(
                EventType.TRANSACTION_STARTED, id, "coord", "p1", ts, payload);

        assertThat(dto.eventType()).isEqualTo(EventType.TRANSACTION_STARTED);
        assertThat(dto.transactionId()).isEqualTo(id);
        assertThat(dto.sourceNodeId()).isEqualTo("coord");
        assertThat(dto.targetNodeId()).isEqualTo("p1");
        assertThat(dto.timestamp()).isEqualTo(ts);
        assertThat(dto.payload()).containsEntry("key", "val");
    }

    @Test
    void nodeEvent_givenTypeAndSource_setsNullTransactionIdAndNullTargetNode() {
        SystemEventDto evt = SystemEventDto.nodeEvent(
                EventType.FAULT_INJECTED, "server-1", Map.of());

        assertThat(evt.eventType()).isEqualTo(EventType.FAULT_INJECTED);
        assertThat(evt.sourceNodeId()).isEqualTo("server-1");
        assertThat(evt.transactionId()).isNull();
        assertThat(evt.targetNodeId()).isNull();
    }

    @Test
    void nodeEvent_givenAnyInput_setsTimestampAfterOrEqualToNow() {
        Instant before = Instant.now();

        SystemEventDto evt = SystemEventDto.nodeEvent(
                EventType.FAULT_INJECTED, "server-1", Map.of());

        assertThat(evt.timestamp()).isAfterOrEqualTo(before);
    }

    @Test
    void nodeEvent_givenPayload_storesPayload() {
        Map<String, Object> payload = Map.of("fault", "CRASH");

        SystemEventDto evt = SystemEventDto.nodeEvent(
                EventType.FAULT_INJECTED, "server-1", payload);

        assertThat(evt.payload()).containsEntry("fault", "CRASH");
    }

    @Test
    void txEvent_givenAllFields_populatesAllAccessors() {
        UUID id = UUID.randomUUID();

        SystemEventDto evt = SystemEventDto.txEvent(
                EventType.VOTE_RECEIVED, id, "coord", "p1", Map.of("vote", "YES"));

        assertThat(evt.eventType()).isEqualTo(EventType.VOTE_RECEIVED);
        assertThat(evt.transactionId()).isEqualTo(id);
        assertThat(evt.sourceNodeId()).isEqualTo("coord");
        assertThat(evt.targetNodeId()).isEqualTo("p1");
        assertThat(evt.payload()).containsEntry("vote", "YES");
    }

    @Test
    void txEvent_givenAnyInput_setsTimestampAfterOrEqualToNow() {
        Instant before = Instant.now();

        SystemEventDto evt = SystemEventDto.txEvent(
                EventType.VOTE_RECEIVED, UUID.randomUUID(), "c", "p", Map.of());

        assertThat(evt.timestamp()).isAfterOrEqualTo(before);
    }

    @Test
    void equals_givenSameValues_returnsTrue() {
        UUID id = UUID.randomUUID();
        Instant ts = Instant.now();
        Map<String, Object> payload = Map.of();

        assertThat(new SystemEventDto(EventType.COMMIT_SENT, id, "c", "p", ts, payload))
                .isEqualTo(new SystemEventDto(EventType.COMMIT_SENT, id, "c", "p", ts, payload));
    }

    @Test
    void equals_givenDifferentEventType_returnsFalse() {
        UUID id = UUID.randomUUID();
        Instant ts = Instant.now();

        assertThat(new SystemEventDto(EventType.COMMIT_SENT, id, "c", "p", ts, Map.of()))
                .isNotEqualTo(new SystemEventDto(EventType.ABORT_SENT, id, "c", "p", ts, Map.of()));
    }
}
