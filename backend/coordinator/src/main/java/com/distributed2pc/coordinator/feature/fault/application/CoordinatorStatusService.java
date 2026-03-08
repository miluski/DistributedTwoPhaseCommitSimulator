package com.distributed2pc.coordinator.feature.fault.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.common.enums.NodeStatus;
import com.distributed2pc.coordinator.feature.fault.domain.CoordinatorNodeStatus;

import lombok.RequiredArgsConstructor;

/**
 * Computes the current health status of the coordinator node.
 *
 * <p>
 * Derives {@link NodeStatus} from the set of active faults:
 * {@code CRASHED} if the crash fault is active, {@code DEGRADED} if any other
 * fault is active, and {@code ONLINE} when no faults are active.
 */
@RequiredArgsConstructor
@Service
public class CoordinatorStatusService {

    private static final String COORDINATOR_SERVER_ID = "coordinator";

    private final CoordinatorFaultService faultService;

    /**
     * Returns an immutable snapshot of the coordinator's current health.
     *
     * @return {@link CoordinatorNodeStatus} containing the server id, derived
     *         status, and list of active fault names.
     */
    public CoordinatorNodeStatus getStatus() {
        List<String> activeFaultNames = resolveActiveFaultNames();
        NodeStatus nodeStatus = deriveNodeStatus(activeFaultNames);
        return new CoordinatorNodeStatus(COORDINATOR_SERVER_ID, nodeStatus, activeFaultNames);
    }

    private List<String> resolveActiveFaultNames() {
        return faultService.listActiveFaults()
                .stream()
                .map(Enum::name)
                .toList();
    }

    private NodeStatus deriveNodeStatus(List<String> activeFaultNames) {
        if (faultService.isActive(FaultType.CRASH)) {
            return NodeStatus.CRASHED;
        }
        if (!activeFaultNames.isEmpty()) {
            return NodeStatus.DEGRADED;
        }
        return NodeStatus.ONLINE;
    }
}
