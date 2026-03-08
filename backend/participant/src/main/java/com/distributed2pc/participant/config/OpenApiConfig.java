package com.distributed2pc.participant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * SpringDoc OpenAPI configuration for the 2PC Participant service.
 *
 * <p>Registers the {@link OpenAPI} bean consumed by SpringDoc to populate
 * the Swagger UI info section.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI metadata displayed in Swagger UI.
     *
     * @return the {@link OpenAPI} bean with title, description and version.
     */
    @Bean
    public OpenAPI participantOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("2PC Participant API")
                        .description("REST API uczestnika protokołu 2PC")
                        .version("1.0.0"));
    }
}
