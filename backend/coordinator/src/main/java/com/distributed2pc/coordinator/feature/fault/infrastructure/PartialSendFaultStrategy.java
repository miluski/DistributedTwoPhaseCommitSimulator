package com.distributed2pc.coordinator.feature.fault.infrastructure;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.distributed2pc.common.enums.FaultDefault;
import com.distributed2pc.coordinator.feature.fault.domain.CoordinatorFaultStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link CoordinatorFaultStrategy} that simulates partial Phase 2 delivery:
 * the coordinator sends commit/abort to only the first {@code count}
 * participants
 * (default 1), leaving the rest uncertain.
 *
 * <p>
 * The {@code count} parameter can be supplied via
 * {@code parameters.get("count")}.
 */
@Slf4j
public class PartialSendFaultStrategy implements CoordinatorFaultStrategy {

    private final AtomicInteger remainingSends = new AtomicInteger(0);

    /** {@inheritDoc} */
    @Override
    public void activate(Map<String, Object> parameters) {
        int count = extractCount(parameters);
        remainingSends.set(count);
        log.warn("Partial-send fault activated: will send decision to {} participant(s) only", count);
    }

    /** {@inheritDoc} */
    @Override
    public void deactivate() {
        remainingSends.set(0);
        log.info("Partial-send fault cleared");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return remainingSends.get() > 0;
    }

    /**
     * Consumes one send slot and returns whether this send should proceed.
     *
     * @return {@code true} if the send is within the allowed partial count.
     */
    public boolean consumeSend() {
        return remainingSends.getAndDecrement() > 0;
    }

    private int extractCount(Map<String, Object> parameters) {
        Object raw = parameters.get("count");
        if (raw instanceof Number n)
            return n.intValue();
        return FaultDefault.PARTIAL_SEND_COUNT.getIntValue();
    }
}
