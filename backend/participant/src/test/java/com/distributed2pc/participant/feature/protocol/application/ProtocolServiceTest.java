package com.distributed2pc.participant.feature.protocol.application;

import com.distributed2pc.common.dto.AbortMessage;
import com.distributed2pc.common.dto.CommitMessage;
import com.distributed2pc.common.dto.PrepareMessage;
import com.distributed2pc.common.dto.VoteMessage;
import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.common.enums.VoteResult;
import com.distributed2pc.participant.feature.fault.infrastructure.ForceAbortVoteFaultStrategy;
import com.distributed2pc.participant.feature.log.infrastructure.InMemoryTransactionLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProtocolService}.
 */
@ExtendWith(MockitoExtension.class)
class ProtocolServiceTest {

    @Mock
    InMemoryTransactionLog transactionLog;

    @Mock
    ForceAbortVoteFaultStrategy forceAbortVoteFaultStrategy;

    ProtocolService service;

    @BeforeEach
    void setUp() {
        service = new ProtocolService("server-1", transactionLog, forceAbortVoteFaultStrategy);
    }

    @Test
    void handlePrepare_givenNoActiveFault_writesLogAndVotesYes() {
        UUID txId = UUID.randomUUID();
        PrepareMessage message = new PrepareMessage(txId, "payload");
        when(forceAbortVoteFaultStrategy.consumeNoVote()).thenReturn(false);

        VoteMessage vote = service.handlePrepare(message);

        assertThat(vote.vote()).isEqualTo(VoteResult.YES);
        assertThat(vote.transactionId()).isEqualTo(txId);
        assertThat(vote.serverId()).isEqualTo("server-1");
        verify(transactionLog).write(any());
    }

    @Test
    void handlePrepare_givenForceAbortFaultActive_votesNo() {
        PrepareMessage message = new PrepareMessage(UUID.randomUUID(), "payload");
        when(forceAbortVoteFaultStrategy.consumeNoVote()).thenReturn(true);

        VoteMessage vote = service.handlePrepare(message);

        assertThat(vote.vote()).isEqualTo(VoteResult.NO);
    }

    @Test
    void handlePrepare_givenMessage_writesPreparingEntryToLog() {
        UUID txId = UUID.randomUUID();
        PrepareMessage message = new PrepareMessage(txId, "my-value");
        when(forceAbortVoteFaultStrategy.consumeNoVote()).thenReturn(false);

        service.handlePrepare(message);

        verify(transactionLog).write(argThat(entry ->
                entry.transactionId().equals(txId) &&
                entry.phase() == TransactionStatus.PREPARING &&
                entry.value().equals("my-value")
        ));
    }

    @Test
    void handleCommit_givenMessage_updatesLogToCommitted() {
        UUID txId = UUID.randomUUID();
        CommitMessage message = new CommitMessage(txId);

        service.handleCommit(message);

        verify(transactionLog).updatePhase(txId, TransactionStatus.COMMITTED);
    }

    @Test
    void handleAbort_givenMessage_updatesLogToAborted() {
        UUID txId = UUID.randomUUID();
        AbortMessage message = new AbortMessage(txId);

        service.handleAbort(message);

        verify(transactionLog).updatePhase(txId, TransactionStatus.ABORTED);
    }
}
