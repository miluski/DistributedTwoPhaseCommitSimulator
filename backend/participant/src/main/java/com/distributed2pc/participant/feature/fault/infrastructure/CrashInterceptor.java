package com.distributed2pc.participant.feature.fault.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Servlet interceptor that returns HTTP 503 for every request when the CRASH fault is active.
 *
 * <p>This interceptor is registered in {@code WebMvcConfig} and runs before all controllers.
 * Simulating a crash this way means any caller—coordinator or peer—receives an immediate
 * 503, which the coordinator treats as a negative vote or communication failure.
 */
@RequiredArgsConstructor
@Component
public class CrashInterceptor implements HandlerInterceptor {

    private final CrashFaultStrategy crashFaultStrategy;

    /**
     * Blocks the request with 503 if this participant is currently simulating a crash.
     *
     * @param request  the incoming HTTP request.
     * @param response the HTTP response.
     * @param handler  the target handler (unused).
     * @return {@code false} to abort the request chain when crashed; {@code true} otherwise.
     * @throws Exception if response writing fails.
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        if (crashFaultStrategy.isActive()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Participant is crashed");
            return false;
        }
        return true;
    }
}
