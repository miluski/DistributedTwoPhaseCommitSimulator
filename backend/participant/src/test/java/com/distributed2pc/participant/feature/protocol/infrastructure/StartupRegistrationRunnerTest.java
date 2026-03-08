package com.distributed2pc.participant.feature.protocol.infrastructure;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link StartupRegistrationRunner}.
 */
@ExtendWith(MockitoExtension.class)
class StartupRegistrationRunnerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    WebClient.Builder webClientBuilder;

    StartupRegistrationRunner runner;

    @BeforeEach
    void setUp() {
        runner = new StartupRegistrationRunner(
                webClientBuilder, "https://coordinator:8443", "server-1", "localhost", 9001);
    }

    @Test
    void run_givenSuccessfulRegistration_logsAndCompletes() {
        when(webClientBuilder.build()
                .post().uri(anyString()).contentType(any()).bodyValue(any())
                .retrieve().toBodilessEntity())
                .thenReturn(Mono.just(ResponseEntity.ok().build()));

        runner.run(new DefaultApplicationArguments());

        verify(webClientBuilder.build().post().uri(anyString()).contentType(any()).bodyValue(any())
                .retrieve()).toBodilessEntity();
    }

    @Test
    void run_givenRegistrationFailure_logsWarningAndContinues() {
        when(webClientBuilder.build()
                .post().uri(anyString()).contentType(any()).bodyValue(any())
                .retrieve().toBodilessEntity())
                .thenReturn(Mono.error(new RuntimeException("coordinator unreachable")));

        assertThatCode(() -> runner.run(new DefaultApplicationArguments())).doesNotThrowAnyException();
    }
}
