package org.web.codefm.domain.session;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

class SessionUserTest {

    private SessionUser sessionUser;

    @BeforeEach
    void beforeEach() {
        this.sessionUser = new SessionUser();
        this.sessionUser.setId("user123");
        this.sessionUser.setUsername("tester");
        this.sessionUser.setEmail("test@example.com");
        this.sessionUser.setLocale(Locale.ENGLISH);
    }

    @Nested
    class GetParameter {

        @Test
        void when_parameter_does_not_exist_expect_null() {
            final Integer result = SessionUserTest.this.sessionUser.getParameter(SessionParameter.TEACHER_ID);

            assertThat(result).isNull();
        }

        @Test
        void when_value_is_valid_expect_integer() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "42");

            final Integer result = SessionUserTest.this.sessionUser.getParameter(SessionParameter.TEACHER_ID);

            assertThat(result).isEqualTo(42);
        }

        @Test
        void when_value_is_invalid_expect_illegal_argument_exception() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "notANumber");
            final ThrowingCallable action = () -> SessionUserTest.this.sessionUser.getParameter(SessionParameter.TEACHER_ID);

            assertThatThrownBy(action)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot convert parameter")
                    .hasMessageContaining("teacher_id");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-5", "999999", "1"})
        void when_integer_values_are_valid_expect_converted_value(final String intValue) {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), intValue);

            final Integer result = SessionUserTest.this.sessionUser.getParameter(SessionParameter.TEACHER_ID);

            assertThat(result).isEqualTo(Integer.parseInt(intValue));
        }

        @Test
        void when_value_is_negative_expect_negative_number() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "-42");

            final Integer result = SessionUserTest.this.sessionUser.getParameter(SessionParameter.TEACHER_ID);

            assertThat(result).isEqualTo(-42);
        }

        @Test
        void when_parameter_is_read_expect_session_state_preserved() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "123");
            SessionUserTest.this.sessionUser.setLocale(Locale.FRENCH);

            final Integer result = SessionUserTest.this.sessionUser.getParameter(SessionParameter.TEACHER_ID);

            assertThat(result).isEqualTo(123);
            assertThat(SessionUserTest.this.sessionUser.getLocale()).isEqualTo(Locale.FRENCH);
            assertThat(SessionUserTest.this.sessionUser.getUsername()).isEqualTo("tester");
        }
    }

    @Nested
    class GetParameterAsList {

        @Test
        void when_parameter_does_not_exist_expect_empty_list() {
            final List<String> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                    String.class);

            assertThat(result).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "STRINGS:role1,role2,role3:String",
                "INTEGERS:1,2,3,4,5:Integer",
                "LONGS:1000000,2000000,3000000:Long"
        })
        void when_element_type_is_supported_expect_list_with_correct_values(final String testCase) {
            final String[] parts = testCase.split(":");
            final String value = parts[1];
            final String typeName = parts[2];
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), value);

            if ("String".equals(typeName)) {
                final List<String> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                        String.class);
                assertThat(result).containsExactly("role1", "role2", "role3");
            } else if ("Integer".equals(typeName)) {
                final List<Integer> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                        Integer.class);
                assertThat(result).containsExactly(1, 2, 3, 4, 5);
            } else if ("Long".equals(typeName)) {
                final List<Long> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                        Long.class);
                assertThat(result).containsExactly(1000000L, 2000000L, 3000000L);
            } else {
                fail("Unexpected typeName: " + typeName);
            }
        }

        @Test
        void when_element_type_is_boolean_expect_boolean_list() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "true,false,true");

            final List<Boolean> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                    Boolean.class);

            assertThat(result).containsExactly(true, false, true);
        }

        @Test
        void when_element_type_is_double_expect_double_list() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "1.5,2.7,3.14");

            final List<Double> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                    Double.class);

            assertThat(result).containsExactly(1.5, 2.7, 3.14);
        }

        @Test
        void when_element_type_is_float_expect_float_list() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "1.5,2.5,3.5");

            final List<Float> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                    Float.class);

            assertThat(result).containsExactly(1.5f, 2.5f, 3.5f);
        }

        @Test
        void when_list_contains_whitespace_expect_trimmed_values() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "role1 , role2 , role3");

            final List<String> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                    String.class);

            assertThat(result).containsExactly("role1", "role2", "role3");
        }

        @Test
        void when_integer_list_contains_negative_numbers_expect_negative_values() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "-1,-2,-3");

            final List<Integer> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                    Integer.class);

            assertThat(result).containsExactly(-1, -2, -3);
        }

        @Test
        void when_value_has_no_commas_expect_single_element_list() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "42");

            final List<Integer> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                    Integer.class);

            assertThat(result).containsExactly(42);
        }

        @Test
        void when_integer_conversion_fails_expect_illegal_argument_exception() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "1,abc,3");
            final ThrowingCallable action = () -> SessionUserTest.this.sessionUser.getParameterAsList(
                    SessionParameter.TEACHER_ID, Integer.class);

            assertThatThrownBy(action)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot convert list elements");
        }

        @Test
        void when_double_conversion_fails_expect_illegal_argument_exception() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "1.5,notADouble,3.5");
            final ThrowingCallable action = () -> SessionUserTest.this.sessionUser.getParameterAsList(
                    SessionParameter.TEACHER_ID, Double.class);

            assertThatThrownBy(action)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot convert list elements");
        }

        @Test
        void when_element_type_is_unsupported_expect_illegal_argument_exception() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "value1,value2");
            final ThrowingCallable action = () -> SessionUserTest.this.sessionUser.getParameterAsList(
                    SessionParameter.TEACHER_ID, LocalDate.class);

            assertThatThrownBy(action)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot convert list elements");
        }

        @Test
        void when_list_contains_empty_string_element_expect_empty_value_preserved() {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), "role1,,role3");

            final List<String> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                    String.class);

            assertThat(result).containsExactly("role1", "", "role3");
        }

        @ParameterizedTest
        @ValueSource(strings = {"1,2,3", "10,20", "100"})
        void when_integer_list_has_multiple_formats_expect_non_null_elements(final String listValue) {
            SessionUserTest.this.sessionUser.getParameters().put(SessionParameter.TEACHER_ID.getClaimName(), listValue);

            final List<Integer> result = SessionUserTest.this.sessionUser.getParameterAsList(SessionParameter.TEACHER_ID,
                    Integer.class);

            assertThat(result).isNotEmpty().doesNotContainNull();
        }
    }
}
