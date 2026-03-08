package com.distributed2pc.participant.feature.fault.api;

import com.distributed2pc.common.dto.FaultRequest;
import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.participant.feature.fault.application.FaultInjectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FaultController}.
 */
@ExtendWith(MockitoExtension.class)
class FaultControllerTest {

    @Mock
    FaultInjectionService faultInjectionService;

    FaultController controller;

    @BeforeEach
    void setUp() {
        controller = new FaultController(faultInjectionService);
    }

    @Test
    void inject_givenEnabledRequest_delegatesToServiceAndReturnsInjectedMessage() {
        FaultRequest request = new FaultRequest(FaultType.CRASH, true, Map.of());

        ResponseEntity<Map<String, String>> response = controller.inject(request);

        verify(faultInjectionService).applyFault(request);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("message", "Fault CRASH injected");
    }

    @Test
    void inject_givenDisabledRequest_returnsClearedMessage() {
        FaultRequest request = new FaultRequest(FaultType.NETWORK_DELAY, false, Map.of());

        ResponseEntity<Map<String, String>> response = controller.inject(request);

        assertThat(response.getBody()).containsEntry("message", "Fault NETWORK_DELAY cleared");
    }

    @Test
    void clear_givenFaultType_callsServiceWithDisabledRequest() {
        ResponseEntity<Map<String, String>> response = controller.clear(FaultType.CRASH);

        verify(faultInjectionService).applyFault(any(FaultRequest.class));
        assertThat(response.getBody()).containsEntry("message", "Fault CRASH cleared");
    }

    @Test
    void listActive_givenActiveFaults_returnsListFromService() {
        when(faultInjectionService.listActiveFaults()).thenReturn(List.of(FaultType.CRASH, FaultType.NETWORK_DELAY));

        ResponseEntity<List<FaultType>> response = controller.listActive();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsExactly(FaultType.CRASH, FaultType.NETWORK_DELAY);
    }

    @Test
    void listActive_givenNoActiveFaults_returnsEmptyList() {
        when(faultInjectionService.listActiveFaults()).thenReturn(List.of());

        ResponseEntity<List<FaultType>> response = controller.listActive();

        assertThat(response.getBody()).isEmpty();
    }
}
