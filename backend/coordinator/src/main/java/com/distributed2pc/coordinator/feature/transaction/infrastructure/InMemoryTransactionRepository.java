package com.distributed2pc.coordinator.feature.transaction.infrastructure;

import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.coordinator.feature.transaction.domain.CoordinatorTransaction;
import com.distributed2pc.coordinator.feature.transaction.domain.TransactionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * Thread-safe in-memory implementation of {@link TransactionRepository}.
 *
 * <p>Uses a {@link ConcurrentHashMap} for O(1) lookups and a separate list
 * to maintain insertion order for the "list all" use case.
 */
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

 private final Map<UUID, CoordinatorTransaction> store = new ConcurrentHashMap<>();
 private final List<UUID> insertionOrder = new ArrayList<>();

 @Override
 public synchronized void save(CoordinatorTransaction transaction) {
  store.put(transaction.getId(), transaction);
  insertionOrder.add(transaction.getId());
 }

 @Override
 public Optional<CoordinatorTransaction> findById(UUID id) {
  return Optional.ofNullable(store.get(id));
 }

 @Override
 public synchronized List<CoordinatorTransaction> findAll() {
  return insertionOrder.stream()
    .map(store::get)
    .toList();
 }

 @Override
 public void updateStatus(UUID id, TransactionStatus status) {
  Optional.ofNullable(store.get(id))
    .ifPresent(tx -> tx.setStatus(status));
 }
}
