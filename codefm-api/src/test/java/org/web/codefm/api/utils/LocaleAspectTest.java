package org.web.codefm.api.utils;

import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.session.SessionUser;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocaleAspectTest {

    @Mock
    private SessionUser sessionUser;

    @Mock
    private JoinPoint joinPoint;

    private LocaleAspect localeAspect;

    private org.web.codefm.api.utils.Locale localeAnnotation;

    @BeforeEach
    void beforeEach() {
        this.localeAspect = new LocaleAspect(this.sessionUser);
        this.localeAnnotation = this.buildLocaleAnnotation(0);
    }

    @Nested
    class SetLocale {

        @ParameterizedTest
        @CsvSource({
                "es, es",
                "ES, es",
                "ga, ga",
                "GA, ga"
        })
        void when_accept_language_matches_expect_locale_set(final String header, final String expectedLocale) {
            when(LocaleAspectTest.this.joinPoint.getArgs()).thenReturn(new Object[]{header});

            LocaleAspectTest.this.localeAspect.setLocale(LocaleAspectTest.this.joinPoint,
                    LocaleAspectTest.this.localeAnnotation);

            verify(LocaleAspectTest.this.sessionUser).setLocale(new Locale(expectedLocale));
        }

        @ParameterizedTest
        @ValueSource(strings = {"en", "fr", "de", "pt", "", " es", "  es", "\tes"})
        void when_accept_language_is_invalid_expect_english_locale(final String header) {
            when(LocaleAspectTest.this.joinPoint.getArgs()).thenReturn(new Object[]{header});

            LocaleAspectTest.this.localeAspect.setLocale(LocaleAspectTest.this.joinPoint,
                    LocaleAspectTest.this.localeAnnotation);

            verify(LocaleAspectTest.this.sessionUser).setLocale(Locale.ENGLISH);
        }

        @Test
        void when_accept_language_is_null_expect_english_locale() {
            when(LocaleAspectTest.this.joinPoint.getArgs()).thenReturn(new Object[]{null});

            LocaleAspectTest.this.localeAspect.setLocale(LocaleAspectTest.this.joinPoint,
                    LocaleAspectTest.this.localeAnnotation);

            verify(LocaleAspectTest.this.sessionUser).setLocale(Locale.ENGLISH);
        }

        @Test
        void when_exception_occurs_expect_english_locale() {
            when(LocaleAspectTest.this.joinPoint.getArgs()).thenThrow(new RuntimeException("Error retrieving arguments"));

            LocaleAspectTest.this.localeAspect.setLocale(LocaleAspectTest.this.joinPoint,
                    LocaleAspectTest.this.localeAnnotation);

            verify(LocaleAspectTest.this.sessionUser).setLocale(Locale.ENGLISH);
        }

        @Test
        void when_locale_index_is_out_of_bounds_expect_english_locale() {
            when(LocaleAspectTest.this.joinPoint.getArgs()).thenReturn(new Object[]{"es"});

            LocaleAspectTest.this.localeAspect.setLocale(LocaleAspectTest.this.joinPoint,
                    LocaleAspectTest.this.buildLocaleAnnotation(5));

            verify(LocaleAspectTest.this.sessionUser).setLocale(Locale.ENGLISH);
        }
    }

    @Nested
    class GetOrder {

        @Test
        void when_order_is_requested_expect_one() {
            final int order = LocaleAspectTest.this.localeAspect.getOrder();

            assertThat(order).isEqualTo(1);
        }
    }

    private org.web.codefm.api.utils.Locale buildLocaleAnnotation(final int value) {
        return new org.web.codefm.api.utils.Locale() {
            @Override
            public int value() {
                return value;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return org.web.codefm.api.utils.Locale.class;
            }
        };
    }
}
