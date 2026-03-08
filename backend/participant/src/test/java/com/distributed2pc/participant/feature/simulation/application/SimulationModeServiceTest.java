package com.distributed2pc.participant.feature.simulation.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SimulationModeService} (participant).
 */
class SimulationModeServiceTest {

    SimulationModeService service;

    @BeforeEach
    void setUp() {
        service = new SimulationModeService();
    }

    @Test
    void isRedundancyEnabled_givenInitialState_returnsTrue() {
        assertThat(service.isRedundancyEnabled()).isTrue();
    }

    @Test
    void setRedundancyEnabled_givenFalse_disablesRedundancy() {
        service.setRedundancyEnabled(false);
        assertThat(service.isRedundancyEnabled()).isFalse();
    }

    @Test
    void setRedundancyEnabled_givenTrueAfterDisable_reEnablesRedundancy() {
        service.setRedundancyEnabled(false);
        service.setRedundancyEnabled(true);
        assertThat(service.isRedundancyEnabled()).isTrue();
    }
}
