package com.distributed2pc.participant.feature.protocol.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.distributed2pc.common.enums.NodeStatus;
import com.distributed2pc.participant.feature.fault.application.FaultInjectionService;
import com.distributed2pc.participant.feature.log.infrastructure.InMemoryTransactionLog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST endpoint that exposes the current health and active-fault summary of
 * this participant node. Used by the coordinator and the monitoring dashboard
 * to poll liveness.
 */
@Tag(name = "Node status", description = "Health and active-fault summary of this participant node")
@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final FaultInjectionService faultService;
    private final String serverId;
    private final InMemoryTransactionLog transactionLog;

    /**
     * Creates a new {@code StatusController} bound to this participant's identity.
     *
     * @param faultService   service to query for active faults.
     * @param serverId       identifier of this participant node.
     * @param transactionLog transaction log to derive the latest committed value.
     */
    public StatusController(FaultInjectionService faultService,
            @Value("${participant.server-id}") String serverId,
            InMemoryTransactionLog transactionLog) {
        this.faultService = faultService;
        this.serverId = serverId;
        this.transactionLog = transactionLog;
    }

    /**
     * Returns a simple status document for this node.
     *
     * @return HTTP 200 with a map containing {@code serverId}, {@code status},
     *         the list of {@code activeFaults}, and the {@code committedValue}.
     */
    @Operation(summary = "Returns the health status and active faults of this node")
    @ApiResponse(responseCode = "200", description = "Participant node status")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStatus() {
        List<String> activeFaults = faultService.listActiveFaults()
                .stream()
                .map(Enum::name)
                .toList();
        boolean crashed = activeFaults.contains("CRASH");
        NodeStatus nodeStatus = crashed ? NodeStatus.CRASHED : NodeStatus.ONLINE;
        Map<String, Object> body = new HashMap<>();
        body.put("serverId", serverId);
        body.put("status", nodeStatus.name());
        body.put("activeFaults", activeFaults);
        body.put("committedValue", transactionLog.getLatestCommittedValue().orElse(null));
        return ResponseEntity.ok(body);
    }
}
