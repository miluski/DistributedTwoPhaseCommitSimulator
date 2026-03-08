package com.distributed2pc.common.api;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Shared HTTP exception-handling logic for all service modules.
 *
 * <p>
 * Translates common client errors (4xx) into a uniform JSON structure
 * {@code {"error": "...", "message": "..."}}. Concrete subclasses activate this
 * logic by declaring {@code @RestControllerAdvice} in their own application
 * context.
 */
@Slf4j
public abstract class HttpExceptionHandlerSupport {

    /**
     * Handles missing or malformed JSON request bodies.
     *
     * @param ex the parsing exception.
     * @return 400 Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return Map.of(ResponseBodyKey.ERROR.getValue(), ResponseBodyKey.BAD_REQUEST.getValue(),
                ResponseBodyKey.MESSAGE.getValue(), "Malformed or missing request body");
    }

    /**
     * Handles path-variable or query-parameter type conversion failures.
     *
     * @param ex the type mismatch exception.
     * @return 400 Bad Request identifying the offending parameter.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());
        return Map.of(ResponseBodyKey.ERROR.getValue(), ResponseBodyKey.BAD_REQUEST.getValue(),
                ResponseBodyKey.MESSAGE.getValue(), "Invalid value for parameter '" + ex.getName() + "'");
    }

    /**
     * Handles {@code @Valid} / {@code @Validated} bean validation failures on
     * request bodies, listing every failing field and its constraint message.
     *
     * @param ex the validation exception.
     * @return 400 Bad Request with a semicolon-separated list of field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", details);
        return Map.of(ResponseBodyKey.ERROR.getValue(), ResponseBodyKey.BAD_REQUEST.getValue(),
                ResponseBodyKey.MESSAGE.getValue(), details);
    }

    /**
     * Handles missing required request parameters.
     *
     * @param ex the missing-parameter exception.
     * @return 400 Bad Request naming the missing parameter.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        return Map.of(ResponseBodyKey.ERROR.getValue(), ResponseBodyKey.BAD_REQUEST.getValue(),
                ResponseBodyKey.MESSAGE.getValue(), "Required parameter '" + ex.getParameterName() + "' is missing");
    }

    /**
     * Handles JSR-303 / Jakarta Bean Validation constraint violations raised at
     * the method level (e.g. on {@code @PathVariable} or service methods).
     *
     * @param ex the constraint violation exception.
     * @return 400 Bad Request with a semicolon-separated list of violations.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleConstraintViolation(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("Constraint violation: {}", details);
        return Map.of(ResponseBodyKey.ERROR.getValue(), ResponseBodyKey.BAD_REQUEST.getValue(),
                ResponseBodyKey.MESSAGE.getValue(), details);
    }

    /**
     * Handles requests that use an HTTP method not supported by the endpoint.
     *
     * @param ex the method-not-supported exception.
     * @return 405 Method Not Allowed.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Map<String, String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMethod());
        return Map.of(ResponseBodyKey.ERROR.getValue(), "Method Not Allowed",
                ResponseBodyKey.MESSAGE.getValue(), "HTTP method '" + ex.getMethod() + "' is not supported");
    }

    /**
     * Handles requests whose {@code Content-Type} is not supported by the endpoint.
     *
     * @param ex the unsupported media type exception.
     * @return 415 Unsupported Media Type.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Map<String, String> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("Unsupported content type: {}", ex.getContentType());
        return Map.of(ResponseBodyKey.ERROR.getValue(), "Unsupported Media Type",
                ResponseBodyKey.MESSAGE.getValue(), "Content type '" + ex.getContentType() + "' is not supported");
    }

    /**
     * Handles requests whose {@code Accept} header cannot be satisfied.
     *
     * @param ex the not-acceptable exception.
     * @return 406 Not Acceptable.
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public Map<String, String> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        log.warn("Not acceptable: {}", ex.getMessage());
        return Map.of(ResponseBodyKey.ERROR.getValue(), "Not Acceptable",
                ResponseBodyKey.MESSAGE.getValue(), "None of the requested media types are supported");
    }
}
