package com.distributed2pc.participant.feature.election.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link CoordinatorHeartbeatMonitor}.
 */
@ExtendWith(MockitoExtension.class)
class CoordinatorHeartbeatMonitorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    WebClient.Builder webClientBuilder;

    @Mock
    UncertainTransactionRecoveryScheduler recoveryScheduler;

    CoordinatorHeartbeatMonitor monitor;

    private static final String COORDINATOR_URL = "https://coordinator:8443";

    @BeforeEach
    void setUp() {
        monitor = new CoordinatorHeartbeatMonitor(webClientBuilder, COORDINATOR_URL, recoveryScheduler);
    }

    @Test
    void checkCoordinatorLiveness_givenCoordinatorResponds_doesNotTriggerRecovery() {
        when(webClientBuilder.build()
                .get().uri(anyString()).retrieve()
                .toBodilessEntity())
                .thenReturn(Mono.just(ResponseEntity.ok().build()));

        monitor.checkCoordinatorLiveness();

        verify(recoveryScheduler, never()).recoverAllPreparingNow();
        assertThat(monitor.isCoordinatorReachable()).isTrue();
    }

    @Test
    void checkCoordinatorLiveness_givenFirstFailure_triggersImmediateRecovery() {
        when(webClientBuilder.build()
                .get().uri(anyString()).retrieve()
                .toBodilessEntity())
                .thenReturn(Mono.error(new RuntimeException("connection refused")));

        monitor.checkCoordinatorLiveness();

        verify(recoveryScheduler).recoverAllPreparingNow();
        assertThat(monitor.isCoordinatorReachable()).isFalse();
    }

    @Test
    void checkCoordinatorLiveness_givenAlreadyUnreachable_doesNotRetriggerRecovery() {
        when(webClientBuilder.build()
                .get().uri(anyString()).retrieve()
                .toBodilessEntity())
                .thenReturn(Mono.error(new RuntimeException("connection refused")));

        monitor.checkCoordinatorLiveness();
        monitor.checkCoordinatorLiveness();

        verify(recoveryScheduler).recoverAllPreparingNow();
    }

    @Test
    void checkCoordinatorLiveness_givenCoordinatorComesBackOnline_resetsReachabilityFlag() {
        when(webClientBuilder.build()
                .get().uri(anyString()).retrieve()
                .toBodilessEntity())
                .thenReturn(Mono.error(new RuntimeException("connection refused")))
                .thenReturn(Mono.just(ResponseEntity.ok().build()));

        monitor.checkCoordinatorLiveness();
        assertThat(monitor.isCoordinatorReachable()).isFalse();

        monitor.checkCoordinatorLiveness();
        assertThat(monitor.isCoordinatorReachable()).isTrue();
    }
}
