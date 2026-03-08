package com.distributed2pc.common.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Keys and standard label values used in JSON error-response bodies returned
 * by HTTP exception handlers.
 *
 * <p>
 * All HTTP error responses from this application follow the structure:
 *
 * <pre>{@code {"error": "<label>", "message": "<description>"}}</pre>
 */
@Getter
@RequiredArgsConstructor
public enum ResponseBodyKey {

    /**
     * JSON key for the short error-category label, e.g. {@code "Bad Request"}.
     */
    ERROR("error"),

    /**
     * JSON key for the human-readable detail message.
     */
    MESSAGE("message"),

    /**
     * Standard label for HTTP 400 Bad Request responses.
     */
    BAD_REQUEST("Bad Request");

    private final String value;
}
