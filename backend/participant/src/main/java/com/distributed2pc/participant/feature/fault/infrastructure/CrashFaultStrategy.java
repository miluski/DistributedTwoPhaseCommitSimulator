package com.distributed2pc.participant.feature.fault.infrastructure;

import com.distributed2pc.participant.feature.fault.domain.ParticipantFaultStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CRASH fault strategy: when active, the node returns HTTP 503 to all incoming requests.
 *
 * <p>The crash flag is checked by a {@code HandlerInterceptor} that runs before every
 * controller method. Clearing this fault simulates node recovery.
 */
@Component
public class CrashFaultStrategy implements ParticipantFaultStrategy {

    private final AtomicBoolean crashed = new AtomicBoolean(false);

    /** {@inheritDoc} */
    @Override
    public void activate(Map<String, Object> parameters) {
        crashed.set(true);
    }

    /** {@inheritDoc} */
    @Override
    public void deactivate() {
        crashed.set(false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return crashed.get();
    }
}
