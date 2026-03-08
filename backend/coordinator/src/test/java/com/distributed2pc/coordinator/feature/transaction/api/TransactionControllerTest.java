package com.distributed2pc.coordinator.feature.transaction.api;

import com.distributed2pc.common.dto.TransactionRequest;
import com.distributed2pc.common.dto.TransactionResponse;
import com.distributed2pc.common.enums.TransactionStatus;
import com.distributed2pc.coordinator.feature.transaction.application.TwoPhaseCommitService;
import com.distributed2pc.coordinator.feature.transaction.domain.CoordinatorTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TransactionController}.
 */
@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    TwoPhaseCommitService twoPhaseCommitService;

    @Mock
    TransactionResponseMapper mapper;

    TransactionController controller;

    private static final UUID TX_ID = UUID.randomUUID();
    private static final TransactionResponse COMMITTED_RESPONSE = new TransactionResponse(
            TX_ID, TransactionStatus.COMMITTED, "value", Instant.now(), Instant.now(), Map.of());

    @BeforeEach
    void setUp() {
        controller = new TransactionController(twoPhaseCommitService, mapper);
    }

    @Test
    void initiate_givenValidRequest_returnsCommittedResponse() {
        CoordinatorTransaction tx = new CoordinatorTransaction("value");
        when(twoPhaseCommitService.initiate("value")).thenReturn(Mono.just(tx));
        when(mapper.toResponse(tx)).thenReturn(COMMITTED_RESPONSE);

        Mono<TransactionResponse> result = controller.initiate(new TransactionRequest("value"));

        StepVerifier.create(result)
                .expectNext(COMMITTED_RESPONSE)
                .verifyComplete();
    }

    @Test
    void listAll_givenExistingTransactions_returnsMappedResponses() {
        CoordinatorTransaction tx = new CoordinatorTransaction("v");
        when(twoPhaseCommitService.findAll()).thenReturn(List.of(tx));
        when(mapper.toResponse(tx)).thenReturn(COMMITTED_RESPONSE);

        List<TransactionResponse> result = controller.listAll();

        assertThat(result).containsExactly(COMMITTED_RESPONSE);
    }

    @Test
    void listAll_givenNoTransactions_returnsEmptyList() {
        when(twoPhaseCommitService.findAll()).thenReturn(List.of());

        List<TransactionResponse> result = controller.listAll();

        assertThat(result).isEmpty();
    }

    @Test
    void getById_givenExistingId_returnsMappedResponse() {
        CoordinatorTransaction tx = new CoordinatorTransaction("v");
        when(twoPhaseCommitService.findById(TX_ID)).thenReturn(Optional.of(tx));
        when(mapper.toResponse(tx)).thenReturn(COMMITTED_RESPONSE);

        TransactionResponse result = controller.getById(TX_ID);

        assertThat(result).isEqualTo(COMMITTED_RESPONSE);
    }

    @Test
    void getById_givenUnknownId_throwsResponseStatusException() {
        when(twoPhaseCommitService.findById(TX_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getById(TX_ID))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }
}
