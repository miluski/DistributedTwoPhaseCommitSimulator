package com.distributed2pc.participant.feature.protocol.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.participant.feature.fault.application.FaultInjectionService;
import com.distributed2pc.participant.feature.log.infrastructure.InMemoryTransactionLog;

/**
 * Unit tests for {@link StatusController}.
 */
@ExtendWith(MockitoExtension.class)
class StatusControllerTest {

    private static final String SERVER_ID = "participant-1";

    @Mock
    FaultInjectionService faultService;

    @Mock
    InMemoryTransactionLog transactionLog;

    StatusController controller;

    @BeforeEach
    void setUp() {
        when(transactionLog.getLatestCommittedValue()).thenReturn(Optional.empty());
        controller = new StatusController(faultService, SERVER_ID, transactionLog);
    }

    @Test
    void getStatus_givenNoActiveFaults_returnsOnlineStatus() {
        when(faultService.listActiveFaults()).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "ONLINE");
    }

    @Test
    void getStatus_givenCrashFaultActive_returnsCrashedStatus() {
        when(faultService.listActiveFaults()).thenReturn(List.of(FaultType.CRASH));

        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        assertThat(response.getBody()).containsEntry("status", "CRASHED");
    }

    @Test
    void getStatus_givenActiveFaults_includesFaultNamesInResponse() {
        when(faultService.listActiveFaults()).thenReturn(List.of(FaultType.NETWORK_DELAY));

        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        @SuppressWarnings("unchecked")
        List<String> faults = (List<String>) response.getBody().get("activeFaults");
        assertThat(faults).containsExactly("NETWORK_DELAY");
    }

    @Test
    void getStatus_givenAnyState_includesServerIdInResponse() {
        when(faultService.listActiveFaults()).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        assertThat(response.getBody()).containsEntry("serverId", SERVER_ID);
    }

    @Test
    void getStatus_givenCommittedValue_includesValueInResponse() {
        when(faultService.listActiveFaults()).thenReturn(List.of());
        when(transactionLog.getLatestCommittedValue()).thenReturn(Optional.of("hello"));

        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        assertThat(response.getBody()).containsEntry("committedValue", "hello");
    }

    @Test
    void getStatus_givenNoCommittedValue_includesNullValueInResponse() {
        when(faultService.listActiveFaults()).thenReturn(List.of());
        when(transactionLog.getLatestCommittedValue()).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        assertThat(response.getBody()).containsKey("committedValue");
        assertThat(response.getBody().get("committedValue")).isNull();
    }
}
