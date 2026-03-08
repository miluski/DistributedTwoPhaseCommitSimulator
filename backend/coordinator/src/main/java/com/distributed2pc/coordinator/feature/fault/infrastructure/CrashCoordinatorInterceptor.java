package com.distributed2pc.coordinator.feature.fault.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that returns HTTP 503 on every request when the
 * coordinator crash simulation is active.
 */
public class CrashCoordinatorInterceptor implements HandlerInterceptor {

 private final CrashCoordinatorFaultStrategy crashStrategy;

 /**
  * @param crashStrategy the crash-fault strategy whose flag this interceptor checks.
  */
 public CrashCoordinatorInterceptor(CrashCoordinatorFaultStrategy crashStrategy) {
  this.crashStrategy = crashStrategy;
 }

 /**
  * Rejects the request with 503 when the crash simulation is active.
  *
  * @param request  the current HTTP request.
  * @param response the current HTTP response.
  * @param handler  the chosen handler (unused).
  * @return {@code false} when crashed (request is blocked), {@code true} otherwise.
  * @throws Exception if writing the error response fails.
  */
 @Override
 public boolean preHandle(HttpServletRequest request,
        HttpServletResponse response,
        Object handler) throws Exception {
  if (crashStrategy.isActive()) {
   response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Coordinator crashed");
   return false;
  }
  return true;
 }
}
