package org.web.codefm.api.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.session.SessionUser;

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
    localeAnnotation = new org.web.codefm.api.utils.Locale() {
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

  @Test
  void setLocaleToSpanish_whenAcceptLanguageHeaderIsEs() {
    when(joinPoint.getArgs()).thenReturn(new Object[]{"es"});

    localeAspect.setLocale(joinPoint, localeAnnotation);

    verify(sessionUser).setLocale(new Locale("es"));
  }

  @Test
  void setLocaleToEnglish_whenAcceptLanguageHeaderIsEn() {
    when(joinPoint.getArgs()).thenReturn(new Object[]{"en"});

    localeAspect.setLocale(joinPoint, localeAnnotation);

    verify(sessionUser).setLocale(Locale.ENGLISH);
  }

  @Test
  void setLocaleToEnglish_whenAcceptLanguageHeaderIsUnknown() {
    when(joinPoint.getArgs()).thenReturn(new Object[]{"fr"});

    localeAspect.setLocale(joinPoint, localeAnnotation);

    verify(sessionUser).setLocale(Locale.ENGLISH);
  }

  @Test
  void setLocaleToSpanish_whenAcceptLanguageHeaderIsMixedCase() {
    when(joinPoint.getArgs()).thenReturn(new Object[]{"ES"});

    localeAspect.setLocale(joinPoint, localeAnnotation);

    verify(sessionUser).setLocale(new Locale("es"));
  }

  @Test
  void setLocaleToEnglish_whenAcceptLanguageHeaderIsNull() {
    when(joinPoint.getArgs()).thenReturn(new Object[]{null});

    localeAspect.setLocale(joinPoint, localeAnnotation);

    verify(sessionUser).setLocale(Locale.ENGLISH);
  }

  @Test
  void setLocaleToEnglish_whenAcceptLanguageHeaderIsEmpty() {
    when(joinPoint.getArgs()).thenReturn(new Object[]{""});

    localeAspect.setLocale(joinPoint, localeAnnotation);

    verify(sessionUser).setLocale(Locale.ENGLISH);
  }

  @Test
  void setLocaleToEnglish_whenExceptionOccurs() {
    when(joinPoint.getArgs()).thenThrow(new RuntimeException("Error retrieving arguments"));

    localeAspect.setLocale(joinPoint, localeAnnotation);

    verify(sessionUser).setLocale(Locale.ENGLISH);
  }

  @Test
  void setLocaleToEnglish_whenLocaleIdIsOutOfBounds() {
    org.web.codefm.api.utils.Locale outOfBoundsAnnotation = new org.web.codefm.api.utils.Locale() {
      @Override
      public int value() {
        return 5;
      }

      @Override
      public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return org.web.codefm.api.utils.Locale.class;
      }
    };
    when(joinPoint.getArgs()).thenReturn(new Object[]{"es"});

    localeAspect.setLocale(joinPoint, outOfBoundsAnnotation);

    verify(sessionUser).setLocale(Locale.ENGLISH);
  }

  @Test
  void getOrderReturnsOne() {
    int order = localeAspect.getOrder();

    assertEquals(1, order);
  }

  @Test
  void setLocaleToEnglish_whenAcceptLanguageHeaderIsEsButWithLeadingWhitespace() {
    when(joinPoint.getArgs()).thenReturn(new Object[]{" es"});

    localeAspect.setLocale(joinPoint, localeAnnotation);

    verify(sessionUser).setLocale(Locale.ENGLISH);
  }
}

