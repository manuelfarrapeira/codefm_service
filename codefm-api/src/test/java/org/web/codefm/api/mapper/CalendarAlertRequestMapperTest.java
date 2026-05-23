package org.web.codefm.api.mapper;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.domain.exception.teachernotebook.CalendarAlertValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.model.CalendarAlertRequestDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CalendarAlertRequestMapperTest {

    @Spy
    @InjectMocks
    private CalendarAlertRequestMapperImpl mapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    @BeforeEach
    void beforeEach() {
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(this.messageSource.getMessage(eq(MessageKeys.CALENDAR_ALERT_VALIDATION_DATE_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("Alert date is required.");
    }

    @Nested
    class ToDomain {

        @Test
        void when_all_fields_are_valid_expect_mapped_alert() {
            final CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
            dto.setDate("15/03/2026");
            dto.setTitle("Parent meeting");
            dto.setDescription("Quarterly parent meeting");
            dto.setStartTime("09:00");
            dto.setEndTime("10:30");

            final CalendarAlert result = CalendarAlertRequestMapperTest.this.mapper.toDomain(dto);

            assertThat(result).isNotNull();
            assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 3, 15));
            assertThat(result.getTitle()).isEqualTo("Parent meeting");
            assertThat(result.getDescription()).isEqualTo("Quarterly parent meeting");
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(10, 30));
            assertThat(result.getId()).isNull();
            assertThat(result.getTeacherId()).isNull();
        }

        @Test
        void when_optional_fields_are_null_expect_null_optional_values() {
            final CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
            dto.setDate("15/03/2026");
            dto.setTitle("Parent meeting");
            dto.setDescription(null);
            dto.setStartTime(null);
            dto.setEndTime(null);

            final CalendarAlert result = CalendarAlertRequestMapperTest.this.mapper.toDomain(dto);

            assertThat(result).isNotNull();
            assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 3, 15));
            assertThat(result.getTitle()).isEqualTo("Parent meeting");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getStartTime()).isNull();
            assertThat(result.getEndTime()).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void when_date_is_null_or_empty_expect_null_date(final String date) {
            final CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
            dto.setDate(date);
            dto.setTitle("Parent meeting");

            final CalendarAlert result = CalendarAlertRequestMapperTest.this.mapper.toDomain(dto);

            assertThat(result).isNotNull();
            assertThat(result.getDate()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"2026-03-15", "32/03/2026", "15/13/2026", "not-a-date"})
        void when_date_is_invalid_expect_validation_exception(final String invalidDate) {
            final CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
            dto.setDate(invalidDate);
            dto.setTitle("Parent meeting");
            final ThrowingCallable action = () -> CalendarAlertRequestMapperTest.this.mapper.toDomain(dto);

            assertThatThrownBy(action)
                    .isInstanceOf(CalendarAlertValidationException.class)
                    .satisfies(throwable -> assertThat(((CalendarAlertValidationException) throwable).getErrors())
                            .isNotEmpty()
                            .first()
                            .satisfies(error -> assertThat(error.getParam()).isEqualTo("date")));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void when_start_time_is_null_or_empty_expect_null_start_time(final String startTime) {
            final CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
            dto.setDate("15/03/2026");
            dto.setTitle("Parent meeting");
            dto.setStartTime(startTime);

            final CalendarAlert result = CalendarAlertRequestMapperTest.this.mapper.toDomain(dto);

            assertThat(result.getStartTime()).isNull();
        }

        @Test
        void when_start_time_is_midnight_expect_midnight_time() {
            final CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
            dto.setDate("15/03/2026");
            dto.setTitle("Parent meeting");
            dto.setStartTime("00:00");

            final CalendarAlert result = CalendarAlertRequestMapperTest.this.mapper.toDomain(dto);

            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(0, 0));
        }

        @Test
        void when_alert_is_mapped_expect_id_and_teacher_id_ignored() {
            final CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
            dto.setDate("15/03/2026");
            dto.setTitle("Parent meeting");

            final CalendarAlert result = CalendarAlertRequestMapperTest.this.mapper.toDomain(dto);

            assertThat(result.getId()).isNull();
            assertThat(result.getTeacherId()).isNull();
        }
    }
}
