package com.distributed2pc.coordinator.feature.transaction.application;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.distributed2pc.common.dto.AbortMessage;
import com.distributed2pc.common.dto.CommitMessage;
import com.distributed2pc.common.dto.PrepareMessage;
import com.distributed2pc.common.dto.SystemEventDto;
import com.distributed2pc.common.dto.VoteMessage;
import com.distributed2pc.common.enums.EventType;
import com.distributed2pc.common.enums.NodeRole;
import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.common.enums.VoteResult;
import com.distributed2pc.coordinator.feature.event.application.EventPublisher;
import com.distributed2pc.coordinator.feature.fault.infrastructure.DelayedDecisionFaultStrategy;
import com.distributed2pc.coordinator.feature.fault.infrastructure.PartialSendFaultStrategy;
import com.distributed2pc.coordinator.feature.participant.application.ParticipantRegistrationService;
import com.distributed2pc.coordinator.feature.participant.domain.ParticipantRepository;
import com.distributed2pc.coordinator.feature.participant.domain.RegisteredParticipant;
import com.distributed2pc.coordinator.feature.transaction.domain.CoordinatorTransaction;
import com.distributed2pc.coordinator.feature.transaction.domain.TransactionRepository;
import com.distributed2pc.coordinator.feature.transaction.infrastructure.ParticipantHttpClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Orchestrates the Two-Phase Commit protocol from the coordinator's
 * perspective.
 *
 * <p>
 * Responsibilities:
 * <ol>
 * <li>Create and persist a {@link CoordinatorTransaction}.</li>
 * <li>Phase 1: broadcast PREPARE to all participants in parallel; collect
 * votes.</li>
 * <li>Phase 2: broadcast COMMIT (all YES) or ABORT (any NO / timeout) in
 * parallel.</li>
 * <li>Publish {@link SystemEventDto} events at each protocol step.</li>
 * </ol>
 */
@Slf4j
@Service
public class TwoPhaseCommitService {

    private final TransactionRepository transactionRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantRegistrationService participantRegistrationService;
    private final ParticipantHttpClient httpClient;
    private final EventPublisher eventPublisher;
    private final PartialSendFaultStrategy partialSendFaultStrategy;
    private final DelayedDecisionFaultStrategy delayedDecisionFaultStrategy;

    public TwoPhaseCommitService(TransactionRepository transactionRepository,
            ParticipantRepository participantRepository,
            ParticipantRegistrationService participantRegistrationService,
            ParticipantHttpClient httpClient,
            EventPublisher eventPublisher,
            PartialSendFaultStrategy partialSendFaultStrategy,
            DelayedDecisionFaultStrategy delayedDecisionFaultStrategy) {
        this.transactionRepository = transactionRepository;
        this.participantRepository = participantRepository;
        this.participantRegistrationService = participantRegistrationService;
        this.httpClient = httpClient;
        this.eventPublisher = eventPublisher;
        this.partialSendFaultStrategy = partialSendFaultStrategy;
        this.delayedDecisionFaultStrategy = delayedDecisionFaultStrategy;
    }

    /**
     * Initiates a new 2PC transaction for the given value.
     *
     * @param value the payload to commit across all participants.
     * @return a Mono that completes with the final transaction state.
     */
    public Mono<CoordinatorTransaction> initiate(String value) {
        CoordinatorTransaction transaction = new CoordinatorTransaction(value);
        transactionRepository.save(transaction);
        publishTransactionStarted(transaction);
        return runPhaseOne(transaction)
                .flatMap(votes -> runPhaseTwo(transaction, votes));
    }

    /**
     * Retrieves a transaction by its unique identifier.
     *
     * @param id the transaction UUID.
     * @return the transaction wrapped in an Optional.
     */
    public java.util.Optional<CoordinatorTransaction> findById(UUID id) {
        return transactionRepository.findById(id);
    }

    /**
     * Returns all transactions recorded by the coordinator.
     *
     * @return immutable list of all transactions.
     */
    public List<CoordinatorTransaction> findAll() {
        return transactionRepository.findAll();
    }

    private Mono<List<VoteMessage>> runPhaseOne(CoordinatorTransaction transaction) {
        transaction.setStatus(TransactionStatus.PREPARING);
        publishPrepareSent(transaction);
        List<RegisteredParticipant> participants = participantRepository.findAll();
        PrepareMessage message = new PrepareMessage(transaction.getId(), transaction.getValue());
        return Flux.fromIterable(participants)
                .flatMap(p -> httpClient.sendPrepare(p, message)
                        .doOnNext(vote -> {
                            recordVote(transaction, vote);
                            participantRegistrationService.markOnline(p.serverId());
                        })
                        .doOnError(e -> participantRegistrationService.markCrashed(p.serverId()))
                        .onErrorReturn(new VoteMessage(transaction.getId(), VoteResult.NO, p.serverId())))
                .collectList()
                .doOnSuccess(ignored -> publishAllVotesCollected(transaction));
    }

