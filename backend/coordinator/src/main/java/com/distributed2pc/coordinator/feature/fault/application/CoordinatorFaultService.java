package com.distributed2pc.coordinator.feature.fault.application;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.distributed2pc.common.dto.FaultRequest;
import com.distributed2pc.common.dto.SystemEventDto;
import com.distributed2pc.common.enums.EventType;
import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.coordinator.feature.event.application.EventPublisher;
import com.distributed2pc.coordinator.feature.fault.domain.CoordinatorFaultStrategy;

import lombok.RequiredArgsConstructor;

/**
 * Manages fault injection state for the coordinator node.
 *
 * <p>
 * Uses a strategy map to delegate activate/deactivate operations per
 * {@link FaultType},
 * eliminating any if-else branching. Publishing a {@link SystemEventDto} on
 * every
 * change keeps the UI in sync in real time.
 */
@RequiredArgsConstructor
@Service
public class CoordinatorFaultService {

    private final Map<FaultType, CoordinatorFaultStrategy> strategies;
    private final EventPublisher eventPublisher;

    /**
     * Applies the fault described in the request to the coordinator.
     *
     * @param request the fault to inject or clear.
     * @throws IllegalArgumentException if the fault type has no registered
     *                                  strategy.
     */
    public void applyFault(FaultRequest request) {
        CoordinatorFaultStrategy strategy = resolveStrategy(request.type());
        if (request.enabled()) {
            strategy.activate(request.parameters());
            publishFaultEvent(request.type(), EventType.FAULT_INJECTED);
        } else {
            strategy.deactivate();
            publishFaultEvent(request.type(), EventType.FAULT_CLEARED);
        }
    }

    /**
     * Returns whether the specified fault is currently active on the coordinator.
     *
     * @param type the fault type to query.
     * @return {@code true} if the fault is active.
     */
    public boolean isActive(FaultType type) {
        return resolveStrategy(type).isActive();
    }

    /**
     * Returns all fault types that are currently active on the coordinator.
     *
     * @return list of active fault types; empty if the coordinator is fully
     *         operational.
     */
    public List<FaultType> listActiveFaults() {
        return strategies.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .map(Map.Entry::getKey)
                .toList();
    }

    private CoordinatorFaultStrategy resolveStrategy(FaultType type) {
        CoordinatorFaultStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy registered for fault type: " + type);
        }
        return strategy;
    }

    private void publishFaultEvent(FaultType type, EventType eventType) {
        eventPublisher.publish(SystemEventDto.nodeEvent(eventType, "coordinator",
                Map.of("faultType", type)));
    }
}
