package com.distributed2pc.coordinator.feature.simulation.application;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

import com.distributed2pc.common.simulation.SimulationModePort;

import lombok.extern.slf4j.Slf4j;

/**
 * Holds the coordinator-side flag that enables or disables the participant
 * election-recovery protocol.
 *
 * <p>
 * When redundancy is <em>disabled</em> the system operates in
 * &quot;non-redundant&quot; mode: participants will not run the termination
 * protocol when the coordinator fails, leaving uncertain transactions
 * permanently unresolved. This allows direct comparison of system behaviour
 * with and without the recovery mechanism.
 */
@Slf4j
@Service
public class SimulationModeService implements SimulationModePort {

    private final AtomicBoolean redundancyEnabled = new AtomicBoolean(true);

    /**
     * Returns whether the recovery redundancy protocol is currently enabled.
     *
     * @return {@code true} when participants will run the election protocol.
     */
    @Override
    public boolean isRedundancyEnabled() {
        return redundancyEnabled.get();
    }

    /**
     * Enables or disables the election-recovery redundancy protocol.
     *
     * @param enabled {@code true} to turn redundancy on; {@code false} to disable
     *                it.
     */
    @Override
    public void setRedundancyEnabled(boolean enabled) {
        redundancyEnabled.set(enabled);
        log.info("Coordinator redundancy set to {}", enabled);
    }
}
