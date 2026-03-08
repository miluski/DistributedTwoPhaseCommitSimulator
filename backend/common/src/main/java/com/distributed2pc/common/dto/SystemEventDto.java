package com.distributed2pc.common.dto;

import com.distributed2pc.common.enums.EventType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Generic event broadcast over WebSocket to the UI via /topic/events.
 *
 * @param eventType     Type of the event (see {@link EventType}).
 * @param transactionId Associated transaction UUID (may be null for node-level
 *                      events).
 * @param sourceNodeId  Identifier of the node that generated the event.
 * @param targetNodeId  Identifier of the target node (if applicable).
 * @param timestamp     When the event occurred.
 * @param payload       Arbitrary key-value data providing event-specific
 *                      detail.
 */
public record SystemEventDto(
        EventType eventType,
        UUID transactionId,
        String sourceNodeId,
        String targetNodeId,
        Instant timestamp,
        Map<String, Object> payload) {

  /**
   * Convenience factory for events that do not involve a specific transaction.
   */
  public static SystemEventDto nodeEvent(EventType type, String sourceNodeId,
      Map<String, Object> payload) {
    return new SystemEventDto(type, null, sourceNodeId, null, Instant.now(), payload);
  }

  /**
   * Convenience factory for transaction-scoped events.
   */
  public static SystemEventDto txEvent(EventType type, UUID transactionId,
      String sourceNodeId, String targetNodeId, Map<String, Object> payload) {
    return new SystemEventDto(type, transactionId, sourceNodeId, targetNodeId,
        Instant.now(), payload);
  }
}
