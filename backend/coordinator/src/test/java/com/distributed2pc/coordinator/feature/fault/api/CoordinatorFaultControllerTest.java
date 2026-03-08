package com.distributed2pc.coordinator.feature.fault.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.distributed2pc.common.dto.FaultRequest;
import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.coordinator.feature.fault.application.CoordinatorFaultService;

/**
 * Unit tests for {@link CoordinatorFaultController}.
 */
@ExtendWith(MockitoExtension.class)
class CoordinatorFaultControllerTest {

    @Mock
    CoordinatorFaultService faultService;

    CoordinatorFaultController controller;

    @BeforeEach
    void setUp() {
        controller = new CoordinatorFaultController(faultService);
    }

    @Test
    void applyFault_givenEnabledRequest_delegatesToServiceAndReturnsInjectedMessage() {
        FaultRequest request = new FaultRequest(FaultType.CRASH, true, Map.of());

        ResponseEntity<Map<String, String>> response = controller.applyFault(request);

        verify(faultService).applyFault(request);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("message", "Fault CRASH injected on coordinator");
    }

    @Test
    void applyFault_givenDisabledRequest_returnsCleared() {
        FaultRequest request = new FaultRequest(FaultType.NETWORK_DELAY, false, Map.of());

        ResponseEntity<Map<String, String>> response = controller.applyFault(request);

        verify(faultService).applyFault(request);
        assertThat(response.getBody()).containsEntry("message", "Fault NETWORK_DELAY cleared on coordinator");
    }
}
