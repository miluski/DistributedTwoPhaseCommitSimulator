package com.distributed2pc.participant.feature.fault.infrastructure;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.distributed2pc.common.enums.FaultDefault;
import com.distributed2pc.participant.feature.fault.domain.ParticipantFaultStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * Fault strategy that simulates a <em>transient</em> failure: the node appears
 * crashed for a configurable duration and then automatically recovers.
 *
 * <p>
 * Calling {@link #activate(Map)} schedules automatic deactivation after
 * {@code durationMs} milliseconds (default 2000 ms). Calling
 * {@link #deactivate()} cancels any pending automatic deactivation immediately.
 */
@Slf4j
@Component
public class TransientFaultStrategy implements ParticipantFaultStrategy {

    private final AtomicBoolean active = new AtomicBoolean(false);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "transient-fault-timer");
        t.setDaemon(true);
        return t;
    });
    private final AtomicReference<ScheduledFuture<?>> pending = new AtomicReference<>();

    /**
     * {@inheritDoc}
     *
     * <p>
     * Accepts an optional {@code durationMs} parameter (Number) to override the
     * default 2-second active window.
     */
    @Override
    public void activate(Map<String, Object> parameters) {
        long duration = parseDuration(parameters);
        cancelPending();
        active.set(true);
        pending.set(scheduler.schedule(() -> {
            active.set(false);
            log.info("Transient fault auto-deactivated after {} ms", duration);
        }, duration, TimeUnit.MILLISECONDS));
        log.warn("Transient fault activated for {} ms", duration);
    }

    /** {@inheritDoc} */
    @Override
    public void deactivate() {
        cancelPending();
        active.set(false);
        log.info("Transient fault manually deactivated");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return active.get();
    }

    private long parseDuration(Map<String, Object> parameters) {
        Object raw = parameters.get("durationMs");
        if (raw instanceof Number n)
            return n.longValue();
        return FaultDefault.TRANSIENT_DURATION_MS.getValue();
    }

    private void cancelPending() {
        ScheduledFuture<?> existing = pending.getAndSet(null);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
        }
    }
}
