package com.distributed2pc.coordinator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the 2PC Coordinator service.
 *
 * <p>The coordinator is responsible for:
 * <ul>
 *   <li>Initiating transactions and driving the Two-Phase Commit protocol.</li>
 *   <li>Registering participants and tracking their health.</li>
 *   <li>Broadcasting system events to the React UI via WebSocket (STOMP).</li>
 *   <li>Exposing fault-injection endpoints for the coordinator itself.</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
public class CoordinatorApplication {

 public static void main(String[] args) {
  SpringApplication.run(CoordinatorApplication.class, args);
 }
}
