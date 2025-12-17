package org.web.codefm.domain.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);
    assertNull(result);
  }

  @Test
  void getParameter_shouldReturnString_whenTypeIsString() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "testValue");

    String result = sessionUser.getParameter(SessionParameter.TEACHER_ID, String.class);

    assertEquals("testValue", result);
  }

  @Test
  void getParameter_shouldReturnInteger_whenTypeIsInteger() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "42");

    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);

    assertEquals(42, result);
  }

  @Test
  void getParameter_shouldReturnLong_whenTypeIsLong() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "9999999999");

    Long result = sessionUser.getParameter(SessionParameter.TEACHER_ID, Long.class);

    assertEquals(9999999999L, result);
  }

  @Test
  void getParameter_shouldReturnBoolean_whenTypeIsBoolean() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "true");

    Boolean result = sessionUser.getParameter(SessionParameter.TEACHER_ID, Boolean.class);

    assertTrue(result);
  }

  @Test
  void getParameter_shouldReturnFalseBoolean_whenValueIsFalse() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "false");

    Boolean result = sessionUser.getParameter(SessionParameter.TEACHER_ID, Boolean.class);

    assertFalse(result);
  }

  @Test
  void getParameter_shouldReturnDouble_whenTypeIsDouble() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "3.14");

    Double result = sessionUser.getParameter(SessionParameter.TEACHER_ID, Double.class);

    assertEquals(3.14, result);
  }

  @Test
  void getParameter_shouldReturnFloat_whenTypeIsFloat() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "2.5");

    Float result = sessionUser.getParameter(SessionParameter.TEACHER_ID, Float.class);

    assertEquals(2.5f, result, 0.001f);
  }

  @Test
  void getParameter_shouldThrowIllegalArgumentException_whenIntegerValueIsInvalid() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "notANumber");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class));

    assertTrue(exception.getMessage().contains("Cannot convert parameter"));
    assertTrue(exception.getMessage().contains("teacher_id"));
  }

  @Test
  void getParameter_shouldThrowIllegalArgumentException_whenLongValueIsInvalid() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "abc123");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> sessionUser.getParameter(SessionParameter.TEACHER_ID, Long.class));

    assertTrue(exception.getMessage().contains("Cannot convert parameter"));
  }

  @Test
  void getParameter_shouldThrowIllegalArgumentException_whenDoubleValueIsInvalid() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "3.14.15");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> sessionUser.getParameter(SessionParameter.TEACHER_ID, Double.class));

    assertTrue(exception.getMessage().contains("Cannot convert parameter"));
  }

  @Test
  void getParameter_shouldThrowIllegalArgumentException_whenFloatValueIsInvalid() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "notFloat");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> sessionUser.getParameter(SessionParameter.TEACHER_ID, Float.class));

    assertTrue(exception.getMessage().contains("Cannot convert parameter"));
  }

  @Test
  void getParameter_shouldThrowIllegalArgumentException_whenTypeIsUnsupported() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "value");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> sessionUser.getParameter(SessionParameter.TEACHER_ID, java.time.LocalDate.class));

    assertTrue(exception.getMessage().contains("Cannot convert parameter"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "-5", "999999", "1"})
  void getParameter_shouldConvertMultipleIntegerValues(String intValue) {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), intValue);

    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);

    assertEquals(Integer.parseInt(intValue), result);
  }

  @Test
  void getParameter_shouldHandleNegativeNumbers() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "-42");

    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);

    assertEquals(-42, result);
  }

  @Test
  void getParameter_shouldHandleZero() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "0");

    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);

    assertEquals(0, result);
  }

  @Test
  void getParameter_shouldMaintainSessionUserState() {
    sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "123");
    sessionUser.setLocale(Locale.FRENCH);

    Integer result = sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);

    assertEquals(123, result);
    assertEquals(Locale.FRENCH, sessionUser.getLocale());
    assertEquals("tester", sessionUser.getUsername());
  }

  @Test
  void getParameterAsList_shouldReturnNull_whenParameterDoesNotExist() {
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

