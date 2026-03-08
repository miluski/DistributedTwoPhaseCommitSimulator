package com.distributed2pc.common.enums;

/**
 * Online/offline status of a node as reported to the coordinator or UI.
 */
public enum NodeStatus {
    /** Node is reachable and operating normally. */
    ONLINE,
    /** Node is simulating a crash; returns 503 to all requests. */
    CRASHED,
    /**
     * Node is degraded (e.g., network delay or message loss active but not
     * crashed).
     */
    DEGRADED
}
