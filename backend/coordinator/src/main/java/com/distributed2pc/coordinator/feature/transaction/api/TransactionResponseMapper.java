package com.distributed2pc.coordinator.feature.transaction.api;

import com.distributed2pc.common.dto.TransactionResponse;
import com.distributed2pc.coordinator.feature.transaction.domain.CoordinatorTransaction;
import org.springframework.stereotype.Component;

/**
 * Maps {@link CoordinatorTransaction} domain objects to {@link TransactionResponse} DTOs.
 *
 * <p>Keeping the mapping logic here prevents the controller from reaching into domain
 * internals and keeps the response shape decoupled from the domain model.
 */
@Component
public class TransactionResponseMapper {

 /**
  * Converts a coordinator transaction to a REST response DTO.
  *
  * @param tx the transaction to map; must not be null.
  * @return the corresponding response DTO.
  */
 public TransactionResponse toResponse(CoordinatorTransaction tx) {
  return new TransactionResponse(
    tx.getId(),
    tx.getStatus(),
    tx.getValue(),
    tx.getInitiatedAt(),
    tx.getDecidedAt(),
    tx.getVotes()
  );
 }
}
