package com.distributed2pc.participant.feature.fault.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CrashInterceptor}.
 */
@ExtendWith(MockitoExtension.class)
class CrashInterceptorTest {

    @Mock
    CrashFaultStrategy crashFaultStrategy;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    CrashInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new CrashInterceptor(crashFaultStrategy);
    }

    @Test
    void preHandle_givenCrashNotActive_allowsRequest() throws Exception {
        when(crashFaultStrategy.isActive()).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_givenCrashActive_rejectsWithServiceUnavailable() throws Exception {
        when(crashFaultStrategy.isActive()).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isFalse();
        verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Participant is crashed");
    }
}
