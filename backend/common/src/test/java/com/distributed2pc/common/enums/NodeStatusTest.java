package com.distributed2pc.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link NodeStatus}.
 */
class NodeStatusTest {

    @Test
    void values_containsThreeConstants() {
        assertThat(NodeStatus.values()).hasSize(3);
    }

    @Test
    void values_containsAllExpectedConstants() {
        assertThat(NodeStatus.values()).contains(
                NodeStatus.ONLINE,
                NodeStatus.CRASHED,
                NodeStatus.DEGRADED);
    }

    @ParameterizedTest
    @MethodSource("provideAllConstantNames")
    void valueOf_givenValidName_returnsConstant(String name) {
        assertThat(NodeStatus.valueOf(name)).isNotNull();
    }

    static Stream<String> provideAllConstantNames() {
        return Stream.of(NodeStatus.values()).map(Enum::name);
    }
}
