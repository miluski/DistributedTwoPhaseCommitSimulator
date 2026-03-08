package com.distributed2pc.coordinator.feature.metrics.application;

import com.distributed2pc.common.dto.MetricsResponse;
import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.coordinator.feature.simulation.application.SimulationModeService;
import com.distributed2pc.coordinator.feature.transaction.application.TwoPhaseCommitService;
import com.distributed2pc.coordinator.feature.transaction.domain.CoordinatorTransaction;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Computes aggregate statistics across all transactions to give the UI a live
 * fault-coverage and outcome summary.
 *
 * <p>Metrics are derived on every call from the in-memory transaction store;
 * no separate counters need to be maintained.
 */
@RequiredArgsConstructor
@Service
public class MetricsService {

 private final TwoPhaseCommitService twoPhaseCommitService;
 private final SimulationModeService simulationModeService;

 /**
  * Computes and returns the current aggregate metrics.
  *
  * @return a {@link MetricsResponse} snapshot of all transaction outcomes
  *         plus the current redundancy flag.
  */
 public MetricsResponse compute() {
  List<CoordinatorTransaction> all = twoPhaseCommitService.findAll();
  long committed = countByStatus(all, TransactionStatus.COMMITTED);
  long aborted = countByStatus(all, TransactionStatus.ABORTED);
  long uncertain = countByStatus(all, TransactionStatus.UNCERTAIN);
  long inProgress = all.size() - committed - aborted - uncertain;
  long avgDecisionMs = computeAvgDecisionMs(all);
  return new MetricsResponse(
    all.size(), committed, aborted, uncertain, inProgress,
    avgDecisionMs, simulationModeService.isRedundancyEnabled());
 }

 private long countByStatus(List<CoordinatorTransaction> all, TransactionStatus status) {
  return all.stream().filter(tx -> tx.getStatus() == status).count();
 }

 private long computeAvgDecisionMs(List<CoordinatorTransaction> all) {
  List<CoordinatorTransaction> decided = all.stream()
    .filter(tx -> tx.getDecidedAt() != null)
    .toList();
  if (decided.isEmpty()) return 0L;
  return decided.stream()
    .mapToLong(tx -> Duration.between(tx.getInitiatedAt(), tx.getDecidedAt()).toMillis())
    .sum() / decided.size();
 }
}
