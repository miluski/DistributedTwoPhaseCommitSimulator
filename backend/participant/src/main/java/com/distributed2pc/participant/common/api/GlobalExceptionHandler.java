package com.distributed2pc.participant.common.api;

import com.distributed2pc.common.api.HttpExceptionHandlerSupport;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Registers the shared HTTP exception handler as a controller advice in the participant context.
 * All exception-handling logic is inherited from {@link HttpExceptionHandlerSupport}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends HttpExceptionHandlerSupport {}
