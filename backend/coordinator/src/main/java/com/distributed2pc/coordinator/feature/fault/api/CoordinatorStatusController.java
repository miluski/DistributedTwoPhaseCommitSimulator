package com.distributed2pc.coordinator.feature.fault.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.distributed2pc.coordinator.feature.fault.application.CoordinatorStatusService;
import com.distributed2pc.coordinator.feature.fault.domain.CoordinatorNodeStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST controller that reports the current status of the coordinator node.
 *
 * <p>
 * This endpoint is deliberately excluded from the crash interceptor so that
 * the monitoring dashboard can always determine whether the coordinator is
 * online, crashed, or degraded regardless of any active fault simulation.
 */
@Tag(name = "Coordinator status", description = "Coordinator node health and active-fault summary")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/coordinator/status")
public class CoordinatorStatusController {

    private final CoordinatorStatusService coordinatorStatusService;

    /**
     * Returns the current health status and active faults of the coordinator.
     *
     * @return HTTP 200 with {@link CoordinatorNodeStatus} containing serverId,
     *         status, and the list of active faults.
     */
    @Operation(summary = "Returns the current health status and active faults of the coordinator")
    @ApiResponse(responseCode = "200", description = "Coordinator node status")
    @GetMapping
    public ResponseEntity<CoordinatorNodeStatus> getStatus() {
        return ResponseEntity.ok(coordinatorStatusService.getStatus());
    }
}
