package com.distributed2pc.common.dto;

import com.distributed2pc.common.enums.FaultType;
import java.util.Map;

/**
 * Request body for injecting a fault into a node.
 *
 * @param type       The type of fault to inject.
 * @param enabled    {@code true} to activate the fault, {@code false} to clear
 *                   it.
 * @param parameters Type-specific parameters:
 *                   <ul>
 *                   <li>NETWORK_DELAY: {@code delayMs} (Integer)</li>
 *                   <li>FORCE_ABORT_VOTE: {@code forNextN} (Integer)</li>
 *                   <li>MESSAGE_LOSS: {@code probability} (Double 0.0–1.0)</li>
 *                   <li>TRANSIENT: {@code durationMs} (Integer)</li>
 *                   <li>INTERMITTENT: {@code onMs} (Integer), {@code offMs}
 *                   (Integer)</li>
 *                   </ul>
 */
public record FaultRequest(FaultType type, boolean enabled, Map<String, Object> parameters) {

  /** Convenience constructor for faults with no parameters (e.g. CRASH). */
  public FaultRequest(FaultType type, boolean enabled) {
    this(type, enabled, Map.of());
  }
}
