package com.distributed2pc.coordinator.feature.fault.domain;

import java.util.List;

import com.distributed2pc.common.enums.NodeStatus;

/**
 * Immutable snapshot of the coordinator's health status at a given point in
 * time.
 *
 * @param serverId     the fixed identifier of the coordinator node.
 * @param status       the current operational status of the coordinator.
 * @param activeFaults names of all fault types currently active on the
 *                     coordinator.
 */
public record CoordinatorNodeStatus(String serverId, NodeStatus status, List<String> activeFaults) {
}
