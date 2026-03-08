package com.distributed2pc.common.simulation;

/**
 * Port for reading and updating the simulation redundancy flag on any node.
 */
public interface SimulationModePort {

    /**
     * Returns whether the recovery redundancy protocol is currently enabled.
     *
     * @return {@code true} when the protocol is active.
     */
    boolean isRedundancyEnabled();

    /**
     * Enables or disables the recovery redundancy protocol.
     *
     * @param enabled {@code true} to enable; {@code false} to disable.
     */
    void setRedundancyEnabled(boolean enabled);
}
