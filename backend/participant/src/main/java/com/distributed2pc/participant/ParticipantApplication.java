package com.distributed2pc.participant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for a 2PC Participant service instance.
 *
 * <p>Multiple instances of this application are launched with different
 * {@code SERVER_ID} and {@code SERVER_PORT} environment variables to form
 * the six-participant system described in the project requirements.
 *
 * <p>Each participant:
 * <ul>
 *   <li>Registers itself with the coordinator at startup.</li>
 *   <li>Handles PREPARE / COMMIT / ABORT messages from the coordinator.</li>
 *   <li>Persists transaction log entries for crash-recovery.</li>
 *   <li>Participates in coordinator-failure election via peer HTTP calls.</li>
 *   <li>Exposes fault injection endpoints for the UI.</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
public class ParticipantApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParticipantApplication.class, args);
    }
}
