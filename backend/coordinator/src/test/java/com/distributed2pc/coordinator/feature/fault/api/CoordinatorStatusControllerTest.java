package com.distributed2pc.coordinator.feature.fault.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.distributed2pc.common.enums.NodeStatus;
import com.distributed2pc.coordinator.feature.fault.application.CoordinatorStatusService;
import com.distributed2pc.coordinator.feature.fault.domain.CoordinatorNodeStatus;

/**
 * Unit tests for {@link CoordinatorStatusController}.
 */
@ExtendWith(MockitoExtension.class)
class CoordinatorStatusControllerTest {

    @Mock
    CoordinatorStatusService coordinatorStatusService;

    CoordinatorStatusController controller;

    @BeforeEach
    void setUp() {
        controller = new CoordinatorStatusController(coordinatorStatusService);
    }

    @Test
    void getStatus_givenNoActiveFaults_returnsOnlineResponse() {
        CoordinatorNodeStatus expected = new CoordinatorNodeStatus("coordinator", NodeStatus.ONLINE, List.of());
        when(coordinatorStatusService.getStatus()).thenReturn(expected);

        ResponseEntity<CoordinatorNodeStatus> response = controller.getStatus();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(coordinatorStatusService).getStatus();
    }

    @Test
    void getStatus_givenCrashActive_returnsCrashedResponse() {
        CoordinatorNodeStatus expected = new CoordinatorNodeStatus("coordinator", NodeStatus.CRASHED, List.of("CRASH"));
        when(coordinatorStatusService.getStatus()).thenReturn(expected);

        ResponseEntity<CoordinatorNodeStatus> response = controller.getStatus();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void getStatus_givenDegradedStatus_returnsDegradedResponse() {
        CoordinatorNodeStatus expected = new CoordinatorNodeStatus("coordinator", NodeStatus.DEGRADED,
                List.of("NETWORK_DELAY"));
        when(coordinatorStatusService.getStatus()).thenReturn(expected);

        ResponseEntity<CoordinatorNodeStatus> response = controller.getStatus();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void getStatus_delegatesExclusivelyToCoordinatorStatusService() {
        CoordinatorNodeStatus expected = new CoordinatorNodeStatus("coordinator", NodeStatus.ONLINE, List.of());
        when(coordinatorStatusService.getStatus()).thenReturn(expected);

        controller.getStatus();

        verify(coordinatorStatusService).getStatus();
    }
}
