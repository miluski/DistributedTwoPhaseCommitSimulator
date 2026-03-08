package com.distributed2pc.participant.feature.fault.infrastructure;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.distributed2pc.common.enums.FaultDefault;
import com.distributed2pc.participant.feature.fault.domain.ParticipantFaultStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * Fault strategy that simulates <em>intermittent</em> failures: once activated
 * the
 * node randomly rejects requests with a configurable probability.
 *
 * <p>
 * {@link #isActive()} returns {@code true} with probability
 * {@code chancePercent / 100}
 * on each call, giving the appearance of sporadic outages. The interceptor and
 * protocol
 * handlers call {@link #isActive()} on every request, so each request is
 * independently
 * subject to failure.
 */
@Slf4j
@Component
public class IntermittentFaultStrategy implements ParticipantFaultStrategy {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AtomicBoolean armed = new AtomicBoolean(false);
    private final AtomicInteger chancePercent = new AtomicInteger(
            FaultDefault.INTERMITTENT_CHANCE_PERCENT.getIntValue());

    /**
     * {@inheritDoc}
     *
     * <p>
     * Accepts an optional {@code chancePercent} parameter (Number, 0–100)
     * controlling
     * the per-request failure probability.
     */
    @Override
    public void activate(Map<String, Object> parameters) {
        Object raw = parameters.get("chancePercent");
        if (raw instanceof Number n) {
            chancePercent.set(n.intValue());
        }
        armed.set(true);
        log.warn("Intermittent fault armed at {}% chance", chancePercent.get());
    }

    /** {@inheritDoc} */
    @Override
    public void deactivate() {
        armed.set(false);
        chancePercent.set(FaultDefault.INTERMITTENT_CHANCE_PERCENT.getIntValue());
        log.info("Intermittent fault disarmed");
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Returns {@code true} with probability {@code chancePercent / 100} when the
     * fault
     * is armed; always {@code false} when disarmed.
     */
    @Override
    public boolean isActive() {
        if (!armed.get())
            return false;
        return SECURE_RANDOM.nextInt(100) < chancePercent.get();
    }
}
