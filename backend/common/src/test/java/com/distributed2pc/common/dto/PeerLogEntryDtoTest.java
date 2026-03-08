package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.distributed2pc.common.enums.TransactionStatus;

/**
 * Unit tests for {@link PeerLogEntryDto}.
 */
class PeerLogEntryDtoTest {

    @Test
    void constructor_givenFields_storesThemCorrectly() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        PeerLogEntryDto dto = new PeerLogEntryDto(id, TransactionStatus.COMMITTED, "server-1", now);

        assertThat(dto.transactionId()).isEqualTo(id);
        assertThat(dto.phase()).isEqualTo(TransactionStatus.COMMITTED);
        assertThat(dto.serverId()).isEqualTo("server-1");
        assertThat(dto.timestamp()).isEqualTo(now);
    }

    @Test
    void equals_givenSameValues_returnsTrue() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        assertThat(new PeerLogEntryDto(id, TransactionStatus.COMMITTED, "s1", now))
                .isEqualTo(new PeerLogEntryDto(id, TransactionStatus.COMMITTED, "s1", now));
    }

    @Test
    void equals_givenDifferentPhase_returnsFalse() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        assertThat(new PeerLogEntryDto(id, TransactionStatus.COMMITTED, "s1", now))
                .isNotEqualTo(new PeerLogEntryDto(id, TransactionStatus.ABORTED, "s1", now));
    }

    @Test
    void equals_givenDifferentServerId_returnsFalse() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        assertThat(new PeerLogEntryDto(id, TransactionStatus.COMMITTED, "s1", now))
                .isNotEqualTo(new PeerLogEntryDto(id, TransactionStatus.COMMITTED, "s2", now));
    }

    @Test
    void hashCode_givenSameValues_returnsSameHashCode() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        assertThat(new PeerLogEntryDto(id, TransactionStatus.COMMITTED, "s1", now).hashCode())
                .hasSameHashCodeAs(new PeerLogEntryDto(id, TransactionStatus.COMMITTED, "s1", now).hashCode());
    }
}
