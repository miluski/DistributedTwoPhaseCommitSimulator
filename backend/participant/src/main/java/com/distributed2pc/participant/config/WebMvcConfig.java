package com.distributed2pc.participant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.distributed2pc.participant.feature.fault.infrastructure.CrashInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * Registers Spring MVC interceptors for the participant service.
 *
 * <p>
 * The {@link CrashInterceptor} is applied to all endpoints so that any
 * active CRASH fault is enforced regardless of which controller handles the
 * request.
 */
@RequiredArgsConstructor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CrashInterceptor crashInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(crashInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/faults", "/api/faults/**", "/api/status");
    }
}
