package com.distributed2pc.coordinator.feature.fault.infrastructure;

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
 * Unit tests for {@link CrashCoordinatorInterceptor}.
 */
@ExtendWith(MockitoExtension.class)
class CrashCoordinatorInterceptorTest {

    @Mock
    CrashCoordinatorFaultStrategy crashStrategy;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    CrashCoordinatorInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new CrashCoordinatorInterceptor(crashStrategy);
    }

    @Test
    void preHandle_givenCrashActive_sends503AndReturnsFalse() throws Exception {
        when(crashStrategy.isActive()).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isFalse();
        verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Coordinator crashed");
    }

    @Test
    void preHandle_givenCrashNotActive_returnsTrueWithoutError() throws Exception {
        when(crashStrategy.isActive()).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }
}
