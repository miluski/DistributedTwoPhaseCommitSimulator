package com.distributed2pc.coordinator.feature.transaction.api;

import com.distributed2pc.common.dto.TransactionRequest;
import com.distributed2pc.common.dto.TransactionResponse;
import com.distributed2pc.coordinator.feature.transaction.application.TwoPhaseCommitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * REST controller that exposes transaction management to the UI and external
 * clients.
 *
 * <p>
 * POST {@code /api/transactions} initiates a new 2PC round and returns the
 * final result
 * asynchronously via a {@link Mono}.
 */
@Tag(name = "Transactions", description = "Initiate and retrieve 2PC transactions")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

 private final TwoPhaseCommitService twoPhaseCommitService;
 private final TransactionResponseMapper mapper;

 /**
  * Initiates a new 2PC transaction.
  *
  * @param request contains the value to commit across all participants.
  * @return a Mono emitting the final {@link TransactionResponse} once Phase 2
  *         completes.
  */
 @Operation(summary = "Initiates a new 2PC transaction")
 @ApiResponse(responseCode = "201", description = "Transaction completed")
 @ApiResponse(responseCode = "400", description = "Malformed request body")
 @ApiResponse(responseCode = "500", description = "Internal coordinator error")
 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 public Mono<TransactionResponse> initiate(@RequestBody TransactionRequest request) {
  return twoPhaseCommitService.initiate(request.value())
    .map(mapper::toResponse);
 }

 /**
  * Returns all transactions recorded by the coordinator.
  *
  * @return list of transaction responses in insertion order.
  */
 @Operation(summary = "Returns all recorded transactions")
 @ApiResponse(responseCode = "200", description = "List of transactions")
 @GetMapping
 public List<TransactionResponse> listAll() {
  return twoPhaseCommitService.findAll().stream()
    .map(mapper::toResponse)
    .toList();
 }

 /**
  * Returns details of a single transaction.
  *
  * @param id the transaction UUID.
  * @return the transaction response.
  * @throws ResponseStatusException 404 if the transaction does not exist.
  */
 @Operation(summary = "Returns transaction details by identifier")
 @ApiResponse(responseCode = "200", description = "Transaction found")
 @ApiResponse(responseCode = "400", description = "Invalid transaction ID format")
 @ApiResponse(responseCode = "404", description = "Transaction not found")
 @GetMapping("/{id}")
 public TransactionResponse getById(@PathVariable UUID id) {
  return twoPhaseCommitService.findById(id)
    .map(mapper::toResponse)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
      "Transaction " + id + " not found"));
 }
}
