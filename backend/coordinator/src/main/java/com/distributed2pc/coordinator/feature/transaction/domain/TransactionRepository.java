package com.distributed2pc.coordinator.feature.transaction.domain;

import com.distributed2pc.common.enums.TransactionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for reading and writing coordinator-side transaction state.
 *
 * <p>Implementations live in {@code infrastructure} and may be replaced
 * with a persistent store without changing any application or API code.
 */
public interface TransactionRepository {

 /**
  * Persists a new transaction.
  *
  * @param transaction the transaction to store.
  */
 void save(CoordinatorTransaction transaction);

 /**
  * Retrieves a transaction by its unique identifier.
  *
  * @param id the transaction UUID.
  * @return the transaction wrapped in Optional, or empty if not found.
  */
 Optional<CoordinatorTransaction> findById(UUID id);

 /**
  * Returns all transactions in insertion order.
  *
  * @return immutable snapshot list.
  */
 List<CoordinatorTransaction> findAll();

 /**
  * Updates the status of an existing transaction.
  *
  * @param id     the target transaction UUID.
  * @param status the new status.
  */
 void updateStatus(UUID id, TransactionStatus status);
}
