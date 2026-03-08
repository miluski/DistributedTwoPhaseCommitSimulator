package com.distributed2pc.common.dto;

/**
 * Request payload for enabling or disabling the redundancy / recovery protocol
 * on any node (coordinator or participant).
 *
 * @param redundancyEnabled {@code true} to enable the election-recovery
 *                          protocol;
 *                          {@code false} to disable it (simulates &quot;no
 *                          redundancy&quot; mode).
 */
public record SimulationConfigRequest(boolean redundancyEnabled) {
}
