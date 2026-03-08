package com.distributed2pc.participant.feature.election.infrastructure;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.distributed2pc.common.dto.PeerLogEntryDto;
import com.distributed2pc.common.enums.TransactionStatus;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link PeerConsultationClient}.
 */
@ExtendWith(MockitoExtension.class)
class PeerConsultationClientTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    WebClient.Builder webClientBuilder;

    PeerConsultationClient client;

    @BeforeEach
    void setUp() {
        client = new PeerConsultationClient(webClientBuilder);
    }

    @Test
    void fetchLogEntry_givenPeerReturnsEntry_emitsEntry() {
        UUID txId = UUID.randomUUID();
        PeerLogEntryDto expected = new PeerLogEntryDto(txId, TransactionStatus.COMMITTED, "server-2", Instant.now());
        when(webClientBuilder.build()
                .get().uri(anyString()).retrieve()
                .bodyToMono(PeerLogEntryDto.class))
                .thenReturn(Mono.just(expected));

        StepVerifier.create(client.fetchLogEntry("https://peer:8445", txId))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void fetchLogEntry_givenPeerUnavailable_returnsEmpty() {
        UUID txId = UUID.randomUUID();
        when(webClientBuilder.build()
                .get().uri(anyString()).retrieve()
                .bodyToMono(PeerLogEntryDto.class))
                .thenReturn(Mono.error(new RuntimeException("peer unreachable")));

        StepVerifier.create(client.fetchLogEntry("https://peer:8445", txId))
                .verifyComplete();
    }
}
