package com.distributed2pc.coordinator.feature.transaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.distributed2pc.common.dto.SystemEventDto;
import com.distributed2pc.common.dto.VoteMessage;
import com.distributed2pc.common.enums.EventType;
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

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link TwoPhaseCommitService}.
 */
@ExtendWith(MockitoExtension.class)
class TwoPhaseCommitServiceTest {

    @Mock
    TransactionRepository transactionRepository;
    @Mock
    ParticipantRepository participantRepository;
    @Mock
    ParticipantRegistrationService participantRegistrationService;
    @Mock
    ParticipantHttpClient httpClient;
    @Mock
    EventPublisher eventPublisher;
    @Mock
    PartialSendFaultStrategy partialSendFaultStrategy;
    @Mock
    DelayedDecisionFaultStrategy delayedDecisionFaultStrategy;

    TwoPhaseCommitService service;

    private static final RegisteredParticipant PARTICIPANT_1 = new RegisteredParticipant("server-1", "localhost", 8081,
            com.distributed2pc.common.enums.NodeStatus.ONLINE);
    private static final RegisteredParticipant PARTICIPANT_2 = new RegisteredParticipant("server-2", "localhost", 8082,
            com.distributed2pc.common.enums.NodeStatus.ONLINE);

    @BeforeEach
    void setUp() {
        service = new TwoPhaseCommitService(
                transactionRepository, participantRepository, participantRegistrationService,
                httpClient, eventPublisher, partialSendFaultStrategy, delayedDecisionFaultStrategy);
    }

