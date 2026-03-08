package com.distributed2pc.coordinator.feature.metrics.api;

import com.distributed2pc.common.dto.MetricsResponse;
import com.distributed2pc.coordinator.feature.metrics.application.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint that exposes aggregate transaction metrics to the UI.
 *
 * <p>
 * The UI polls this endpoint periodically to refresh the metrics bar showing
 * committed / aborted / uncertain counts and average decision latency.
 */
@Tag(name = "Metrics", description = "Aggregate transaction outcome metrics")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

 private final MetricsService metricsService;

 /**
  * Returns the current aggregate transaction outcome metrics.
  *
  * @return a {@link MetricsResponse} containing counts and timing data.
  */
 @Operation(summary = "Returns aggregate transaction outcome metrics")
 @ApiResponse(responseCode = "200", description = "Transaction metrics")
 @GetMapping
 public MetricsResponse getMetrics() {
  return metricsService.compute();
 }
}
