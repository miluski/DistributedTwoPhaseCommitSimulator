package com.distributed2pc.coordinator.common.api;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.distributed2pc.common.api.HttpExceptionHandlerSupport;

/**
 * Registers the shared HTTP exception handler as a controller advice in the
 * coordinator context.
 * All exception-handling logic is inherited from
 * {@link HttpExceptionHandlerSupport}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends HttpExceptionHandlerSupport {
}
