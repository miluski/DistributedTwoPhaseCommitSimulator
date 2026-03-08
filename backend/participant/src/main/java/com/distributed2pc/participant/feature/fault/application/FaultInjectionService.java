package com.distributed2pc.participant.feature.fault.application;

import com.distributed2pc.common.dto.FaultRequest;
import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.participant.feature.fault.domain.ParticipantFaultStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Manages fault injection state for a single participant node.
 *
 * <p>Delegates to a strategy map keyed by {@link FaultType}. This eliminates
 * any branching logic: adding a new fault type requires only a new strategy
 * implementation, not a change to this service.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FaultInjectionService {

    private final Map<FaultType, ParticipantFaultStrategy> strategies;

    /**
     * Applies or clears the fault described in the request.
     *
     * @param request the fault to inject or clear.
     * @throws IllegalArgumentException if no strategy is registered for the fault type.
     */
    public void applyFault(FaultRequest request) {
        ParticipantFaultStrategy strategy = resolveStrategy(request.type());
        if (request.enabled()) {
            strategy.activate(request.parameters());
            log.warn("Fault {} activated", request.type());
        } else {
            strategy.deactivate();
            log.info("Fault {} deactivated", request.type());
        }
    }

    /**
     * Returns whether the specified fault is currently active.
     *
     * @param type the fault type to check.
     * @return {@code true} if active.
     */
    public boolean isActive(FaultType type) {
        return resolveStrategy(type).isActive();
    }

    /**
     * Returns all active faults for status reporting.
     *
     * @return list of active fault types.
     */
    public java.util.List<FaultType> listActiveFaults() {
        return strategies.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .map(Map.Entry::getKey)
                .toList();
    }

    private ParticipantFaultStrategy resolveStrategy(FaultType type) {
        ParticipantFaultStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy for fault type: " + type);
        }
        return strategy;
    }
}
