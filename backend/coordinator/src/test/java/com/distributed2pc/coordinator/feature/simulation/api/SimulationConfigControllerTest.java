package com.distributed2pc.coordinator.feature.simulation.api;

import com.distributed2pc.common.dto.SimulationConfigRequest;
import com.distributed2pc.coordinator.feature.simulation.application.SimulationModeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the coordinator's {@link SimulationConfigController}.
 */
@ExtendWith(MockitoExtension.class)
class SimulationConfigControllerTest {

    @Mock
    SimulationModeService simulationModeService;

    SimulationConfigController controller;

    @BeforeEach
    void setUp() {
        controller = new SimulationConfigController(simulationModeService);
    }

    @Test
    void getConfig_givenRedundancyEnabled_returnsEnabledFlag() {
        when(simulationModeService.isRedundancyEnabled()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.getConfig();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("redundancyEnabled", true);
    }

    @Test
    void getConfig_givenRedundancyDisabled_returnsDisabledFlag() {
        when(simulationModeService.isRedundancyEnabled()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.getConfig();

        assertThat(response.getBody()).containsEntry("redundancyEnabled", false);
    }

    @Test
    void updateConfig_givenEnableRequest_delegatesAndReturnsNewState() {
        SimulationConfigRequest request = new SimulationConfigRequest(true);
        when(simulationModeService.isRedundancyEnabled()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.updateConfig(request);

        verify(simulationModeService).setRedundancyEnabled(true);
        assertThat(response.getBody()).containsEntry("redundancyEnabled", true);
    }

    @Test
    void updateConfig_givenDisableRequest_delegatesAndReturnsNewState() {
        SimulationConfigRequest request = new SimulationConfigRequest(false);
        when(simulationModeService.isRedundancyEnabled()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.updateConfig(request);

        verify(simulationModeService).setRedundancyEnabled(false);
        assertThat(response.getBody()).containsEntry("redundancyEnabled", false);
    }
}
