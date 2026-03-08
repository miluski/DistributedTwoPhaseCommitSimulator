package com.distributed2pc.participant.feature.fault.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.distributed2pc.common.dto.FaultRequest;
import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.participant.feature.fault.domain.ParticipantFaultStrategy;

/**
 * Unit tests for {@link FaultInjectionService}.
 */
class FaultInjectionServiceTest {

    Map<FaultType, ParticipantFaultStrategy> strategies;
    FaultInjectionService service;

    @BeforeEach
    void setUp() {
        strategies = new EnumMap<>(FaultType.class);
        for (FaultType type : FaultType.values()) {
            strategies.put(type, mock(ParticipantFaultStrategy.class));
        }
        service = new FaultInjectionService(strategies);
    }

    @ParameterizedTest
    @EnumSource(FaultType.class)
    void applyFault_givenEnableTrue_thenStrategyActivated(FaultType type) {
        FaultRequest request = new FaultRequest(type, true, Map.of());

        service.applyFault(request);

        verify(strategies.get(type)).activate(Map.of());
    }

    @ParameterizedTest
    @EnumSource(FaultType.class)
    void applyFault_givenEnableFalse_thenStrategyDeactivated(FaultType type) {
        FaultRequest request = new FaultRequest(type, false);

        service.applyFault(request);

        verify(strategies.get(type)).deactivate();
    }

    @Test
    void isActive_givenActiveStrategy_thenReturnsTrue() {
        when(strategies.get(FaultType.CRASH).isActive()).thenReturn(true);

        assertThat(service.isActive(FaultType.CRASH)).isTrue();
    }

    @Test
    void listActiveFaults_givenOneCrashActive_thenReturnsCrash() {
        when(strategies.get(FaultType.CRASH).isActive()).thenReturn(true);

        assertThat(service.listActiveFaults()).containsExactly(FaultType.CRASH);
    }

    @Test
    void applyFault_givenUnknownType_thenThrowsIllegalArgument() {
        FaultInjectionService emptyService = new FaultInjectionService(Map.of());
        FaultRequest request = new FaultRequest(FaultType.CRASH, true);

        assertThatThrownBy(() -> emptyService.applyFault(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
