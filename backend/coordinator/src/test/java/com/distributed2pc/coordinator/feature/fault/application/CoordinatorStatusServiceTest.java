package com.distributed2pc.coordinator.feature.fault.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.common.enums.NodeStatus;
import com.distributed2pc.coordinator.feature.fault.domain.CoordinatorNodeStatus;

/**
 * Unit tests for {@link CoordinatorStatusService}.
 */
@ExtendWith(MockitoExtension.class)
class CoordinatorStatusServiceTest {

    @Mock
    CoordinatorFaultService faultService;

    CoordinatorStatusService coordinatorStatusService;

    @BeforeEach
    void setUp() {
        coordinatorStatusService = new CoordinatorStatusService(faultService);
    }

    @Test
    void getStatus_givenNoActiveFaults_returnsOnlineWithEmptyFaultList() {
        when(faultService.listActiveFaults()).thenReturn(List.of());
        when(faultService.isActive(FaultType.CRASH)).thenReturn(false);

        CoordinatorNodeStatus result = coordinatorStatusService.getStatus();

        assertThat(result.serverId()).isEqualTo("coordinator");
        assertThat(result.status()).isEqualTo(NodeStatus.ONLINE);
        assertThat(result.activeFaults()).isEmpty();
    }

    @Test
    void getStatus_givenCrashActive_returnsStatusCrashed() {
        when(faultService.listActiveFaults()).thenReturn(List.of(FaultType.CRASH));
        when(faultService.isActive(FaultType.CRASH)).thenReturn(true);

        CoordinatorNodeStatus result = coordinatorStatusService.getStatus();

        assertThat(result.status()).isEqualTo(NodeStatus.CRASHED);
        assertThat(result.activeFaults()).containsExactly("CRASH");
    }

    @Test
    void getStatus_givenNonCrashFaultActive_returnsStatusDegraded() {
        when(faultService.listActiveFaults()).thenReturn(List.of(FaultType.NETWORK_DELAY));
        when(faultService.isActive(FaultType.CRASH)).thenReturn(false);

        CoordinatorNodeStatus result = coordinatorStatusService.getStatus();

        assertThat(result.status()).isEqualTo(NodeStatus.DEGRADED);
        assertThat(result.activeFaults()).containsExactly("NETWORK_DELAY");
    }

    @Test
    void getStatus_givenMultipleFaultsIncludingCrash_returnsCrashedWithAllFaults() {
        when(faultService.listActiveFaults()).thenReturn(List.of(FaultType.CRASH, FaultType.MESSAGE_LOSS));
        when(faultService.isActive(FaultType.CRASH)).thenReturn(true);

        CoordinatorNodeStatus result = coordinatorStatusService.getStatus();

        assertThat(result.status()).isEqualTo(NodeStatus.CRASHED);
        assertThat(result.activeFaults()).containsExactlyInAnyOrder("CRASH", "MESSAGE_LOSS");
    }

    @Test
    void getStatus_givenMultipleNonCrashFaults_returnsStatusDegraded() {
        when(faultService.listActiveFaults()).thenReturn(List.of(FaultType.NETWORK_DELAY, FaultType.MESSAGE_LOSS));
        when(faultService.isActive(FaultType.CRASH)).thenReturn(false);

        CoordinatorNodeStatus result = coordinatorStatusService.getStatus();

        assertThat(result.status()).isEqualTo(NodeStatus.DEGRADED);
        assertThat(result.activeFaults()).containsExactlyInAnyOrder("NETWORK_DELAY", "MESSAGE_LOSS");
    }
}
