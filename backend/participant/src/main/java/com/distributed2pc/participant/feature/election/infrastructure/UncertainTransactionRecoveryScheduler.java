package com.distributed2pc.participant.feature.election.infrastructure;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.participant.feature.election.application.ElectionService;
import com.distributed2pc.participant.feature.log.domain.LogEntry;
import com.distributed2pc.participant.feature.log.infrastructure.InMemoryTransactionLog;

import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled component that automatically recovers participants stuck in the
 * {@link TransactionStatus#PREPARING} phase after the coordinator has failed.
 *
 * <p>
 * Every {@code participant.recovery.interval-ms} milliseconds this scheduler
 * scans the transaction log for any entry that has been in {@code PREPARING}
 * state
 * for longer than {@code participant.recovery.timeout-ms} and triggers the
 * peer-consultation termination protocol ({@link ElectionService#elect}) to
 * resolve the outcome without any coordinator involvement.
 *
 * <p>
 * If the election returns {@link TransactionStatus#UNCERTAIN} (all peers are
 * also undecided), the entry is left unchanged and retried on the next tick.
 */
@Slf4j
@Component
public class UncertainTransactionRecoveryScheduler {

    private final InMemoryTransactionLog transactionLog;
    private final ElectionService electionService;
    private final long timeoutMs;

    /**
     * @param transactionLog  the participant's durable log to scan for stuck
     *                        transactions.
     * @param electionService the termination-protocol service used to resolve
     *                        outcomes.
     * @param timeoutMs       how long a PREPARING entry must be stale before
     *                        recovery is triggered.
     */
    public UncertainTransactionRecoveryScheduler(
            InMemoryTransactionLog transactionLog,
            ElectionService electionService,
            @Value("${participant.recovery.timeout-ms:5000}") long timeoutMs) {
        this.transactionLog = transactionLog;
        this.electionService = electionService;
        this.timeoutMs = timeoutMs;
    }

    /**
     * Scans for timed-out PREPARING transactions and triggers peer-election for
     * each.
     * Invoked automatically by the Spring scheduler.
     */
    @Scheduled(fixedDelayString = "${participant.recovery.interval-ms:5000}")
    public void recoverUncertainTransactions() {
        Instant cutoff = Instant.now().minusMillis(timeoutMs);
        transactionLog.findAllWithPhase(TransactionStatus.PREPARING).stream()
                .filter(entry -> entry.timestamp().isBefore(cutoff))
                .forEach(this::recoverEntry);
    }

    /**
     * Immediately triggers peer-election for <em>all</em> PREPARING transactions
     * regardless of their age.
     *
     * <p>
     * Called by {@link CoordinatorHeartbeatMonitor} when the coordinator is
     * detected as unreachable, allowing stuck transactions to be resolved without
     * waiting for the normal timeout window.
     */
    public void recoverAllPreparingNow() {
        transactionLog.findAllWithPhase(TransactionStatus.PREPARING)
                .forEach(this::recoverEntry);
    }

    private void recoverEntry(LogEntry entry) {
        TransactionStatus resolved = electionService.elect(entry.transactionId());
        log.info("Auto-recovery: tx {} resolved to {}", entry.transactionId(), resolved);
        if (resolved != TransactionStatus.UNCERTAIN) {
            transactionLog.updatePhase(entry.transactionId(), resolved);
        }
    }
}
