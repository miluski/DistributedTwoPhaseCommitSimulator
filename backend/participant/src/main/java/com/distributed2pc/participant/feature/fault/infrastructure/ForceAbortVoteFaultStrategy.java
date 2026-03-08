package com.distributed2pc.participant.feature.fault.infrastructure;

import com.distributed2pc.participant.feature.fault.domain.ParticipantFaultStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FORCE_ABORT_VOTE fault strategy: forces the participant to vote NO on the next N prepare requests.
 *
 * <p>The counter is decremented on each prepare call. When it reaches zero the fault
 * auto-deactivates. For unlimited NO votes, set {@code forNextN} to {@link Integer#MAX_VALUE}.
 */
@Component
public class ForceAbortVoteFaultStrategy implements ParticipantFaultStrategy {

    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicInteger remainingNoVotes = new AtomicInteger(0);

    /** {@inheritDoc} */
    @Override
    public void activate(Map<String, Object> parameters) {
        Object param = parameters.get("forNextN");
        int count = (param instanceof Number number) ? number.intValue() : 1;
        remainingNoVotes.set(count);
        active.set(true);
    }

    /** {@inheritDoc} */
    @Override
    public void deactivate() {
        active.set(false);
        remainingNoVotes.set(0);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return active.get();
    }

    /**
     * Consumes one forced NO vote. Deactivates the fault when the quota is exhausted.
     *
     * @return {@code true} if this call should produce a forced NO vote.
     */
    public boolean consumeNoVote() {
        if (!active.get()) {
            return false;
        }
        int remaining = remainingNoVotes.decrementAndGet();
        if (remaining <= 0) {
            active.set(false);
        }
        return true;
    }
}
