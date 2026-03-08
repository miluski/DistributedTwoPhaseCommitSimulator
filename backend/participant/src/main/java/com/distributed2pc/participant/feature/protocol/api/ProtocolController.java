package com.distributed2pc.participant.feature.protocol.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.distributed2pc.common.dto.AbortMessage;
import com.distributed2pc.common.dto.CommitMessage;
import com.distributed2pc.common.dto.PrepareMessage;
import com.distributed2pc.common.dto.VoteMessage;
import com.distributed2pc.participant.feature.protocol.application.ProtocolService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST controllers that receive 2PC protocol messages from the coordinator.
 *
 * <p>
 * All endpoints are on the path {@code /api/2pc/} and are reachable only
 * by the coordinator or other participants (during election). The crash fault
 * interceptor blocks these endpoints when {@code CRASH} fault is active.
 */
@Tag(name = "2PC protocol", description = "Handles 2PC protocol messages from the coordinator")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/2pc")
public class ProtocolController {

    private final ProtocolService protocolService;

    /**
     * Phase 1: receives a PREPARE and returns this participant's vote.
     *
     * @param message the coordinator's prepare request.
     * @return the vote response.
     */
    @Operation(summary = "Phase 1: receives PREPARE and returns this participant's vote")
    @ApiResponse(responseCode = "200", description = "Participant vote")
    @PostMapping("/prepare")
    public ResponseEntity<VoteMessage> prepare(@RequestBody PrepareMessage message) {
        return ResponseEntity.ok(protocolService.handlePrepare(message));
    }

    /**
     * Phase 2 COMMIT: applies the commit decision to the local log.
     *
     * @param message the coordinator's commit instruction.
     * @return 204 No Content on success.
     */
    @Operation(summary = "Phase 2: applies the commit decision")
    @ApiResponse(responseCode = "204", description = "Transaction committed")
    @PostMapping("/commit")
    public ResponseEntity<Void> commit(@RequestBody CommitMessage message) {
        protocolService.handleCommit(message);
        return ResponseEntity.noContent().build();
    }

    /**
     * Phase 2 ABORT: applies the abort decision to the local log.
     *
     * @param message the coordinator's abort instruction.
     * @return 204 No Content on success.
     */
    @Operation(summary = "Phase 2: applies the abort decision")
    @ApiResponse(responseCode = "204", description = "Transaction aborted")
    @PostMapping("/abort")
    public ResponseEntity<Void> abort(@RequestBody AbortMessage message) {
        protocolService.handleAbort(message);
        return ResponseEntity.noContent().build();
    }
}
