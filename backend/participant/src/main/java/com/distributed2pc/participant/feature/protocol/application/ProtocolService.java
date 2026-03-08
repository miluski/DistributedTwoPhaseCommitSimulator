package com.distributed2pc.participant.feature.protocol.application;

import com.distributed2pc.common.dto.AbortMessage;
import com.distributed2pc.common.dto.CommitMessage;
import com.distributed2pc.common.dto.PrepareMessage;
import com.distributed2pc.common.dto.VoteMessage;
import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.common.enums.VoteResult;
import com.distributed2pc.participant.feature.fault.infrastructure.ForceAbortVoteFaultStrategy;
import com.distributed2pc.participant.feature.log.domain.LogEntry;
import com.distributed2pc.participant.feature.log.infrastructure.InMemoryTransactionLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Handles the 2PC protocol messages for this participant.
 *
 * <p>Each method writes a log entry before responding, ensuring durability of
 * the vote and decision for coordinator-failure recovery.
 */
@Slf4j
@Service
public class ProtocolService {

    private final String serverId;
    private final InMemoryTransactionLog transactionLog;
    private final ForceAbortVoteFaultStrategy forceAbortVoteFaultStrategy;

    public ProtocolService(
            @Value("${participant.server-id}") String serverId,
            InMemoryTransactionLog transactionLog,
            ForceAbortVoteFaultStrategy forceAbortVoteFaultStrategy) {
        this.serverId = serverId;
        this.transactionLog = transactionLog;
        this.forceAbortVoteFaultStrategy = forceAbortVoteFaultStrategy;
    }

    /**
     * Handles a PREPARE message by deciding the vote and persisting it.
     *
     * @param message the PREPARE message from the coordinator.
     * @return the participant's vote response.
     */
    public VoteMessage handlePrepare(PrepareMessage message) {
        VoteResult vote = determineVote();
        LogEntry entry = new LogEntry(message.transactionId(),
                TransactionStatus.PREPARING, message.value(), Instant.now());
        transactionLog.write(entry);
        log.info("[{}] PREPARE txId={} vote={}", serverId, message.transactionId(), vote);
        return new VoteMessage(message.transactionId(), vote, serverId);
    }

    /**
     * Handles a COMMIT message by updating the log to COMMITTED.
     *
     * @param message the COMMIT message from the coordinator.
     */
    public void handleCommit(CommitMessage message) {
        transactionLog.updatePhase(message.transactionId(), TransactionStatus.COMMITTED);
        log.info("[{}] COMMIT applied txId={}", serverId, message.transactionId());
    }

    /**
     * Handles an ABORT message by updating the log to ABORTED.
     *
     * @param message the ABORT message from the coordinator.
     */
    public void handleAbort(AbortMessage message) {
        transactionLog.updatePhase(message.transactionId(), TransactionStatus.ABORTED);
        log.info("[{}] ABORT applied txId={}", serverId, message.transactionId());
    }

    private VoteResult determineVote() {
        if (forceAbortVoteFaultStrategy.consumeNoVote()) {
            return VoteResult.NO;
        }
        return VoteResult.YES;
    }
}
