package com.distributed2pc.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link ResponseBodyKey}.
 */
class ResponseBodyKeyTest {

    @Test
    void values_containsThreeConstants() {
        assertThat(ResponseBodyKey.values()).hasSize(3);
    }

    @Test
    void error_givenGetValue_returnsErrorString() {
        assertThat(ResponseBodyKey.ERROR.getValue()).isEqualTo("error");
    }

    @Test
    void message_givenGetValue_returnsMessageString() {
        assertThat(ResponseBodyKey.MESSAGE.getValue()).isEqualTo("message");
    }

    @Test
    void badRequest_givenGetValue_returnsBadRequestLabel() {
        assertThat(ResponseBodyKey.BAD_REQUEST.getValue()).isEqualTo("Bad Request");
    }

    @ParameterizedTest
    @MethodSource("provideAllConstantNames")
    void valueOf_givenValidName_returnsConstant(String name) {
        assertThat(ResponseBodyKey.valueOf(name)).isNotNull();
    }

    static Stream<String> provideAllConstantNames() {
        return Stream.of(ResponseBodyKey.values()).map(Enum::name);
    }
}
