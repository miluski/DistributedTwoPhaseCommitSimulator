package com.distributed2pc.coordinator.feature.simulation.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.distributed2pc.common.simulation.BaseSimulationConfigController;
import com.distributed2pc.coordinator.feature.simulation.application.SimulationModeService;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST endpoint for reading and updating the simulation mode on the
 * coordinator.
 *
 * <p>
 * The UI calls {@code POST /api/simulation/config} to toggle between redundant
 * and
 * non-redundant operating modes. After toggling the coordinator, the UI must
 * also call
 * the equivalent endpoint on every participant.
 */
@Tag(name = "Simulation config", description = "Read and update coordinator simulation mode")
@RestController
@RequestMapping("/api/simulation/config")
public class SimulationConfigController extends BaseSimulationConfigController {

    /**
     * Creates the controller with the coordinator's simulation mode service.
     *
     * @param service the service managing the coordinator's redundancy flag.
     */
    public SimulationConfigController(SimulationModeService service) {
        super(service);
    }
}
