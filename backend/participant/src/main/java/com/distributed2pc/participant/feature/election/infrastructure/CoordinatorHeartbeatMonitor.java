package com.distributed2pc.participant.feature.election.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Actively monitors coordinator liveness by polling its
 * {@code GET /api/coordinator/status} endpoint at a configurable interval.
 *
 * <p>
 * When the coordinator transitions from reachable to unreachable this monitor
 * immediately delegates to
 * {@link UncertainTransactionRecoveryScheduler#recoverAllPreparingNow()}
 * so that any transaction stuck in {@code PREPARING} is resolved via
 * peer-consultation without waiting for the passive timeout window.
 *
 * <p>
 * When the coordinator comes back online the {@code coordinatorReachable}
 * flag is reset, allowing the next failure to be detected afresh.
 */
@Slf4j
@Component
public class CoordinatorHeartbeatMonitor {

    private final WebClient webClient;
    private final String coordinatorUrl;
    private final UncertainTransactionRecoveryScheduler recoveryScheduler;

    private volatile boolean coordinatorReachable = true;

    /**
     * @param webClientBuilder  shared SSL-configured {@link WebClient.Builder}.
     * @param coordinatorUrl    base URL of the coordinator, e.g.
     *                          {@code https://coordinator:8443}.
     * @param recoveryScheduler scheduler that owns the recovery logic for stuck
     *                          transactions.
     */
    public CoordinatorHeartbeatMonitor(
            WebClient.Builder webClientBuilder,
            @Value("${participant.coordinator-url}") String coordinatorUrl,
            UncertainTransactionRecoveryScheduler recoveryScheduler) {
        this.webClient = webClientBuilder.build();
        this.coordinatorUrl = coordinatorUrl;
        this.recoveryScheduler = recoveryScheduler;
    }

    /**
     * Polls the coordinator's status endpoint.
     * On first failure triggers immediate election for all PREPARING transactions.
     * Invoked automatically by the Spring scheduler.
     */
    @Scheduled(fixedDelayString = "${participant.heartbeat.interval-ms:3000}")
    public void checkCoordinatorLiveness() {
        webClient.get()
                .uri(coordinatorUrl + "/api/coordinator/status")
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(r -> handleCoordinatorReachable())
                .doOnError(this::handleCoordinatorUnreachable)
                .onErrorResume(e -> Mono.empty())
                .block();
    }

    /**
     * Returns whether the coordinator was reachable on the last heartbeat check.
     *
     * @return {@code true} if the coordinator responded to the most recent probe.
     */
    public boolean isCoordinatorReachable() {
        return coordinatorReachable;
    }

    private void handleCoordinatorReachable() {
        if (!coordinatorReachable) {
            log.info("Coordinator is back online — resetting reachability flag");
        }
        coordinatorReachable = true;
    }

    private void handleCoordinatorUnreachable(Throwable cause) {
        boolean wasReachable = coordinatorReachable;
        coordinatorReachable = false;
        if (wasReachable) {
            log.warn("Coordinator unreachable ({}): triggering immediate recovery for PREPARING transactions",
                    cause.getMessage());
            recoveryScheduler.recoverAllPreparingNow();
        }
    }
}
