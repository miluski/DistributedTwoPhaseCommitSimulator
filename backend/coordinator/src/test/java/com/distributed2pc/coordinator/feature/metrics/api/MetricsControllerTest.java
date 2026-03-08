package com.distributed2pc.coordinator.feature.metrics.api;

import com.distributed2pc.common.dto.MetricsResponse;
import com.distributed2pc.coordinator.feature.metrics.application.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MetricsController}.
 */
@ExtendWith(MockitoExtension.class)
class MetricsControllerTest {

    @Mock
    MetricsService metricsService;

    MetricsController controller;

    @BeforeEach
    void setUp() {
        controller = new MetricsController(metricsService);
    }

    @Test
    void getMetrics_givenServiceReturnsResponse_returnsDelegatedResponse() {
        MetricsResponse expected = new MetricsResponse(5, 3, 1, 0, 1, 120L, true);
        when(metricsService.compute()).thenReturn(expected);

        MetricsResponse result = controller.getMetrics();

        assertThat(result).isEqualTo(expected);
    }
}
