package com.distributed2pc.participant.feature.protocol.infrastructure;

import com.distributed2pc.common.dto.ParticipantRegistrationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Registers this participant with the coordinator on application startup.
 *
 * <p>Posts a {@link ParticipantRegistrationDto} to
 * {@code <coordinatorUrl>/api/participants/register}.  A failure is logged as a
 * warning rather than propagated, so the participant still starts even when the
 * coordinator is temporarily unavailable; re-registration can be retried later.
 */
@Slf4j
@Component
public class StartupRegistrationRunner implements ApplicationRunner {

    private final WebClient webClient;
    private final String coordinatorUrl;
    private final String serverId;
    private final String host;
    private final int port;

    /**
     * @param webClientBuilder shared WebClient builder (configured to trust the dev TLS cert).
     * @param coordinatorUrl   URL of the coordinator, e.g. {@code https://coordinator:8443}.
     * @param serverId         unique server identifier, e.g. {@code server-1}.
     * @param host             hostname to advertise to the coordinator.
     * @param port             HTTPS port to advertise to the coordinator.
     */
    public StartupRegistrationRunner(
            WebClient.Builder webClientBuilder,
            @Value("${participant.coordinator-url}") String coordinatorUrl,
            @Value("${participant.server-id}") String serverId,
            @Value("${participant.host:localhost}") String host,
            @Value("${server.port:8444}") int port) {
        this.webClient = webClientBuilder.build();
        this.coordinatorUrl = coordinatorUrl;
        this.serverId = serverId;
        this.host = host;
        this.port = port;
    }

    /**
     * Executes on startup – posts the registration DTO to the coordinator.
     *
     * @param args application arguments (unused).
     */
    @Override
    public void run(ApplicationArguments args) {
        ParticipantRegistrationDto dto = new ParticipantRegistrationDto(serverId, host, port);
        webClient.post()
                .uri(coordinatorUrl + "/api/participants/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(r -> log.info("Registered with coordinator as {} on port {}", serverId, port))
                .doOnError(e -> log.warn("Failed to register with coordinator: {}", e.getMessage()))
                .subscribe();
    }
}
