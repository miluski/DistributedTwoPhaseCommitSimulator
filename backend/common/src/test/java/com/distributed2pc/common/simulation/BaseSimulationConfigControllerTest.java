package com.distributed2pc.common.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.distributed2pc.common.dto.SimulationConfigRequest;

/**
 * Unit tests for {@link BaseSimulationConfigController}.
 */
@ExtendWith(MockitoExtension.class)
class BaseSimulationConfigControllerTest {

    @Mock
    SimulationModePort simulationModePort;

    BaseSimulationConfigController controller;

    /** Creates a minimal concrete subclass for testing the abstract base. */
    @BeforeEach
    void setUp() {
        controller = new BaseSimulationConfigController(simulationModePort) {
        };
    }

    /**
     * getConfig returns redundancyEnabled=true when port reports enabled.
     */
    @Test
    void getConfig_givenRedundancyEnabled_returnsEnabledFlag() {
        when(simulationModePort.isRedundancyEnabled()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.getConfig();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("redundancyEnabled", true);
    }

    /**
     * getConfig returns redundancyEnabled=false when port reports disabled.
     */
    @Test
    void getConfig_givenRedundancyDisabled_returnsDisabledFlag() {
        when(simulationModePort.isRedundancyEnabled()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.getConfig();

        assertThat(response.getBody()).containsEntry("redundancyEnabled", false);
    }

    /**
     * updateConfig delegates to port and returns the new state.
     */
    @Test
    void updateConfig_givenEnableRequest_delegatesAndReturnsNewState() {
        SimulationConfigRequest request = new SimulationConfigRequest(true);
        when(simulationModePort.isRedundancyEnabled()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.updateConfig(request);

        verify(simulationModePort).setRedundancyEnabled(true);
        assertThat(response.getBody()).containsEntry("redundancyEnabled", true);
    }

    /**
     * updateConfig with disable request sets flag to false.
     */
    @Test
    void updateConfig_givenDisableRequest_setsDisabledAndReturns() {
        SimulationConfigRequest request = new SimulationConfigRequest(false);
        when(simulationModePort.isRedundancyEnabled()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.updateConfig(request);

        verify(simulationModePort).setRedundancyEnabled(false);
        assertThat(response.getBody()).containsEntry("redundancyEnabled", false);
    }
}
