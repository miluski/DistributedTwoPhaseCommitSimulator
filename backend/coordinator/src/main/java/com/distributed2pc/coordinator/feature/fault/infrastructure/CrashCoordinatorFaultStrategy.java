package com.distributed2pc.coordinator.feature.fault.infrastructure;

import com.distributed2pc.coordinator.feature.fault.domain.CoordinatorFaultStrategy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link CoordinatorFaultStrategy} that simulates a coordinator crash by setting
 * an atomic flag. The {@link CrashCoordinatorInterceptor} checks this flag on
 * every incoming request and returns HTTP 503 when active.
 */
@Slf4j
public class CrashCoordinatorFaultStrategy implements CoordinatorFaultStrategy {

 private final AtomicBoolean crashed = new AtomicBoolean(false);

 /** {@inheritDoc} */
 @Override
 public void activate(Map<String, Object> parameters) {
  crashed.set(true);
  log.warn("Coordinator crash simulated — all requests will return 503");
 }

 /** {@inheritDoc} */
 @Override
 public void deactivate() {
  crashed.set(false);
  log.info("Coordinator crash cleared — resuming normal operation");
 }

 /** {@inheritDoc} */
 @Override
 public boolean isActive() {
  return crashed.get();
 }
}
