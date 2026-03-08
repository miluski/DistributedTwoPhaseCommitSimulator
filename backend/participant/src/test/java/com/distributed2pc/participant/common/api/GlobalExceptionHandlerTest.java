package com.distributed2pc.participant.common.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /**
     * Malformed body maps to a stable 400 message that does not echo input.
     */
    @Test
    void handleUnreadableMessage_givenParsingException_returnsBadRequest() {
        Map<String, String> response = handler.handleUnreadableMessage(mock(HttpMessageNotReadableException.class));

        assertThat(response)
                .containsEntry("error", "Bad Request")
                .containsEntry("message", "Malformed or missing request body");
    }

    /**
     * Path-variable type mismatch identifies the offending parameter.
     */
    @Test
    void handleTypeMismatch_givenMismatchOnTxId_returnsBadRequestWithParamName() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("txId");

        Map<String, String> response = handler.handleTypeMismatch(ex);

        assertThat(response).containsEntry("message", "Invalid value for parameter 'txId'");
    }

    /**
     * Bean-validation failure lists the constraint message of each field error.
     */
    @Test
    void handleValidation_givenFieldError_returnsBadRequestWithDetails() {
        FieldError fieldError = new FieldError("req", "value", "must not be blank");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        Map<String, String> response = handler.handleValidation(ex);

        assertThat(response)
                .containsEntry("error", "Bad Request")
                .containsEntry("message", "must not be blank");
    }

    /**
     * Multiple field errors are joined with a semicolon separator.
     */
    @Test
    void handleValidation_givenMultipleFieldErrors_joinsMessages() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("req", "a", "msg1"),
                new FieldError("req", "b", "msg2")));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        Map<String, String> response = handler.handleValidation(ex);

        assertThat(response.get("message")).contains("msg1").contains("msg2");
    }

    /**
     * Missing required query parameter response includes the parameter name.
     */
    @Test
    void handleMissingParam_givenMissingValue_returnsBadRequestWithParamName() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("value", "String");

        Map<String, String> response = handler.handleMissingParam(ex);

        assertThat(response)
                .containsEntry("error", "Bad Request")
                .containsEntry("message", "Required parameter 'value' is missing");
    }

    /**
     * Constraint violations are collected with property path and message.
     */
    @Test
    @SuppressWarnings("unchecked")
    void handleConstraintViolation_givenViolation_returnsBadRequestWithDetails() {
        ConstraintViolation<Object> cv = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("value");
        when(cv.getPropertyPath()).thenReturn(path);
        when(cv.getMessage()).thenReturn("must not be null");
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(cv);
        ConstraintViolationException ex = new ConstraintViolationException("cv", violations);

        Map<String, String> response = handler.handleConstraintViolation(ex);

        assertThat(response)
                .containsEntry("error", "Bad Request")
                .containsEntry("message", "value: must not be null");
    }

    /**
     * Wrong HTTP method maps to 405 and includes the rejected method name.
     */
    @Test
    void handleMethodNotSupported_givenDeleteOnReadOnly_returnsMethodNotAllowed() {
        HttpRequestMethodNotSupportedException ex = mock(HttpRequestMethodNotSupportedException.class);
        when(ex.getMethod()).thenReturn("DELETE");

        Map<String, String> response = handler.handleMethodNotSupported(ex);

        assertThat(response)
                .containsEntry("error", "Method Not Allowed")
                .containsEntry("message", "HTTP method 'DELETE' is not supported");
    }

    /**
     * Unsupported Content-Type maps to 415 and includes the rejected type.
     */
    @Test
    void handleMediaTypeNotSupported_givenTextPlain_returnsUnsupportedMediaType() {
        HttpMediaTypeNotSupportedException ex = mock(HttpMediaTypeNotSupportedException.class);
        when(ex.getContentType()).thenReturn(MediaType.TEXT_PLAIN);

        Map<String, String> response = handler.handleMediaTypeNotSupported(ex);

        assertThat(response)
                .containsEntry("error", "Unsupported Media Type")
                .containsEntry("message",
                        "Content type '" + MediaType.TEXT_PLAIN + "' is not supported");
    }

    /**
     * Unsatisfiable Accept header maps to 406.
     */
    @Test
    void handleMediaTypeNotAcceptable_givenUnsatisfiableAccept_returnsNotAcceptable() {
        Map<String, String> response = handler
                .handleMediaTypeNotAcceptable(mock(HttpMediaTypeNotAcceptableException.class));

        assertThat(response)
                .containsEntry("error", "Not Acceptable")
                .containsEntry("message", "None of the requested media types are supported");
    }
}
