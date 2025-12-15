package org.web.codefm.api.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.web.codefm.domain.session.SessionUser;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class LocaleAspect implements Ordered {

    private static final Integer DEFAULT_ORDER = 1;

    private final SessionUser sessionUser;

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Before(value = "@annotation(localeAnnotation)")
    public void setLocale(final JoinPoint joinPoint, final Locale localeAnnotation) {

        try {
            int localeId = localeAnnotation.value();
            sessionUser.setLocale(getLocale((String) joinPoint.getArgs()[localeId]));
        } catch (Exception e) {
            log.error("Error get Locale", e);
            sessionUser.setLocale(java.util.Locale.ENGLISH);
        }

        log.info("Locale user: ", sessionUser.getLocale());

    }

    private java.util.Locale getLocale(String acceptLanguage) {
        if ("es".equalsIgnoreCase(acceptLanguage)) {
            return new java.util.Locale("es");
        } else {
            return java.util.Locale.ENGLISH;
        }
    }


}
