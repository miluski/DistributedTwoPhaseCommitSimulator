package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.distributed2pc.common.enums.VoteResult;

/**
 * Unit tests for {@link VoteMessage}.
 */
class VoteMessageTest {

    @Test
    void constructor_givenFields_storesThemCorrectly() {
        UUID id = UUID.randomUUID();

        VoteMessage msg = new VoteMessage(id, VoteResult.YES, "server-1");

        assertThat(msg.transactionId()).isEqualTo(id);
        assertThat(msg.vote()).isEqualTo(VoteResult.YES);
        assertThat(msg.serverId()).isEqualTo("server-1");
    }

    @Test
    void equals_givenSameValues_returnsTrue() {
        UUID id = UUID.randomUUID();

        assertThat(new VoteMessage(id, VoteResult.YES, "s1"))
                .isEqualTo(new VoteMessage(id, VoteResult.YES, "s1"));
    }

    @Test
    void equals_givenDifferentVote_returnsFalse() {
        UUID id = UUID.randomUUID();

        assertThat(new VoteMessage(id, VoteResult.YES, "s1"))
                .isNotEqualTo(new VoteMessage(id, VoteResult.NO, "s1"));
    }

    @Test
    void equals_givenDifferentServerId_returnsFalse() {
        UUID id = UUID.randomUUID();

        assertThat(new VoteMessage(id, VoteResult.YES, "s1"))
                .isNotEqualTo(new VoteMessage(id, VoteResult.YES, "s2"));
    }

    @Test
    void hashCode_givenSameValues_returnsSameHashCode() {
        UUID id = UUID.randomUUID();

        assertThat(new VoteMessage(id, VoteResult.YES, "s1").hashCode())
                .hasSameHashCodeAs(new VoteMessage(id, VoteResult.YES, "s1").hashCode());
    }

    @Test
    void toString_givenInstance_containsServerId() {
        UUID id = UUID.randomUUID();

        assertThat(new VoteMessage(id, VoteResult.YES, "server-1").toString())
                .contains("server-1");
    }
}
