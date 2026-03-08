package com.distributed2pc.coordinator.feature.metrics.application;

import com.distributed2pc.common.dto.MetricsResponse;
import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.coordinator.feature.simulation.application.SimulationModeService;
import com.distributed2pc.coordinator.feature.transaction.application.TwoPhaseCommitService;
import com.distributed2pc.coordinator.feature.transaction.domain.CoordinatorTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MetricsService}.
 */
@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    TwoPhaseCommitService twoPhaseCommitService;

    @Mock
    SimulationModeService simulationModeService;

    MetricsService service;

    @BeforeEach
    void setUp() {
        service = new MetricsService(twoPhaseCommitService, simulationModeService);
        when(simulationModeService.isRedundancyEnabled()).thenReturn(true);
    }

    @Test
    void compute_givenNoTransactions_returnsZeroCounts() {
        when(twoPhaseCommitService.findAll()).thenReturn(List.of());

        MetricsResponse metrics = service.compute();

        assertThat(metrics.total()).isZero();
        assertThat(metrics.committed()).isZero();
        assertThat(metrics.aborted()).isZero();
    }

    @Test
    void compute_givenCommittedTransaction_countsCommitted() {
        CoordinatorTransaction tx = committedTx();
        when(twoPhaseCommitService.findAll()).thenReturn(List.of(tx));

        MetricsResponse metrics = service.compute();

        assertThat(metrics.total()).isEqualTo(1);
        assertThat(metrics.committed()).isEqualTo(1);
        assertThat(metrics.aborted()).isZero();
    }

    @Test
    void compute_givenUncertainTransaction_countsUncertain() {
        CoordinatorTransaction tx = uncertainTx();
        when(twoPhaseCommitService.findAll()).thenReturn(List.of(tx));

        MetricsResponse metrics = service.compute();

        assertThat(metrics.uncertain()).isEqualTo(1);
    }

    @Test
    void compute_givenRedundancyDisabled_reflectsFlag() {
        when(simulationModeService.isRedundancyEnabled()).thenReturn(false);
        when(twoPhaseCommitService.findAll()).thenReturn(List.of());

        MetricsResponse metrics = service.compute();

        assertThat(metrics.redundancyEnabled()).isFalse();
    }

    @Test
    void compute_givenDecidedTransaction_calculatesAvgDecisionMs() {
        CoordinatorTransaction tx = committedTx();
        when(twoPhaseCommitService.findAll()).thenReturn(List.of(tx));

        MetricsResponse metrics = service.compute();

        assertThat(metrics.avgDecisionMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void compute_givenMixedTransactions_countsAllStatuses() {
        when(twoPhaseCommitService.findAll()).thenReturn(
                List.of(committedTx(), abortedTx(), uncertainTx()));

        MetricsResponse metrics = service.compute();

        assertThat(metrics.total()).isEqualTo(3);
        assertThat(metrics.committed()).isEqualTo(1);
        assertThat(metrics.aborted()).isEqualTo(1);
        assertThat(metrics.uncertain()).isEqualTo(1);
    }

    private CoordinatorTransaction committedTx() {
        CoordinatorTransaction tx = new CoordinatorTransaction("v");
        tx.applyDecision(TransactionStatus.COMMITTED);
        return tx;
    }

    private CoordinatorTransaction abortedTx() {
        CoordinatorTransaction tx = new CoordinatorTransaction("v");
        tx.applyDecision(TransactionStatus.ABORTED);
        return tx;
    }

    private CoordinatorTransaction uncertainTx() {
        CoordinatorTransaction tx = new CoordinatorTransaction("v");
        tx.setStatus(TransactionStatus.UNCERTAIN);
        return tx;
    }
}
