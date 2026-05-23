package org.web.codefm.api.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.session.SessionUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogParamsAspectTest {

    @Mock
    private SessionUser sessionUser;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private LogParamsAspect logParamsAspect;

    @BeforeEach
    void beforeEach() {
        this.logParamsAspect = new LogParamsAspect(this.sessionUser, this.request);
    }

    @Nested
    class PrintCustomLog {

        @Test
        void when_session_user_is_present_expect_result_returned() throws Throwable {
            when(LogParamsAspectTest.this.sessionUser.getUsername()).thenReturn("user1");
            when(LogParamsAspectTest.this.sessionUser.getRoles()).thenReturn(java.util.List.of("ADMIN", "USER"));
            when(LogParamsAspectTest.this.sessionUser.getPermisos()).thenReturn(java.util.List.of("READ", "WRITE"));
            when(LogParamsAspectTest.this.request.getRequestURI()).thenReturn("/api/test");
            when(LogParamsAspectTest.this.request.getMethod()).thenReturn("GET");
            when(LogParamsAspectTest.this.joinPoint.getSignature()).thenReturn(LogParamsAspectTest.this.methodSignature);
            when(LogParamsAspectTest.this.methodSignature.getParameterNames()).thenReturn(new String[]{"param1"});
            when(LogParamsAspectTest.this.joinPoint.getArgs()).thenReturn(new Object[]{"value1"});
            when(LogParamsAspectTest.this.joinPoint.proceed()).thenReturn("result");

            final Object result = LogParamsAspectTest.this.logParamsAspect.printCustomLog(LogParamsAspectTest.this.joinPoint,
                    null);

            assertThat(result).isEqualTo("result");
        }

        @Test
        void when_session_user_is_missing_expect_result_returned() throws Throwable {
            when(LogParamsAspectTest.this.sessionUser.getUsername()).thenReturn(null);
            when(LogParamsAspectTest.this.request.getRequestURI()).thenReturn("/api/test");
            when(LogParamsAspectTest.this.request.getMethod()).thenReturn("POST");
            when(LogParamsAspectTest.this.joinPoint.getSignature()).thenReturn(LogParamsAspectTest.this.methodSignature);
            when(LogParamsAspectTest.this.methodSignature.getParameterNames()).thenReturn(new String[]{"param1"});
            when(LogParamsAspectTest.this.joinPoint.getArgs()).thenReturn(new Object[]{"value1"});
            when(LogParamsAspectTest.this.joinPoint.proceed()).thenReturn("result");

            final Object result = LogParamsAspectTest.this.logParamsAspect.printCustomLog(LogParamsAspectTest.this.joinPoint,
                    null);

            assertThat(result).isEqualTo("result");
        }

        @Test
        void when_parameter_names_are_empty_expect_result_returned() throws Throwable {
            when(LogParamsAspectTest.this.sessionUser.getUsername()).thenReturn("user1");
            when(LogParamsAspectTest.this.sessionUser.getRoles()).thenReturn(java.util.List.of());
            when(LogParamsAspectTest.this.sessionUser.getPermisos()).thenReturn(java.util.List.of());
            when(LogParamsAspectTest.this.request.getRequestURI()).thenReturn("/api/empty");
            when(LogParamsAspectTest.this.request.getMethod()).thenReturn("PUT");
            when(LogParamsAspectTest.this.joinPoint.getSignature()).thenReturn(LogParamsAspectTest.this.methodSignature);
            when(LogParamsAspectTest.this.methodSignature.getParameterNames()).thenReturn(new String[]{});
            when(LogParamsAspectTest.this.joinPoint.getArgs()).thenReturn(new Object[]{});
            when(LogParamsAspectTest.this.joinPoint.proceed()).thenReturn("result");

            final Object result = LogParamsAspectTest.this.logParamsAspect.printCustomLog(LogParamsAspectTest.this.joinPoint,
                    null);

            assertThat(result).isEqualTo("result");
        }

        @Test
        void when_request_is_null_expect_result_returned() throws Throwable {
            LogParamsAspectTest.this.logParamsAspect = new LogParamsAspect(LogParamsAspectTest.this.sessionUser, null);
            when(LogParamsAspectTest.this.sessionUser.getUsername()).thenReturn("user1");
            when(LogParamsAspectTest.this.sessionUser.getRoles()).thenReturn(java.util.List.of("ADMIN"));
            when(LogParamsAspectTest.this.sessionUser.getPermisos()).thenReturn(java.util.List.of("READ"));
            when(LogParamsAspectTest.this.joinPoint.getSignature()).thenReturn(LogParamsAspectTest.this.methodSignature);
            when(LogParamsAspectTest.this.methodSignature.getParameterNames()).thenReturn(new String[]{"param1"});
            when(LogParamsAspectTest.this.joinPoint.getArgs()).thenReturn(new Object[]{"value1"});
            when(LogParamsAspectTest.this.joinPoint.proceed()).thenReturn("result");

            final Object result = LogParamsAspectTest.this.logParamsAspect.printCustomLog(LogParamsAspectTest.this.joinPoint,
                    null);

            assertThat(result).isEqualTo("result");
        }

        @Test
        void when_argument_value_is_null_expect_result_returned() throws Throwable {
            when(LogParamsAspectTest.this.sessionUser.getUsername()).thenReturn("user1");
            when(LogParamsAspectTest.this.sessionUser.getRoles()).thenReturn(java.util.List.of("ADMIN"));
            when(LogParamsAspectTest.this.sessionUser.getPermisos()).thenReturn(java.util.List.of("READ"));
            when(LogParamsAspectTest.this.request.getRequestURI()).thenReturn("/api/null");
            when(LogParamsAspectTest.this.request.getMethod()).thenReturn("PATCH");
            when(LogParamsAspectTest.this.joinPoint.getSignature()).thenReturn(LogParamsAspectTest.this.methodSignature);
            when(LogParamsAspectTest.this.methodSignature.getParameterNames()).thenReturn(new String[]{"param1", "param2"});
            when(LogParamsAspectTest.this.joinPoint.getArgs()).thenReturn(new Object[]{null, "value2"});
            when(LogParamsAspectTest.this.joinPoint.proceed()).thenReturn("result");

            final Object result = LogParamsAspectTest.this.logParamsAspect.printCustomLog(LogParamsAspectTest.this.joinPoint,
                    null);

            assertThat(result).isEqualTo("result");
        }
    }
}