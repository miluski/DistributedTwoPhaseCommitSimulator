package com.distributed2pc.coordinator.feature.fault.infrastructure;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.distributed2pc.common.enums.FaultDefault;
import com.distributed2pc.coordinator.feature.fault.domain.CoordinatorFaultStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link CoordinatorFaultStrategy} that inserts an artificial delay before the
 * coordinator broadcasts its Phase 2 decision, simulating a slow or stalled
 * coordinator.
 *
 * <p>
 * The delay in milliseconds is read from {@code parameters.get("delayMs")};
 * defaults to 3 000 ms when not supplied.
 */
@Slf4j
public class DelayedDecisionFaultStrategy implements CoordinatorFaultStrategy {

    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicLong delayMs = new AtomicLong(FaultDefault.DELAYED_DECISION_MS.getValue());

    /** {@inheritDoc} */
    @Override
    public void activate(Map<String, Object> parameters) {
        long delay = extractDelay(parameters);
        delayMs.set(delay);
        active.set(true);
        log.warn("Delayed-decision fault activated: {}ms delay before Phase-2 dispatch", delay);
    }

    /** {@inheritDoc} */
    @Override
    public void deactivate() {
        active.set(false);
        log.info("Delayed-decision fault cleared");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return active.get();
    }

    /**
     * Returns the configured delay in milliseconds.
     *
     * @return delay in ms, or 0 if the fault is not active.
     */
    public long getDelayMs() {
        return active.get() ? delayMs.get() : 0L;
    }

    private long extractDelay(Map<String, Object> parameters) {
        Object raw = parameters.get("delayMs");
        if (raw instanceof Number n)
            return n.longValue();
        return FaultDefault.DELAYED_DECISION_MS.getValue();
    }
}
