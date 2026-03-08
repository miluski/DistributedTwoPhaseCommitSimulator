package com.distributed2pc.coordinator.feature.fault.api;

import com.distributed2pc.common.dto.FaultRequest;
import com.distributed2pc.coordinator.feature.fault.application.CoordinatorFaultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for injecting and clearing faults on the coordinator node.
 *
 * <p>
 * The UI calls {@code POST /api/coordinator/fault} to simulate coordinator
 * crashes,
 * delayed decisions, or partial-send scenarios.
 */
@Tag(name = "Coordinator faults", description = "Inject and clear faults on the coordinator node")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/coordinator/fault")
public class CoordinatorFaultController {

 private final CoordinatorFaultService faultService;

 /**
  * Injects or clears a fault on the coordinator.
  *
  * @param request the fault type, enabled flag, and optional parameters.
  * @return a confirmation message.
  */
 @Operation(summary = "Injects or clears a fault on the coordinator")
 @ApiResponse(responseCode = "200", description = "Operation confirmation")
 @ApiResponse(responseCode = "400", description = "Malformed request body")
 @PostMapping
 public ResponseEntity<Map<String, String>> applyFault(@RequestBody FaultRequest request) {
  faultService.applyFault(request);
  String action = request.enabled() ? "injected" : "cleared";
  return ResponseEntity.ok(Map.of("message",
    "Fault " + request.type() + " " + action + " on coordinator"));
 }
}
