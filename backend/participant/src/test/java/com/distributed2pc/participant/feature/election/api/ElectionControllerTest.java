package com.distributed2pc.participant.feature.election.api;

import com.distributed2pc.common.dto.PeerLogEntryDto;
import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.participant.feature.election.application.ElectionService;
import com.distributed2pc.participant.feature.log.domain.LogEntry;
import com.distributed2pc.participant.feature.log.infrastructure.InMemoryTransactionLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ElectionController}.
 */
@ExtendWith(MockitoExtension.class)
class ElectionControllerTest {

    @Mock
    InMemoryTransactionLog transactionLog;

    @Mock
    ElectionService electionService;

    ElectionController controller;

    @BeforeEach
    void setUp() {
        controller = new ElectionController(transactionLog, electionService, "server-1");
    }

    @Test
    void getLogEntry_givenKnownTransaction_returnsEntryDto() {
        UUID txId = UUID.randomUUID();
        LogEntry entry = new LogEntry(txId, TransactionStatus.PREPARING, "v", Instant.now());
        when(transactionLog.read(txId)).thenReturn(Optional.of(entry));

        ResponseEntity<PeerLogEntryDto> response = controller.getLogEntry(txId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().transactionId()).isEqualTo(txId);
        assertThat(response.getBody().phase()).isEqualTo(TransactionStatus.PREPARING);
        assertThat(response.getBody().serverId()).isEqualTo("server-1");
    }

    @Test
    void getLogEntry_givenUnknownTransaction_returns404() {
        UUID txId = UUID.randomUUID();
        when(transactionLog.read(txId)).thenReturn(Optional.empty());

        ResponseEntity<PeerLogEntryDto> response = controller.getLogEntry(txId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void runElection_givenTransaction_returnsResolvedStatus() {
        UUID txId = UUID.randomUUID();
        when(electionService.elect(txId)).thenReturn(TransactionStatus.COMMITTED);

        ResponseEntity<PeerLogEntryDto> response = controller.runElection(txId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().phase()).isEqualTo(TransactionStatus.COMMITTED);
        assertThat(response.getBody().serverId()).isEqualTo("server-1");
        assertThat(response.getBody().transactionId()).isEqualTo(txId);
    }

    @Test
    void runElection_givenUncertainOutcome_returnsAborted() {
        UUID txId = UUID.randomUUID();
        when(electionService.elect(txId)).thenReturn(TransactionStatus.ABORTED);

        ResponseEntity<PeerLogEntryDto> response = controller.runElection(txId);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().phase()).isEqualTo(TransactionStatus.ABORTED);
    }
}
