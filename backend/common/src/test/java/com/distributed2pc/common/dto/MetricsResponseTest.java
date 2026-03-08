package com.distributed2pc.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MetricsResponse}.
 */
class MetricsResponseTest {

    private static final MetricsResponse SAMPLE = new MetricsResponse(10, 7, 2, 1, 0, 150, true);

    @Test
    void constructor_givenFields_storesThemCorrectly() {
        assertThat(SAMPLE.total()).isEqualTo(10);
        assertThat(SAMPLE.committed()).isEqualTo(7);
        assertThat(SAMPLE.aborted()).isEqualTo(2);
        assertThat(SAMPLE.uncertain()).isEqualTo(1);
        assertThat(SAMPLE.inProgress()).isZero();
        assertThat(SAMPLE.avgDecisionMs()).isEqualTo(150);
        assertThat(SAMPLE.redundancyEnabled()).isTrue();
    }

    @Test
    void equals_givenSameValues_returnsTrue() {
        MetricsResponse other = new MetricsResponse(10, 7, 2, 1, 0, 150, true);

        assertThat(other).isEqualTo(SAMPLE);
    }

    @Test
    void equals_givenDifferentTotal_returnsFalse() {
        MetricsResponse different = new MetricsResponse(99, 7, 2, 1, 0, 150, true);

        assertThat(different).isNotEqualTo(SAMPLE);
    }

    @Test
    void equals_givenDifferentRedundancyFlag_returnsFalse() {
        MetricsResponse different = new MetricsResponse(10, 7, 2, 1, 0, 150, false);

        assertThat(different).isNotEqualTo(SAMPLE);
    }

    @Test
    void hashCode_givenSameValues_returnsSameHashCode() {
        MetricsResponse other = new MetricsResponse(10, 7, 2, 1, 0, 150, true);

        assertThat(SAMPLE.hashCode()).hasSameHashCodeAs(other.hashCode());
    }

    @Test
    void toString_givenInstance_containsTotalAndAvgDecisionMs() {
        assertThat(SAMPLE.toString()).contains("10").contains("150");
    }
}
