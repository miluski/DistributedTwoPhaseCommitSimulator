package com.distributed2pc.coordinator.feature.transaction.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.distributed2pc.common.dto.AbortMessage;
import com.distributed2pc.common.dto.CommitMessage;
import com.distributed2pc.common.dto.PrepareMessage;
import com.distributed2pc.common.dto.VoteMessage;
import com.distributed2pc.common.enums.NodeStatus;
import com.distributed2pc.common.enums.VoteResult;
import com.distributed2pc.coordinator.feature.participant.domain.RegisteredParticipant;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link ParticipantHttpClient}.
 */
@ExtendWith(MockitoExtension.class)
class ParticipantHttpClientTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    WebClient.Builder webClientBuilder;

    ParticipantHttpClient client;

    private static final RegisteredParticipant PARTICIPANT = new RegisteredParticipant("server-1", "localhost", 8081,
            NodeStatus.ONLINE);

    @BeforeEach
    void setUp() {
        client = new ParticipantHttpClient(webClientBuilder);
    }

    @Test
    void sendPrepare_givenSuccessfulResponse_returnsMono() {
        UUID txId = UUID.randomUUID();
        VoteMessage expected = new VoteMessage(txId, VoteResult.YES, "server-1");
        when(webClientBuilder.baseUrl(anyString()).build()
                .post().uri(anyString()).bodyValue(any()).retrieve()
                .bodyToMono(VoteMessage.class))
                .thenReturn(Mono.just(expected));

        StepVerifier.create(client.sendPrepare(PARTICIPANT, new PrepareMessage(txId, "value")))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void sendPrepare_givenError_propagatesError() {
        UUID txId = UUID.randomUUID();
        when(webClientBuilder.baseUrl(anyString()).build()
                .post().uri(anyString()).bodyValue(any()).retrieve()
                .bodyToMono(VoteMessage.class))
                .thenReturn(Mono.error(new RuntimeException("timeout")));

        StepVerifier.create(client.sendPrepare(PARTICIPANT, new PrepareMessage(txId, "value")))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void sendCommit_givenSuccessfulResponse_completesEmpty() {
        UUID txId = UUID.randomUUID();
        when(webClientBuilder.baseUrl(anyString()).build()
                .post().uri(anyString()).bodyValue(any()).retrieve()
                .bodyToMono(Void.class))
                .thenReturn(Mono.empty());

        StepVerifier.create(client.sendCommit(PARTICIPANT, new CommitMessage(txId)))
                .verifyComplete();
    }

    @Test
    void sendCommit_givenError_propagatesError() {
        UUID txId = UUID.randomUUID();
        when(webClientBuilder.baseUrl(anyString()).build()
                .post().uri(anyString()).bodyValue(any()).retrieve()
                .bodyToMono(Void.class))
                .thenReturn(Mono.error(new RuntimeException("connection refused")));

        StepVerifier.create(client.sendCommit(PARTICIPANT, new CommitMessage(txId)))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void sendAbort_givenSuccessfulResponse_completesEmpty() {
        UUID txId = UUID.randomUUID();
        when(webClientBuilder.baseUrl(anyString()).build()
                .post().uri(anyString()).bodyValue(any()).retrieve()
                .bodyToMono(Void.class))
                .thenReturn(Mono.empty());

        StepVerifier.create(client.sendAbort(PARTICIPANT, new AbortMessage(txId)))
                .verifyComplete();
    }

    @Test
    void sendAbort_givenError_propagatesError() {
        UUID txId = UUID.randomUUID();
        when(webClientBuilder.baseUrl(anyString()).build()
                .post().uri(anyString()).bodyValue(any()).retrieve()
                .bodyToMono(Void.class))
                .thenReturn(Mono.error(new RuntimeException("unreachable")));

        StepVerifier.create(client.sendAbort(PARTICIPANT, new AbortMessage(txId)))
                .expectError(RuntimeException.class)
                .verify();
    }
}
