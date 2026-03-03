package org.web.codefm.domain.session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class SessionUserTest {

  private SessionUser sessionUser;

  @BeforeEach
  void setUp() {
    sessionUser = new SessionUser();
    sessionUser.setId("user123");
    sessionUser.setUsername("tester");
    sessionUser.setEmail("test@example.com");
    sessionUser.setLocale(Locale.ENGLISH);
  }

  @Test
  void getParameter_shouldReturnNull_whenParameterDoesNotExist() {
    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID);
    assertNull(result);
  }

  @Test
  void getParameter_shouldReturnInteger_whenValueIsValid() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "42");

    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID);

    assertEquals(42, result);
  }

  @Test
  void getParameter_shouldThrowIllegalArgumentException_whenValueIsInvalid() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "notANumber");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> sessionUser.getParameter(SessionParameter.TEACHER_ID));

    assertTrue(exception.getMessage().contains("Cannot convert parameter"));
    assertTrue(exception.getMessage().contains("teacher_id"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "-5", "999999", "1"})
  void getParameter_shouldConvertMultipleIntegerValues(String intValue) {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), intValue);

    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID);

    assertEquals(Integer.parseInt(intValue), result);
  }

  @Test
  void getParameter_shouldHandleNegativeNumbers() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "-42");

    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID);

    assertEquals(-42, result);
  }

  @Test
  void getParameter_shouldHandleZero() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "0");

    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID);

    assertEquals(0, result);
  }

  @Test
  void getParameter_shouldMaintainSessionUserState() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "123");
    sessionUser.setLocale(Locale.FRENCH);

    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID);

    assertEquals(123, result);
    assertEquals(Locale.FRENCH, sessionUser.getLocale());
    assertEquals("tester", sessionUser.getUsername());
  }

  @Test
  void getParameterAsList_shouldReturnEmptyList_whenParameterDoesNotExist() {
    List<String> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, String.class);

    assertTrue(result.isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "STRINGS:role1,role2,role3:String",
      "INTEGERS:1,2,3,4,5:Integer",
      "LONGS:1000000,2000000,3000000:Long"
  })
  void getParameterAsList_shouldReturnListWithCorrectElementType(String testCase) {
    String[] parts = testCase.split(":");
    String value = parts[1];
    String typeName = parts[2];

    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), value);

    if ("String".equals(typeName)) {
      List<String> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, String.class);
      assertNotNull(result);
      assertEquals(3, result.size());
      assertEquals("role1", result.get(0));
      assertEquals("role2", result.get(1));
      assertEquals("role3", result.get(2));
    } else if ("Integer".equals(typeName)) {
      List<Integer> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, Integer.class);
      assertNotNull(result);
      assertEquals(5, result.size());
      assertEquals(1, result.get(0));
      assertEquals(3, result.get(2));
      assertEquals(5, result.get(4));
    } else if ("Long".equals(typeName)) {
      List<Long> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, Long.class);
      assertNotNull(result);
      assertEquals(3, result.size());
      assertEquals(1000000L, result.get(0));
      assertEquals(3000000L, result.get(2));
    }
  }

  @Test
  void getParameterAsList_shouldReturnListOfBooleans_whenElementTypeIsBoolean() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "true,false,true");

    List<Boolean> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, Boolean.class);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(result.get(0));
    assertFalse(result.get(1));
    assertTrue(result.get(2));
  }

  @Test
  void getParameterAsList_shouldReturnListOfDoubles_whenElementTypeIsDouble() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "1.5,2.7,3.14");

    List<Double> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, Double.class);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(1.5, result.get(0));
    assertEquals(2.7, result.get(1));
    assertEquals(3.14, result.get(2));
  }

  @Test
  void getParameterAsList_shouldReturnListOfFloats_whenElementTypeIsFloat() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "1.5,2.5,3.5");

    List<Float> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, Float.class);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(1.5f, result.get(0), 0.001f);
    assertEquals(2.5f, result.get(1), 0.001f);
    assertEquals(3.5f, result.get(2), 0.001f);
  }

  @Test
  void getParameterAsList_shouldTrimWhitespace_fromListElements() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "role1 , role2 , role3");

    List<String> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, String.class);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("role1", result.get(0));
    assertEquals("role2", result.get(1));
    assertEquals("role3", result.get(2));
  }

  @Test
  void getParameterAsList_shouldHandleNegativeNumbers_inIntegerList() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "-1,-2,-3");

    List<Integer> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, Integer.class);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(-1, result.get(0));
    assertEquals(-2, result.get(1));
    assertEquals(-3, result.get(2));
  }

  @Test
  void getParameterAsList_shouldReturnSingleElementList_whenValueHasNoCommas() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "42");

    List<Integer> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, Integer.class);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(42, result.get(0));
  }

  @Test
  void getParameterAsList_shouldThrowIllegalArgumentException_whenIntegerConversionFails() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "1,abc,3");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, Integer.class));

    assertTrue(exception.getMessage().contains("Cannot convert list elements"));
  }

  @Test
  void getParameterAsList_shouldThrowIllegalArgumentException_whenDoubleConversionFails() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "1.5,notADouble,3.5");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, Double.class));

    assertTrue(exception.getMessage().contains("Cannot convert list elements"));
  }

  @Test
  void getParameterAsList_shouldThrowIllegalArgumentException_whenElementTypeIsUnsupported() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "value1,value2");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, java.time.LocalDate.class));

    assertTrue(exception.getMessage().contains("Cannot convert list elements"));
  }

  @Test
  void getParameterAsList_shouldHandleEmptyStringElement() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "role1,,role3");

    List<String> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, String.class);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("role1", result.get(0));
    assertEquals("", result.get(1));
    assertEquals("role3", result.get(2));
  }

  @ParameterizedTest
  @ValueSource(strings = {"1,2,3", "10,20", "100"})
  void getParameterAsList_shouldHandleMultipleIntegerListFormats(String listValue) {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), listValue);

    List<Integer> result = sessionUser.getParameterAsList(SessionParameter.TEACHER_ID, Integer.class);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    result.forEach(Assertions::assertNotNull);
  }
}

