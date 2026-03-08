package com.distributed2pc.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link NodeRole}.
 */
class NodeRoleTest {

    @Test
    void values_containsOneConstant() {
        assertThat(NodeRole.values()).hasSize(1);
    }

    @Test
    void coordinator_givenGetValue_returnsCoordinatorString() {
        assertThat(NodeRole.COORDINATOR.getValue()).isEqualTo("coordinator");
    }

    @ParameterizedTest
    @MethodSource("provideAllConstantNames")
    void valueOf_givenValidName_returnsConstant(String name) {
        assertThat(NodeRole.valueOf(name)).isNotNull();
    }

    static Stream<String> provideAllConstantNames() {
        return Stream.of(NodeRole.values()).map(Enum::name);
    }
}
