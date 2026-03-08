package com.distributed2pc.coordinator.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.coordinator.feature.fault.domain.CoordinatorFaultStrategy;
import com.distributed2pc.coordinator.feature.fault.infrastructure.CrashCoordinatorFaultStrategy;
import com.distributed2pc.coordinator.feature.fault.infrastructure.CrashCoordinatorInterceptor;
import com.distributed2pc.coordinator.feature.fault.infrastructure.DelayedDecisionFaultStrategy;
import com.distributed2pc.coordinator.feature.fault.infrastructure.PartialSendFaultStrategy;

/**
 * Spring configuration that wires {@link CoordinatorFaultStrategy}
 * implementations
 * into a {@code Map<FaultType, CoordinatorFaultStrategy>} bean consumed by the
 * fault-injection service, and registers the crash interceptor.
 */
@Configuration
public class CoordinatorConfig implements WebMvcConfigurer {

    /**
     * Exposes all coordinator fault strategies keyed by {@link FaultType}.
     *
     * @param crash           strategy simulating a coordinator crash.
     * @param partialSend     strategy simulating partial Phase-2 delivery.
     * @param delayedDecision strategy simulating a stalled decision broadcast.
     * @return immutable strategy map.
     */
    @Bean
    public Map<FaultType, CoordinatorFaultStrategy> coordinatorFaultStrategies(
            CrashCoordinatorFaultStrategy crash,
            PartialSendFaultStrategy partialSend,
            DelayedDecisionFaultStrategy delayedDecision) {
        return Map.of(
                FaultType.CRASH, crash,
                FaultType.MESSAGE_LOSS, partialSend,
                FaultType.NETWORK_DELAY, delayedDecision);
    }

    /**
     * Singleton bean for the crash-fault strategy so the interceptor and the
     * strategy map share the same instance.
     *
     * @return a new {@link CrashCoordinatorFaultStrategy}.
     */
    @Bean
    public CrashCoordinatorFaultStrategy crashCoordinatorFaultStrategy() {
        return new CrashCoordinatorFaultStrategy();
    }

    /**
     * Singleton bean for the partial-send fault strategy.
     *
     * @return a new {@link PartialSendFaultStrategy}.
     */
    @Bean
    public PartialSendFaultStrategy partialSendFaultStrategy() {
        return new PartialSendFaultStrategy();
    }

    /**
     * Singleton bean for the delayed-decision fault strategy.
     *
     * @return a new {@link DelayedDecisionFaultStrategy}.
     */
    @Bean
    public DelayedDecisionFaultStrategy delayedDecisionFaultStrategy() {
        return new DelayedDecisionFaultStrategy();
    }

    /**
     * Registers the crash interceptor on all API paths.
     *
     * @param registry Spring's interceptor registry.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CrashCoordinatorInterceptor(crashCoordinatorFaultStrategy()))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/coordinator/status", "/api/coordinator/fault");
    }
}
