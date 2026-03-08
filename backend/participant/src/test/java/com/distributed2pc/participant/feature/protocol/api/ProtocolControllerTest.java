package com.distributed2pc.participant.feature.protocol.api;

import com.distributed2pc.common.dto.AbortMessage;
import com.distributed2pc.common.dto.CommitMessage;
import com.distributed2pc.common.dto.PrepareMessage;
import com.distributed2pc.common.dto.VoteMessage;
import com.distributed2pc.common.enums.VoteResult;
import com.distributed2pc.participant.feature.protocol.application.ProtocolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProtocolController}.
 */
@ExtendWith(MockitoExtension.class)
class ProtocolControllerTest {

    @Mock
    ProtocolService protocolService;

    ProtocolController controller;

    @BeforeEach
    void setUp() {
        controller = new ProtocolController(protocolService);
    }

    @Test
    void prepare_givenValidMessage_returnsVoteResponse() {
        UUID txId = UUID.randomUUID();
        PrepareMessage message = new PrepareMessage(txId, "value");
        VoteMessage vote = new VoteMessage(txId, VoteResult.YES, "server-1");
        when(protocolService.handlePrepare(message)).thenReturn(vote);

        ResponseEntity<VoteMessage> response = controller.prepare(message);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(vote);
    }

    @Test
    void commit_givenValidMessage_returns204() {
        UUID txId = UUID.randomUUID();
        CommitMessage message = new CommitMessage(txId);

        ResponseEntity<Void> response = controller.commit(message);

        verify(protocolService).handleCommit(message);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void abort_givenValidMessage_returns204() {
        UUID txId = UUID.randomUUID();
        AbortMessage message = new AbortMessage(txId);

        ResponseEntity<Void> response = controller.abort(message);

        verify(protocolService).handleAbort(message);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
