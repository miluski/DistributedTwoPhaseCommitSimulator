package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.distributed2pc.common.enums.FaultType;

/**
 * Unit tests for {@link FaultRequest}.
 */
class FaultRequestTest {

    @Test
    void constructor_givenAllFields_storesThemCorrectly() {
        Map<String, Object> params = Map.of("delayMs", 200);

        FaultRequest request = new FaultRequest(FaultType.NETWORK_DELAY, true, params);

        assertThat(request.type()).isEqualTo(FaultType.NETWORK_DELAY);
        assertThat(request.enabled()).isTrue();
        assertThat(request.parameters()).containsEntry("delayMs", 200);
    }

    @Test
    void convenienceConstructor_givenTypeAndEnabled_usesEmptyParameters() {
        FaultRequest request = new FaultRequest(FaultType.CRASH, true);

        assertThat(request.type()).isEqualTo(FaultType.CRASH);
        assertThat(request.enabled()).isTrue();
        assertThat(request.parameters()).isEmpty();
    }

    @Test
    void convenienceConstructor_givenFalseEnabled_storesFalse() {
        FaultRequest request = new FaultRequest(FaultType.CRASH, false);

        assertThat(request.enabled()).isFalse();
    }

    @Test
    void equals_givenSameValues_returnsTrue() {
        assertThat(new FaultRequest(FaultType.CRASH, false))
                .isEqualTo(new FaultRequest(FaultType.CRASH, false));
    }

    @Test
    void equals_givenDifferentType_returnsFalse() {
        assertThat(new FaultRequest(FaultType.CRASH, true))
                .isNotEqualTo(new FaultRequest(FaultType.NETWORK_DELAY, true));
    }

    @Test
    void hashCode_givenSameValues_returnsSameHashCode() {
        assertThat(new FaultRequest(FaultType.CRASH, true).hashCode())
                .hasSameHashCodeAs(new FaultRequest(FaultType.CRASH, true).hashCode());
    }
}
