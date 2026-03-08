package com.distributed2pc.coordinator.feature.transaction.infrastructure;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.distributed2pc.common.dto.AbortMessage;
import com.distributed2pc.common.dto.CommitMessage;
import com.distributed2pc.common.dto.PrepareMessage;
import com.distributed2pc.common.dto.VoteMessage;
import com.distributed2pc.coordinator.feature.participant.domain.RegisteredParticipant;
import com.distributed2pc.coordinator.feature.transaction.domain.ProtocolTimeout;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * HTTP client for sending 2PC protocol messages to a single participant.
 *
 * <p>
 * Each method returns a {@link Mono} that can be composed into parallel
 * flows by the coordinator service. Errors are logged and propagated as
 * reactive errors for the caller to handle.
 */
@Slf4j
@Component
public class ParticipantHttpClient {

    private final WebClient.Builder webClientBuilder;

    public ParticipantHttpClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * Sends a PREPARE message to the given participant and returns its vote.
     *
     * @param participant the target participant.
     * @param message     the prepare message payload.
     * @return a Mono emitting the participant's vote response.
     */
    public Mono<VoteMessage> sendPrepare(RegisteredParticipant participant, PrepareMessage message) {
        return webClientBuilder.baseUrl(participant.baseUrl()).build()
                .post()
                .uri("/api/2pc/prepare")
                .bodyValue(message)
                .retrieve()
                .bodyToMono(VoteMessage.class)
                .timeout(ProtocolTimeout.PARTICIPANT_REQUEST.getDuration())
                .doOnError(e -> log.error("PREPARE failed for {}: {}", participant.serverId(), e.getMessage()));
    }

    /**
     * Sends a COMMIT message to the given participant.
     *
     * @param participant the target participant.
     * @param message     the commit message payload.
     * @return a Mono that completes when the participant acks.
     */
    public Mono<Void> sendCommit(RegisteredParticipant participant, CommitMessage message) {
        return webClientBuilder.baseUrl(participant.baseUrl()).build()
                .post()
                .uri("/api/2pc/commit")
                .bodyValue(message)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(ProtocolTimeout.PARTICIPANT_REQUEST.getDuration())
                .doOnError(e -> log.error("COMMIT failed for {}: {}", participant.serverId(), e.getMessage()));
    }

    /**
     * Sends an ABORT message to the given participant.
     *
     * @param participant the target participant.
     * @param message     the abort message payload.
     * @return a Mono that completes when the participant acks.
     */
    public Mono<Void> sendAbort(RegisteredParticipant participant, AbortMessage message) {
        return webClientBuilder.baseUrl(participant.baseUrl()).build()
                .post()
                .uri("/api/2pc/abort")
                .bodyValue(message)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(ProtocolTimeout.PARTICIPANT_REQUEST.getDuration())
                .doOnError(e -> log.error("ABORT failed for {}: {}", participant.serverId(), e.getMessage()));
    }
}
