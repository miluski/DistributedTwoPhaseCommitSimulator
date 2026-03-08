package com.distributed2pc.common.enums;

/**
 * Logical roles that identify nodes participating in the 2PC protocol.
 *
 * <p>
 * Constants of this enum are used as {@code source} and {@code target}
 * identifiers when constructing
 * {@link com.distributed2pc.common.dto.SystemEventDto}
 * payloads so the React UI can label events correctly.
 */
public enum NodeRole {

    /**
     * Identifies the coordinator node in event payloads.
     */
    COORDINATOR("coordinator");

    private final String value;

    NodeRole(String value) {
        this.value = value;
    }

    /**
     * Returns the string identifier for this node role.
     *
     * @return the role string used in event payloads.
     */
    public String getValue() {
        return value;
    }
}
