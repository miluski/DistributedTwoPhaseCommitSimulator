package com.distributed2pc.participant.feature.simulation.application;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

import com.distributed2pc.common.simulation.SimulationModePort;

import lombok.extern.slf4j.Slf4j;

/**
 * Holds the participant-side flag that enables or disables the
 * election-recovery
 * termination protocol.
 *
 * <p>
 * When redundancy is <em>disabled</em> the {@link
 * com.distributed2pc.participant.feature.election.application.ElectionService}
 * immediately returns
 * {@link com.distributed2pc.common.enums.TransactionStatus#UNCERTAIN}
 * instead of consulting peers. This simulates the &quot;without
 * redundancy&quot; scenario
 * used for comparison runs.
 */
@Slf4j
@Service
public class SimulationModeService implements SimulationModePort {

    private final AtomicBoolean redundancyEnabled = new AtomicBoolean(true);

    /**
     * Returns whether the election-recovery protocol is currently active on this
     * participant.
     *
     * @return {@code true} when the termination protocol is enabled.
     */
    @Override
    public boolean isRedundancyEnabled() {
        return redundancyEnabled.get();
    }

    /**
     * Enables or disables the election-recovery redundancy protocol on this
     * participant.
     *
     * @param enabled {@code true} to allow peer consultation; {@code false} to
     *                short-circuit.
     */
    @Override
    public void setRedundancyEnabled(boolean enabled) {
        redundancyEnabled.set(enabled);
        log.info("Participant redundancy set to {}", enabled);
    }
}
