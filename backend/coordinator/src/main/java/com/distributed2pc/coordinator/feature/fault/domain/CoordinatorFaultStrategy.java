package com.distributed2pc.coordinator.feature.fault.domain;

import com.distributed2pc.common.enums.FaultType;
import java.util.Map;

/**
 * Strategy contract for applying or clearing a specific type of fault on the coordinator.
 *
 * <p>Each {@link FaultType} has exactly one implementation of this interface,
 * registered in a {@code Map<FaultType, CoordinatorFaultStrategy>} configuration bean.
 * The application service looks up the strategy by fault type — no if-else chains.
 */
public interface CoordinatorFaultStrategy {

 /**
  * Activates this fault with the provided parameters.
  *
  * @param parameters type-specific configuration values (may be empty).
  */
 void activate(Map<String, Object> parameters);

 /**
  * Deactivates this fault, returning the coordinator to normal operation.
  */
 void deactivate();

 /**
  * Returns whether this fault is currently active.
  *
  * @return {@code true} if the fault is active.
  */
 boolean isActive();
}
