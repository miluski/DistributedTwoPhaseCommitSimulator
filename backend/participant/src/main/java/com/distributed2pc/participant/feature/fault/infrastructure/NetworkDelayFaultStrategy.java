package com.distributed2pc.participant.feature.fault.infrastructure;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.distributed2pc.common.enums.FaultDefault;
import com.distributed2pc.participant.feature.fault.domain.ParticipantFaultStrategy;

/**
 * NETWORK_DELAY fault strategy: adds configurable latency to every outbound
 * response.
 *
 * <p>
 * The delay is applied by calling {@code Thread.sleep} inside a servlet filter
 * that inspects {@link #getDelayMs()} before writing the response. This
 * deliberately
 * slows all HTTP responses from this participant to simulate network
 * congestion.
 */
@Component
public class NetworkDelayFaultStrategy implements ParticipantFaultStrategy {

    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicLong delayMs = new AtomicLong(FaultDefault.NETWORK_DELAY_MS.getValue());

    /** {@inheritDoc} */
    @Override
    public void activate(Map<String, Object> parameters) {
        Object param = parameters.get("delayMs");
        if (param instanceof Number number) {
            delayMs.set(number.longValue());
        }
        active.set(true);
    }

    /** {@inheritDoc} */
    @Override
    public void deactivate() {
        active.set(false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return active.get();
    }

    /**
     * Returns the configured delay in milliseconds.
     *
     * @return delay in ms.
     */
    public long getDelayMs() {
        return delayMs.get();
    }
}
