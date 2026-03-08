package com.distributed2pc.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link VoteResult}.
 */
class VoteResultTest {

    @Test
    void values_containsTwoConstants() {
        assertThat(VoteResult.values()).hasSize(2);
    }

    @Test
    void values_containsBothExpectedConstants() {
        assertThat(VoteResult.values()).contains(VoteResult.YES, VoteResult.NO);
    }

    @ParameterizedTest
    @MethodSource("provideAllConstantNames")
    void valueOf_givenValidName_returnsConstant(String name) {
        assertThat(VoteResult.valueOf(name)).isNotNull();
    }

    static Stream<String> provideAllConstantNames() {
        return Stream.of(VoteResult.values()).map(Enum::name);
    }
}
