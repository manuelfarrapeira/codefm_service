package org.web.codefm.api.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.session.SessionUser;

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

    @InjectMocks
    private LogParamsAspect logParamsAspect;

    @BeforeEach
    void setUp() {
        logParamsAspect = new LogParamsAspect(sessionUser, request);
    }

    @Test
    void logsWithUserInfoWhenSessionUserPresent() throws Throwable {
        when(sessionUser.getUsername()).thenReturn("user1");
        when(sessionUser.getRoles()).thenReturn(java.util.List.of("ADMIN", "USER"));
        when(sessionUser.getPermisos()).thenReturn(java.util.List.of("READ", "WRITE"));
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"param1"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{"value1"});
        when(joinPoint.proceed()).thenReturn("result");

        Object result = logParamsAspect.printCustomLog(joinPoint, null);

        Assertions.assertEquals("result", result);
    }

    @Test
    void logsWithoutUserInfoWhenSessionUserNull() throws Throwable {
        when(sessionUser.getUsername()).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("POST");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"param1"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{"value1"});
        when(joinPoint.proceed()).thenReturn("result");

        Object result = logParamsAspect.printCustomLog(joinPoint, null);

        Assertions.assertEquals("result", result);
    }

    @Test
    void handlesEmptyParameterNamesGracefully() throws Throwable {
        when(sessionUser.getUsername()).thenReturn("user1");
        when(sessionUser.getRoles()).thenReturn(java.util.List.of());
        when(sessionUser.getPermisos()).thenReturn(java.util.List.of());
        when(request.getRequestURI()).thenReturn("/api/empty");
        when(request.getMethod()).thenReturn("PUT");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{});
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn("result");

        Object result = logParamsAspect.printCustomLog(joinPoint, null);

        Assertions.assertEquals("result", result);
    }

    @Test
    void handlesNullRequestGracefully() throws Throwable {
        logParamsAspect = new LogParamsAspect(sessionUser, null);
        when(sessionUser.getUsername()).thenReturn("user1");
        when(sessionUser.getRoles()).thenReturn(java.util.List.of("ADMIN"));
        when(sessionUser.getPermisos()).thenReturn(java.util.List.of("READ"));
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"param1"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{"value1"});
        when(joinPoint.proceed()).thenReturn("result");

        Object result = logParamsAspect.printCustomLog(joinPoint, null);

        Assertions.assertEquals("result", result);
    }

    @Test
    void printsNullForNullArgumentValues() throws Throwable {
        when(sessionUser.getUsername()).thenReturn("user1");
        when(sessionUser.getRoles()).thenReturn(java.util.List.of("ADMIN"));
        when(sessionUser.getPermisos()).thenReturn(java.util.List.of("READ"));
        when(request.getRequestURI()).thenReturn("/api/null");
        when(request.getMethod()).thenReturn("PATCH");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"param1", "param2"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{null, "value2"});
        when(joinPoint.proceed()).thenReturn("result");

        Object result = logParamsAspect.printCustomLog(joinPoint, null);

        org.junit.jupiter.api.Assertions.assertEquals("result", result);
    }
}