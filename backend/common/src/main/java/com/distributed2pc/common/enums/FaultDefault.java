package com.distributed2pc.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Default parameter values for all fault-injection strategies.
 *
 * <p>
 * Each constant encapsulates the out-of-the-box behaviour that a strategy
 * falls back to when the caller does not supply an explicit configuration
 * parameter.
 */
@Getter
@RequiredArgsConstructor
public enum FaultDefault {

    /**
     * Default artificial delay applied by the NETWORK_DELAY strategy (1 000 ms).
     */
    NETWORK_DELAY_MS(1_000L),

    /**
     * Default delay before the Phase-2 broadcast in the DELAYED_DECISION strategy
     * (3 000 ms).
     */
    DELAYED_DECISION_MS(3_000L),

    /**
     * Default number of participants that receive the Phase-2 decision in the
     * PARTIAL_SEND strategy (1).
     */
    PARTIAL_SEND_COUNT(1L),

    /**
     * Default per-request failure probability for the INTERMITTENT strategy
     * (50 %).
     */
    INTERMITTENT_CHANCE_PERCENT(50L),

    /**
     * Default active window for the TRANSIENT strategy (2 000 ms).
     */
    TRANSIENT_DURATION_MS(2_000L);

    private final long value;

    /**
     * Returns the default value as an {@code int}, for use with APIs that require
     * an integer (e.g. {@link java.util.concurrent.atomic.AtomicInteger}).
     *
     * @return the default value cast to {@code int}.
     */
    public int getIntValue() {
        return (int) value;
    }
}
