package org.web.codefm.api.utils;

import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.session.SessionUser;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocaleAspectTest {

    @Mock
    private SessionUser sessionUser;

    @Mock
    private JoinPoint joinPoint;

    @InjectMocks
    private LocaleAspect localeAspect;

    private org.web.codefm.api.utils.Locale localeAnnotation;

    @BeforeEach
    void setUp() {
        this.localeAnnotation = new org.web.codefm.api.utils.Locale() {
            @Override
            public int value() {
                return 0;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return org.web.codefm.api.utils.Locale.class;
            }
        };
    }

    @ParameterizedTest
    @CsvSource({
            "es, es",
            "ES, es",
            "ga, ga",
            "GA, ga"
    })
    void setLocaleToExpected_whenAcceptLanguageHeaderMatches(String header, String expectedLocale) {
        when(this.joinPoint.getArgs()).thenReturn(new Object[]{header});

        this.localeAspect.setLocale(this.joinPoint, this.localeAnnotation);

        verify(this.sessionUser).setLocale(new Locale(expectedLocale));
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "fr", "de", "pt", "", " es", "  es", "\tes"})
    void setLocaleToEnglish_whenAcceptLanguageHeaderIsInvalidOrEmpty(String header) {
        when(this.joinPoint.getArgs()).thenReturn(new Object[]{header});

        this.localeAspect.setLocale(this.joinPoint, this.localeAnnotation);

        verify(this.sessionUser).setLocale(Locale.ENGLISH);
    }

    @Test
    void setLocaleToEnglish_whenAcceptLanguageHeaderIsNull() {
        when(this.joinPoint.getArgs()).thenReturn(new Object[]{null});

        this.localeAspect.setLocale(this.joinPoint, this.localeAnnotation);

        verify(this.sessionUser).setLocale(Locale.ENGLISH);
    }

    @Test
    void setLocaleToEnglish_whenExceptionOccurs() {
        when(this.joinPoint.getArgs()).thenThrow(new RuntimeException("Error retrieving arguments"));

        this.localeAspect.setLocale(this.joinPoint, this.localeAnnotation);

        verify(this.sessionUser).setLocale(Locale.ENGLISH);
    }

    @Test
    void setLocaleToEnglish_whenLocaleIdIsOutOfBounds() {
        final org.web.codefm.api.utils.Locale outOfBoundsAnnotation = new org.web.codefm.api.utils.Locale() {
            @Override
            public int value() {
                return 5;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return org.web.codefm.api.utils.Locale.class;
            }
        };
        when(this.joinPoint.getArgs()).thenReturn(new Object[]{"es"});

        this.localeAspect.setLocale(this.joinPoint, outOfBoundsAnnotation);

        verify(this.sessionUser).setLocale(Locale.ENGLISH);
    }

    @Test
    void getOrderReturnsOne() {
        final int order = this.localeAspect.getOrder();

        assertEquals(1, order);
    }
}
