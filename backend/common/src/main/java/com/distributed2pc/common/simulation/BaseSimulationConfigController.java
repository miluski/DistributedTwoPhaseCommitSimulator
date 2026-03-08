package com.distributed2pc.common.simulation;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.distributed2pc.common.dto.SimulationConfigRequest;

/**
 * Abstract base controller for the simulation-mode configuration endpoint.
 *
 * <p>
 * Concrete subclasses in each service module declare {@code @RestController}
 * and
 * {@code @RequestMapping} to activate the inherited handler methods within
 * their own
 * Spring application context.
 */
@RequestMapping("/api/simulation/config")
public abstract class BaseSimulationConfigController {

    private final SimulationModePort simulationModePort;

    /**
     * Creates the controller with the given simulation mode port.
     *
     * @param simulationModePort the port used to read and update the redundancy
     *                           flag.
     */
    protected BaseSimulationConfigController(SimulationModePort simulationModePort) {
        this.simulationModePort = simulationModePort;
    }

    /**
     * Returns the current simulation configuration.
     *
     * @return 200 with {@code { "redundancyEnabled": true|false }}.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(Map.of("redundancyEnabled", simulationModePort.isRedundancyEnabled()));
    }

    /**
     * Updates the simulation configuration on this node.
     *
     * @param request contains the new {@code redundancyEnabled} flag.
     * @return 200 with the updated configuration.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody SimulationConfigRequest request) {
        simulationModePort.setRedundancyEnabled(request.redundancyEnabled());
        return ResponseEntity.ok(Map.of("redundancyEnabled", simulationModePort.isRedundancyEnabled()));
    }
}
