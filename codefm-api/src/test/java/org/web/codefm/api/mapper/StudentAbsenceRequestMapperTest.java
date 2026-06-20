package org.web.codefm.api.mapper;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.web.codefm.domain.exception.teachernotebook.StudentAbsenceValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.session.SessionUser;

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentAbsenceRequestMapperTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    private StudentAbsenceRequestMapper mapper;

    @BeforeEach
    void beforeEach() {
        this.mapper = new StudentAbsenceRequestMapper();
        ReflectionTestUtils.setField(this.mapper, "messageSource", this.messageSource);
        ReflectionTestUtils.setField(this.mapper, "sessionUser", this.sessionUser);
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(this.messageSource.getMessage(eq(MessageKeys.ABSENCE_VALIDATION_DATE_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("Date is required.");
        when(this.messageSource.getMessage(eq(MessageKeys.ABSENCE_VALIDATION_DATE_INVALID), eq(null), any(Locale.class)))
                .thenReturn("Invalid date format.");
    }

    @Nested
    class ParseDate {

        @Test
        void when_date_is_valid_expect_local_date() {
            final LocalDate result = StudentAbsenceRequestMapperTest.this.mapper.parseDate("15/03/2026");

            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 15));
        }

        @Test
        void when_first_day_of_year_is_parsed_expect_local_date() {
            final LocalDate result = StudentAbsenceRequestMapperTest.this.mapper.parseDate("01/01/2025");

            assertThat(result).isEqualTo(LocalDate.of(2025, 1, 1));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void when_date_is_null_or_blank_expect_validation_exception(final String dateString) {
            final ThrowingCallable action = () -> StudentAbsenceRequestMapperTest.this.mapper.parseDate(dateString);

            assertThatThrownBy(action)
                    .isInstanceOf(StudentAbsenceValidationException.class)
                    .satisfies(throwable -> assertThat(((StudentAbsenceValidationException) throwable).getErrors())
                            .isNotEmpty()
                            .first()
                            .satisfies(error -> assertThat(error.getParam()).isEqualTo("date")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"2026-03-15", "32/03/2026", "15/13/2026", "not-a-date", "15-03-2026"})
        void when_date_format_is_invalid_expect_validation_exception(final String invalidDate) {
            final ThrowingCallable action = () -> StudentAbsenceRequestMapperTest.this.mapper.parseDate(invalidDate);

            assertThatThrownBy(action)
                    .isInstanceOf(StudentAbsenceValidationException.class)
                    .satisfies(throwable -> assertThat(((StudentAbsenceValidationException) throwable).getErrors())
                            .isNotEmpty()
                            .first()
                            .satisfies(error -> assertThat(error.getParam()).isEqualTo("date")));
        }
    }
}
