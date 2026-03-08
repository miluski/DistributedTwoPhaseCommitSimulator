package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SimulationConfigRequest}.
 */
class SimulationConfigRequestTest {

    @Test
    void constructor_givenTrue_storesRedundancyEnabled() {
        assertThat(new SimulationConfigRequest(true).redundancyEnabled()).isTrue();
    }

    @Test
    void constructor_givenFalse_storesRedundancyDisabled() {
        assertThat(new SimulationConfigRequest(false).redundancyEnabled()).isFalse();
    }

    @Test
    void equals_givenBothTrue_returnsTrue() {
        assertThat(new SimulationConfigRequest(true))
                .isEqualTo(new SimulationConfigRequest(true));
    }

    @Test
    void equals_givenBothFalse_returnsTrue() {
        assertThat(new SimulationConfigRequest(false))
                .isEqualTo(new SimulationConfigRequest(false));
    }

    @Test
    void equals_givenDifferentValues_returnsFalse() {
        assertThat(new SimulationConfigRequest(true))
                .isNotEqualTo(new SimulationConfigRequest(false));
    }

    @Test
    void hashCode_givenSameValue_returnsSameHashCode() {
        assertThat(new SimulationConfigRequest(true).hashCode())
                .hasSameHashCodeAs(new SimulationConfigRequest(true).hashCode());
    }
}
