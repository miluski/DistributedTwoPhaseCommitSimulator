package com.distributed2pc.participant.feature.fault.domain;

import java.util.Map;

/**
 * Strategy contract for applying or clearing a specific fault type on a participant node.
 *
 * <p>Implementations are registered in a {@code Map<FaultType, ParticipantFaultStrategy>}
 * configuration bean. The {@code FaultInjectionService} performs a single map lookup
 * to dispatch without any if-else branching.
 */
public interface ParticipantFaultStrategy {

    /**
     * Activates this fault using the given parameters.
     *
     * @param parameters type-specific settings (may be empty for faults like CRASH).
     */
    void activate(Map<String, Object> parameters);

    /**
     * Deactivates this fault, restoring normal node behaviour.
     */
    void deactivate();

    /**
     * Returns whether this fault is currently active.
     *
     * @return {@code true} if the fault is active.
     */
    boolean isActive();
}