    @Test
    void initiate_givenAllYesVotes_thenTransactionCommitted() {
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1));
        when(httpClient.sendPrepare(eq(PARTICIPANT_1), any()))
                .thenReturn(Mono.just(new VoteMessage(UUID.randomUUID(), VoteResult.YES, "server-1")));
        when(httpClient.sendCommit(eq(PARTICIPANT_1), any())).thenReturn(Mono.empty());

        CoordinatorTransaction result = service.initiate("test-value").block();

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMMITTED);
    }

    @Test
    void initiate_givenOneNoVote_thenTransactionAborted() {
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1));
        when(httpClient.sendPrepare(eq(PARTICIPANT_1), any()))
                .thenReturn(Mono.just(new VoteMessage(UUID.randomUUID(), VoteResult.NO, "server-1")));
        when(httpClient.sendAbort(eq(PARTICIPANT_1), any())).thenReturn(Mono.empty());

        CoordinatorTransaction result = service.initiate("test-value").block();

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.ABORTED);
    }

    @Test
    void initiate_givenParticipantTimeout_thenTransactionAborted() {
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1));
        when(httpClient.sendPrepare(eq(PARTICIPANT_1), any()))
                .thenReturn(Mono.error(new RuntimeException("timeout")));
        when(httpClient.sendAbort(eq(PARTICIPANT_1), any())).thenReturn(Mono.empty());

        CoordinatorTransaction result = service.initiate("test-value").block();

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.ABORTED);
    }

    @Test
    void initiate_givenAllYesVotes_thenTransactionPersistedWithCommittedStatus() {
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1));
        when(httpClient.sendPrepare(eq(PARTICIPANT_1), any()))
                .thenReturn(Mono.just(new VoteMessage(UUID.randomUUID(), VoteResult.YES, "server-1")));
        when(httpClient.sendCommit(eq(PARTICIPANT_1), any())).thenReturn(Mono.empty());

        service.initiate("test-value").block();

        ArgumentCaptor<CoordinatorTransaction> captor = ArgumentCaptor.forClass(CoordinatorTransaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getValue()).isEqualTo("test-value");
    }

    @ParameterizedTest
    @MethodSource("noVoteScenarios")
    void initiate_givenAtLeastOneNoVote_thenAborted(List<VoteResult> voteResults) {
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1));
        UUID txId = UUID.randomUUID();
        when(httpClient.sendPrepare(eq(PARTICIPANT_1), any()))
                .thenReturn(Mono.just(new VoteMessage(txId, voteResults.get(0), "server-1")));
        when(httpClient.sendAbort(any(), any())).thenReturn(Mono.empty());

        CoordinatorTransaction result = service.initiate("value").block();

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.ABORTED);
    }

    static Stream<List<VoteResult>> noVoteScenarios() {
        return Stream.of(
                List.of(VoteResult.NO),
                List.of(VoteResult.NO));
    }

    @Test
    void initiate_givenPartialSendActive_thenSkipsExcessParticipantsDuringCommit() {
        when(partialSendFaultStrategy.isActive()).thenReturn(true);
        when(partialSendFaultStrategy.consumeSend()).thenReturn(true, false);
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1, PARTICIPANT_2));
        when(httpClient.sendPrepare(any(), any()))
                .thenReturn(Mono.just(new VoteMessage(UUID.randomUUID(), VoteResult.YES, "server-1")));
        when(httpClient.sendCommit(eq(PARTICIPANT_1), any())).thenReturn(Mono.empty());

        CoordinatorTransaction result = service.initiate("partial-send-test").block();

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMMITTED);
        verify(httpClient).sendCommit(eq(PARTICIPANT_1), any());
        verify(httpClient, never()).sendCommit(eq(PARTICIPANT_2), any());
    }

    @Test
    void initiate_givenPartialSendActive_thenSkipsExcessParticipantsDuringAbort() {
        when(partialSendFaultStrategy.isActive()).thenReturn(true);
        when(partialSendFaultStrategy.consumeSend()).thenReturn(true, false);
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1, PARTICIPANT_2));
        when(httpClient.sendPrepare(any(), any()))
                .thenReturn(Mono.just(new VoteMessage(UUID.randomUUID(), VoteResult.NO, "server-1")));
        when(httpClient.sendAbort(eq(PARTICIPANT_1), any())).thenReturn(Mono.empty());

        CoordinatorTransaction result = service.initiate("partial-abort-test").block();

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.ABORTED);
        verify(httpClient).sendAbort(eq(PARTICIPANT_1), any());
        verify(httpClient, never()).sendAbort(eq(PARTICIPANT_2), any());
    }

    @Test
    void initiate_givenAllYesVotes_publishesAllVotesCollectedEvent() {
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1, PARTICIPANT_2));
        when(httpClient.sendPrepare(eq(PARTICIPANT_1), any()))
                .thenReturn(Mono.just(new VoteMessage(UUID.randomUUID(), VoteResult.YES, "server-1")));
        when(httpClient.sendPrepare(eq(PARTICIPANT_2), any()))
                .thenReturn(Mono.just(new VoteMessage(UUID.randomUUID(), VoteResult.YES, "server-2")));
        when(httpClient.sendCommit(any(), any())).thenReturn(Mono.empty());

        service.initiate("test").block();

        ArgumentCaptor<SystemEventDto> captor = ArgumentCaptor.forClass(SystemEventDto.class);
        verify(eventPublisher, atLeastOnce()).publish(captor.capture());
        Optional<SystemEventDto> event = captor.getAllValues().stream()
                .filter(e -> e.eventType() == EventType.ALL_VOTES_COLLECTED)
                .findFirst();
        assertThat(event).isPresent();
        assertThat(event.get().payload()).containsEntry("yesCount", 2L).containsEntry("noCount", 0L);
    }

    @Test
    void initiate_givenMixedVotes_publishesAllVotesCollectedWithCorrectCounts() {
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1, PARTICIPANT_2));
        when(httpClient.sendPrepare(eq(PARTICIPANT_1), any()))
                .thenReturn(Mono.just(new VoteMessage(UUID.randomUUID(), VoteResult.YES, "server-1")));
        when(httpClient.sendPrepare(eq(PARTICIPANT_2), any()))
                .thenReturn(Mono.just(new VoteMessage(UUID.randomUUID(), VoteResult.NO, "server-2")));
        when(httpClient.sendAbort(any(), any())).thenReturn(Mono.empty());

        service.initiate("test").block();

        ArgumentCaptor<SystemEventDto> captor = ArgumentCaptor.forClass(SystemEventDto.class);
        verify(eventPublisher, atLeastOnce()).publish(captor.capture());
        Optional<SystemEventDto> event = captor.getAllValues().stream()
                .filter(e -> e.eventType() == EventType.ALL_VOTES_COLLECTED)
                .findFirst();
        assertThat(event).isPresent();
        assertThat(event.get().payload()).containsEntry("yesCount", 1L).containsEntry("noCount", 1L);
    }

    @Test
    void initiate_givenDelayedDecisionActive_thenDelaysBeforePhaseTwo() {
        when(delayedDecisionFaultStrategy.getDelayMs()).thenReturn(5_000L);
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1));
        when(httpClient.sendPrepare(eq(PARTICIPANT_1), any()))
                .thenReturn(Mono.just(new VoteMessage(UUID.randomUUID(), VoteResult.YES, "server-1")));
        when(httpClient.sendCommit(eq(PARTICIPANT_1), any())).thenReturn(Mono.empty());

        StepVerifier.withVirtualTime(() -> service.initiate("delayed-test"))
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(4_999))
                .thenAwait(Duration.ofMillis(1))
                .expectNextMatches(tx -> tx.getStatus() == TransactionStatus.COMMITTED)
                .verifyComplete();
    }

    @Test
    void initiate_givenParticipantTimeout_marksParticipantAsCrashed() {
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1));
        when(httpClient.sendPrepare(eq(PARTICIPANT_1), any()))
                .thenReturn(Mono.error(new RuntimeException("timeout")));
        when(httpClient.sendAbort(eq(PARTICIPANT_1), any())).thenReturn(Mono.empty());

        service.initiate("test-value").block();

        verify(participantRegistrationService).markCrashed("server-1");
    }

    @Test
    void initiate_givenSuccessfulPrepare_marksParticipantAsOnline() {
        when(participantRepository.findAll()).thenReturn(List.of(PARTICIPANT_1));
        when(httpClient.sendPrepare(eq(PARTICIPANT_1), any()))
                .thenReturn(Mono.just(new VoteMessage(UUID.randomUUID(), VoteResult.YES, "server-1")));
        when(httpClient.sendCommit(eq(PARTICIPANT_1), any())).thenReturn(Mono.empty());

        service.initiate("test-value").block();

        verify(participantRegistrationService).markOnline("server-1");
    }
}
