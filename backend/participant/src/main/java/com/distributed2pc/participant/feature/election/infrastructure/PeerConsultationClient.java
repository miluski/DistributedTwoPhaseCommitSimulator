package com.distributed2pc.participant.feature.election.infrastructure;

import com.distributed2pc.common.dto.PeerLogEntryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * HTTP client used to query a peer participant's persisted log entry for a
 * given transaction during coordinator-failure election.
 */
@Slf4j
@Component
public class PeerConsultationClient {

    private final WebClient webClient;

    /**
     * @param webClientBuilder shared builder (configured to trust the dev TLS cert).
     */
    public PeerConsultationClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Fetches the persisted log entry for {@code transactionId} from the peer
     * at {@code peerBaseUrl}.
     *
     * @param peerBaseUrl   HTTPS base URL of the peer, e.g. {@code https://peer:8445}.
     * @param transactionId ID of the transaction to query.
     * @return a {@link Mono} emitting the peer's {@link PeerLogEntryDto}, or empty on error.
     */
    public Mono<PeerLogEntryDto> fetchLogEntry(String peerBaseUrl, UUID transactionId) {
        return webClient.get()
                .uri(peerBaseUrl + "/api/peers/log/" + transactionId)
                .retrieve()
                .bodyToMono(PeerLogEntryDto.class)
                .doOnError(e -> log.warn("Peer {} log query failed for {}: {}", peerBaseUrl, transactionId, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}
