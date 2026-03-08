package com.distributed2pc.common.enums;

/**
 * Types of faults that can be injected into any node (coordinator or
 * participant).
 */
public enum FaultType {
    /**
     * Node returns HTTP 503 to all requests, simulating a full crash.
     * The node can be "restarted" by clearing this fault via the REST API.
     */
    CRASH,

    /**
     * Adds artificial latency (configurable milliseconds) to all outbound
     * responses,
     * simulating a slow or congested network link.
     */
    NETWORK_DELAY,

    /**
     * Forces the participant to vote NO on the next N prepare requests, regardless
     * of
     * its actual state. Only applicable to participants.
     */
    FORCE_ABORT_VOTE,

    /**
     * Drops outbound messages (HTTP responses) with a configurable probability
     * (0.0–1.0),
     * simulating an unreliable network.
     */
    MESSAGE_LOSS,

    /**
     * Fault is active for a fixed duration (configurable milliseconds) and then
     * automatically clears. Models a transient hardware or software glitch.
     */
    TRANSIENT,

    /**
     * Fault alternates between active and inactive at a configurable period (onMs /
     * offMs),
     * modelling intermittent connectivity issues.
     */
    INTERMITTENT
}
