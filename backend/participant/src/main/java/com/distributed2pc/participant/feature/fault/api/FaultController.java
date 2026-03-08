package com.distributed2pc.participant.feature.fault.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.distributed2pc.common.dto.FaultRequest;
import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.participant.feature.fault.application.FaultInjectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for injecting and clearing faults on this participant node.
 */
@Tag(name = "Participant faults", description = "Inject and clear faults on this participant node")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/faults")
public class FaultController {

    private final FaultInjectionService faultInjectionService;

    /**
     * Injects or clears a fault.
     *
     * @param request the fault type, parameters, and enabled flag.
     * @return a confirmation message.
     */
    @Operation(summary = "Injects or clears a fault")
    @ApiResponse(responseCode = "200", description = "Operation confirmation")
    @ApiResponse(responseCode = "400", description = "Malformed request body")
    @PostMapping
    public ResponseEntity<Map<String, String>> inject(@RequestBody FaultRequest request) {
        faultInjectionService.applyFault(request);
        String action = request.enabled() ? "injected" : "cleared";
        return ResponseEntity.ok(Map.of("message", "Fault " + request.type() + " " + action));
    }

    /**
     * Clears a specific fault by type.
     *
     * @param type the fault type to clear.
     * @return a confirmation message.
     */
    @Operation(summary = "Clears a fault of the specified type")
    @ApiResponse(responseCode = "200", description = "Fault cleared")
    @ApiResponse(responseCode = "400", description = "Unknown fault type value")
    @DeleteMapping("/{type}")
    public ResponseEntity<Map<String, String>> clear(@PathVariable FaultType type) {
        faultInjectionService.applyFault(new FaultRequest(type, false));
        return ResponseEntity.ok(Map.of("message", "Fault " + type + " cleared"));
    }

    /**
     * Returns all currently active faults on this participant.
     *
     * @return list of active fault types.
     */
    @Operation(summary = "Returns all currently active faults")
    @ApiResponse(responseCode = "200", description = "Active faults")
    @GetMapping
    public ResponseEntity<List<FaultType>> listActive() {
        return ResponseEntity.ok(faultInjectionService.listActiveFaults());
    }
}
