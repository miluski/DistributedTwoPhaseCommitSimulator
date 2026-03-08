package com.distributed2pc.participant.feature.simulation.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.distributed2pc.common.simulation.BaseSimulationConfigController;
import com.distributed2pc.participant.feature.simulation.application.SimulationModeService;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST endpoint for reading and updating the simulation mode on this
 * participant node.
 *
 * <p>
 * Toggling redundancy off disables the peer-consultation termination protocol
 * in
 * {@link com.distributed2pc.participant.feature.election.application.ElectionService},
 * which simulates operation without coordinator-failure recovery.
 */
@Tag(name = "Simulation config", description = "Read and update simulation mode on this participant node")
@RestController
@RequestMapping("/api/simulation/config")
public class SimulationConfigController extends BaseSimulationConfigController {

    /**
     * Creates the controller with the participant's simulation mode service.
     *
     * @param service the service managing this participant's redundancy flag.
     */
    public SimulationConfigController(SimulationModeService service) {
        super(service);
    }
}
