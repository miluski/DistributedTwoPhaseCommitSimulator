package com.distributed2pc.coordinator.feature.fault.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.distributed2pc.common.dto.FaultRequest;
import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.coordinator.feature.event.application.EventPublisher;
import com.distributed2pc.coordinator.feature.fault.domain.CoordinatorFaultStrategy;

/**
 * Unit tests for {@link CoordinatorFaultService}.
 */
@ExtendWith(MockitoExtension.class)
class CoordinatorFaultServiceTest {

    @Mock
    CoordinatorFaultStrategy crashStrategy;

    @Mock
    CoordinatorFaultStrategy delayStrategy;

    @Mock
    EventPublisher eventPublisher;

    CoordinatorFaultService service;

    @BeforeEach
    void setUp() {
        Map<FaultType, CoordinatorFaultStrategy> strategies = Map.of(
                FaultType.CRASH, crashStrategy,
                FaultType.NETWORK_DELAY, delayStrategy);
        service = new CoordinatorFaultService(strategies, eventPublisher);
    }

    @Test
    void applyFault_givenEnabledRequest_activatesStrategyAndPublishesEvent() {
        FaultRequest request = new FaultRequest(FaultType.CRASH, true, Map.of());

        service.applyFault(request);

        verify(crashStrategy).activate(Map.of());
        verify(eventPublisher).publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void applyFault_givenDisabledRequest_deactivatesStrategyAndPublishesEvent() {
        FaultRequest request = new FaultRequest(FaultType.CRASH, false, Map.of());

        service.applyFault(request);

        verify(crashStrategy).deactivate();
        verify(eventPublisher).publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void applyFault_givenUnregisteredFaultType_throwsIllegalArgumentException() {
        FaultRequest request = new FaultRequest(FaultType.TRANSIENT, true, Map.of());

        assertThatThrownBy(() -> service.applyFault(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TRANSIENT");
    }

    @Test
    void isActive_givenActiveStrategy_returnsTrue() {
        when(crashStrategy.isActive()).thenReturn(true);

        assertThat(service.isActive(FaultType.CRASH)).isTrue();
    }

    @Test
    void isActive_givenInactiveStrategy_returnsFalse() {
        when(crashStrategy.isActive()).thenReturn(false);

        assertThat(service.isActive(FaultType.CRASH)).isFalse();
    }

    @Test
    void applyFault_givenNetworkDelayWithParameters_passesParametersToStrategy() {
        Map<String, Object> params = Map.of("delayMs", 500);
        FaultRequest request = new FaultRequest(FaultType.NETWORK_DELAY, true, params);

        service.applyFault(request);

        verify(delayStrategy).activate(params);
    }

    @Test
    void listActiveFaults_givenNoActiveFaults_returnsEmptyList() {
        when(crashStrategy.isActive()).thenReturn(false);
        when(delayStrategy.isActive()).thenReturn(false);

        assertThat(service.listActiveFaults()).isEmpty();
    }

    @Test
    void listActiveFaults_givenCrashActive_returnsListWithCrash() {
        when(crashStrategy.isActive()).thenReturn(true);
        when(delayStrategy.isActive()).thenReturn(false);

        assertThat(service.listActiveFaults()).containsExactly(FaultType.CRASH);
    }

    @Test
    void listActiveFaults_givenAllFaultsActive_returnsAllActiveFaultTypes() {
        when(crashStrategy.isActive()).thenReturn(true);
        when(delayStrategy.isActive()).thenReturn(true);

        assertThat(service.listActiveFaults())
                .containsExactlyInAnyOrder(FaultType.CRASH, FaultType.NETWORK_DELAY);
    }
}