    private Mono<CoordinatorTransaction> runPhaseTwo(CoordinatorTransaction tx,
            List<VoteMessage> votes) {
        long delayMs = delayedDecisionFaultStrategy.getDelayMs();
        boolean allYes = votes.stream().allMatch(v -> v.vote() == VoteResult.YES);
        Mono<CoordinatorTransaction> decision = allYes ? broadcastCommit(tx) : broadcastAbort(tx);
        return delayMs > 0 ? Mono.delay(Duration.ofMillis(delayMs)).then(decision) : decision;
    }

    private Mono<CoordinatorTransaction> broadcastCommit(CoordinatorTransaction tx) {
        publishDecisionMade(tx, TransactionStatus.COMMITTED);
        CommitMessage message = new CommitMessage(tx.getId());
        boolean partial = partialSendFaultStrategy.isActive();
        return Flux.fromIterable(participantRepository.findAll())
                .filter(p -> !partial || partialSendFaultStrategy.consumeSend())
                .flatMap(p -> httpClient.sendCommit(p, message).onErrorComplete())
                .then(applyDecision(tx, TransactionStatus.COMMITTED));
    }

    private Mono<CoordinatorTransaction> broadcastAbort(CoordinatorTransaction tx) {
        publishDecisionMade(tx, TransactionStatus.ABORTED);
        AbortMessage message = new AbortMessage(tx.getId());
        boolean partial = partialSendFaultStrategy.isActive();
        return Flux.fromIterable(participantRepository.findAll())
                .filter(p -> !partial || partialSendFaultStrategy.consumeSend())
                .flatMap(p -> httpClient.sendAbort(p, message).onErrorComplete())
                .then(applyDecision(tx, TransactionStatus.ABORTED));
    }

    private Mono<CoordinatorTransaction> applyDecision(CoordinatorTransaction tx,
            TransactionStatus decision) {
        tx.applyDecision(decision);
        transactionRepository.updateStatus(tx.getId(), decision);
        publishTransactionCompleted(tx);
        log.info("Transaction {} {}", tx.getId(), decision);
        return Mono.just(tx);
    }

    private void recordVote(CoordinatorTransaction tx, VoteMessage vote) {
        tx.recordVote(vote.serverId(), vote.vote());
        eventPublisher.publish(SystemEventDto.txEvent(EventType.VOTE_RECEIVED,
                tx.getId(), vote.serverId(), NodeRole.COORDINATOR.getValue(), Map.of("vote", vote.vote())));
    }

    private void publishTransactionStarted(CoordinatorTransaction tx) {
        eventPublisher.publish(SystemEventDto.txEvent(EventType.TRANSACTION_STARTED,
                tx.getId(), NodeRole.COORDINATOR.getValue(), null, Map.of("value", tx.getValue())));
    }

    private void publishPrepareSent(CoordinatorTransaction tx) {
        eventPublisher.publish(SystemEventDto.txEvent(EventType.PREPARE_SENT,
                tx.getId(), NodeRole.COORDINATOR.getValue(), "all", Map.of()));
    }

    private void publishDecisionMade(CoordinatorTransaction tx, TransactionStatus decision) {
        Map<String, VoteResult> voteMap = tx.getVotes();
        long yesCount = countVotes(voteMap, VoteResult.YES);
        long noCount = countVotes(voteMap, VoteResult.NO);
        eventPublisher.publish(SystemEventDto.txEvent(EventType.DECISION_MADE,
                tx.getId(), NodeRole.COORDINATOR.getValue(), "all",
                Map.<String, Object>of("decision", decision, "yesCount", yesCount, "noCount", noCount)));
    }

    private void publishAllVotesCollected(CoordinatorTransaction tx) {
        Map<String, VoteResult> voteMap = tx.getVotes();
        long yesCount = countVotes(voteMap, VoteResult.YES);
        long noCount = countVotes(voteMap, VoteResult.NO);
        eventPublisher.publish(SystemEventDto.txEvent(EventType.ALL_VOTES_COLLECTED,
                tx.getId(), NodeRole.COORDINATOR.getValue(), null,
                Map.<String, Object>of("yesCount", yesCount, "noCount", noCount)));
    }

    private long countVotes(Map<String, VoteResult> votes, VoteResult target) {
        return votes.values().stream().filter(v -> v == target).count();
    }

    private void publishTransactionCompleted(CoordinatorTransaction tx) {
        eventPublisher.publish(SystemEventDto.txEvent(EventType.TRANSACTION_COMPLETED,
                tx.getId(), NodeRole.COORDINATOR.getValue(), null, Map.of("status", tx.getStatus())));
    }
}
