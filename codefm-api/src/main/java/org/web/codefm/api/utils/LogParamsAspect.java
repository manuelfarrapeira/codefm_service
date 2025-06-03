package org.web.codefm.api.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.web.codefm.domain.session.SessionUser;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class LogParamsAspect implements Ordered {


    public static final String STARTING_REQUEST_WHITH_USER = "Starting request on {} ({}) | user: {} , roles -> {} , permissions -> {}";

    public static final String STARTING_REQUEST_WITHOUT_USER = "Starting request on {} ({})";

    public static final String PRINT_PARAMS = "Request params: {}";

    private static final Integer DEFAULT_ORDER = 1;

    private final SessionUser sessionUser;

    private final HttpServletRequest request;

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Around(value = "@annotation(Logged)", argNames = "Logged")
    public Object printCustomLog(final ProceedingJoinPoint joinPoint, final Logged logged) throws Throwable {

        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = methodSignature.getParameterNames();

        String requestURI = request != null ? request.getRequestURI() : "";
        String requestMethod = request != null ? request.getMethod() : "";

        if (sessionUser.getUsername() != null) {

            String roles = StringUtils.join(sessionUser.getRoles(), " , ");
            String permisos = StringUtils.join(sessionUser.getPermisos(), " , ");

            log.info(STARTING_REQUEST_WHITH_USER, requestURI, requestMethod, sessionUser.getUsername(), roles, permisos);

        } else {
            log.info(STARTING_REQUEST_WITHOUT_USER, requestURI, requestMethod);
        }

        if (parameterNames.length > 0) {
            log.info(PRINT_PARAMS, printParams(args, parameterNames));
        }

        return joinPoint.proceed();
    }

    private String printParams(Object[] args, String[] parameterNames) {
        return IntStream.range(0, args.length)
                .mapToObj(i -> parameterNames[i] + "=" +
                        Optional.ofNullable(args[i])
                                .map(Object::toString)
                                .orElse("null"))
                .collect(Collectors.joining(", "));
    }


}
