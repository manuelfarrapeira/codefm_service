package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.domain.exception.teachernotebook.CalendarAlertNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.CalendarAlertValidationException;
import org.web.codefm.domain.repository.teachernotebook.CalendarAlertRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarAlertServiceImplTest {

    @Mock
    private CalendarAlertRepository calendarAlertRepository;
    @Mock
    private MessageSource messageSource;
    @Mock
    private SessionUser sessionUser;

    private CalendarAlertServiceImpl calendarAlertService;

    private static final Integer TEACHER_ID = 1;

    @BeforeEach
    void beforeEach() {
        this.calendarAlertService = new CalendarAlertServiceImpl(this.calendarAlertRepository, this.messageSource, this.sessionUser);
        lenient().when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    }

    @Nested
    class GetCalendarAlerts {

        @Test
        void when_alerts_are_found_expect_alert_list() {
            final List<CalendarAlert> expectedAlerts = Arrays.asList(
                    CalendarAlert.builder().id(1).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 15)).title("Meeting").build(),
                    CalendarAlert.builder().id(2).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 20)).title("Exam").build()
            );

            when(calendarAlertRepository.findByTeacherId(TEACHER_ID)).thenReturn(expectedAlerts);

            final List<CalendarAlert> result = calendarAlertService.getCalendarAlerts();

            assertThat(result).isNotNull().hasSize(2);
            verify(calendarAlertRepository).findByTeacherId(TEACHER_ID);
        }

        @Test
        void when_no_alerts_are_found_expect_empty_list() {
            when(calendarAlertRepository.findByTeacherId(TEACHER_ID)).thenReturn(Collections.emptyList());

            final List<CalendarAlert> result = calendarAlertService.getCalendarAlerts();

            assertThat(result).isNotNull().isEmpty();
            verify(calendarAlertRepository).findByTeacherId(TEACHER_ID);
        }
    }

    @Nested
    class CreateCalendarAlert {

        @Test
        void when_data_is_valid_expect_alert_to_be_created() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Parent meeting")
                    .build();
            final CalendarAlert savedAlert = CalendarAlert.builder()
                    .id(1)
                    .teacherId(TEACHER_ID)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Parent meeting")
                    .build();

            when(calendarAlertRepository.save(any(CalendarAlert.class))).thenReturn(savedAlert);

            final CalendarAlert result = calendarAlertService.createCalendarAlert(alertToCreate);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getTitle()).isEqualTo("Parent meeting");
            assertThat(result.getTeacherId()).isEqualTo(TEACHER_ID);
            verify(calendarAlertRepository).save(any(CalendarAlert.class));
        }

        @Test
        void when_title_is_null_expect_validation_exception() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 3, 15))
                    .title(null)
                    .build();

            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.createCalendarAlert(alertToCreate);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).save(any());
        }

        @Test
        void when_title_is_empty_expect_validation_exception() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 3, 15))
                    .title("")
                    .build();

            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.createCalendarAlert(alertToCreate);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).save(any());
        }

        @Test
        void when_title_exceeds_one_hundred_characters_expect_validation_exception() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 3, 15))
                    .title("a".repeat(101))
                    .build();

            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.createCalendarAlert(alertToCreate);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).save(any());
        }

        @Test
        void when_date_is_null_expect_validation_exception() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(null)
                    .title("Valid title")
                    .build();

            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.createCalendarAlert(alertToCreate);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).save(any());
        }

        @Test
        void when_title_and_date_are_null_expect_validation_exception() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(null)
                    .title(null)
                    .build();

            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

            final ThrowingCallable call = () -> calendarAlertService.createCalendarAlert(alertToCreate);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(CalendarAlertValidationException.class);
            final CalendarAlertValidationException exception = (CalendarAlertValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(2);
            verify(calendarAlertRepository, never()).save(any());
        }

        @Test
        void when_end_time_is_provided_without_start_time_expect_validation_exception() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Valid title")
                    .endTime(LocalTime.of(10, 0))
                    .build();

            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.createCalendarAlert(alertToCreate);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).save(any());
        }

        @Test
        void when_end_time_is_before_start_time_expect_validation_exception() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Valid title")
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(9, 0))
                    .build();

            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.createCalendarAlert(alertToCreate);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).save(any());
        }

        @Test
        void when_end_time_equals_start_time_expect_validation_exception() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Valid title")
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(10, 0))
                    .build();

            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.createCalendarAlert(alertToCreate);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).save(any());
        }

        @Test
        void when_start_time_and_end_time_are_valid_expect_alert_to_be_created() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Parent meeting")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 0))
                    .build();
            final CalendarAlert savedAlert = CalendarAlert.builder()
                    .id(1)
                    .teacherId(TEACHER_ID)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Parent meeting")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 0))
                    .build();

            when(calendarAlertRepository.save(any(CalendarAlert.class))).thenReturn(savedAlert);

            final CalendarAlert result = calendarAlertService.createCalendarAlert(alertToCreate);

            assertThat(result).isNotNull();
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(10, 0));
            verify(calendarAlertRepository).save(any(CalendarAlert.class));
        }

        @Test
        void when_only_start_time_is_provided_expect_alert_to_be_created() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Parent meeting")
                    .startTime(LocalTime.of(9, 0))
                    .build();
            final CalendarAlert savedAlert = CalendarAlert.builder()
                    .id(1)
                    .teacherId(TEACHER_ID)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Parent meeting")
                    .startTime(LocalTime.of(9, 0))
                    .build();

            when(calendarAlertRepository.save(any(CalendarAlert.class))).thenReturn(savedAlert);

            final CalendarAlert result = calendarAlertService.createCalendarAlert(alertToCreate);

            assertThat(result).isNotNull();
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.getEndTime()).isNull();
            verify(calendarAlertRepository).save(any(CalendarAlert.class));
        }
    }

    @Nested
    class UpdateCalendarAlert {

        @Test
        void when_data_is_valid_expect_alert_to_be_updated() {
            final Integer alertId = 1;
            final CalendarAlert alertToUpdate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 4, 10))
                    .title("Updated title")
                    .description("Updated description")
                    .build();
            final CalendarAlert existingAlert = CalendarAlert.builder()
                    .id(alertId)
                    .teacherId(TEACHER_ID)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Original title")
                    .build();
            final CalendarAlert savedAlert = CalendarAlert.builder()
                    .id(alertId)
                    .teacherId(TEACHER_ID)
                    .date(LocalDate.of(2026, 4, 10))
                    .title("Updated title")
                    .description("Updated description")
                    .build();

            when(calendarAlertRepository.findByIdAndTeacherId(alertId, TEACHER_ID)).thenReturn(Optional.of(existingAlert));
            when(calendarAlertRepository.save(any(CalendarAlert.class))).thenReturn(savedAlert);

            final CalendarAlert result = calendarAlertService.updateCalendarAlert(alertId, alertToUpdate);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated title");
            assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 4, 10));
            verify(calendarAlertRepository).findByIdAndTeacherId(alertId, TEACHER_ID);
            verify(calendarAlertRepository).save(any(CalendarAlert.class));
        }

        @Test
        void when_alert_does_not_exist_expect_not_found_exception() {
            final Integer alertId = 999;
            final CalendarAlert alertToUpdate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 4, 10))
                    .title("Updated title")
                    .build();

            when(calendarAlertRepository.findByIdAndTeacherId(alertId, TEACHER_ID)).thenReturn(Optional.empty());
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Not found");
            final ThrowingCallable callable = () -> calendarAlertService.updateCalendarAlert(alertId, alertToUpdate);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertNotFoundException.class);
            verify(calendarAlertRepository, never()).save(any());
        }

        @Test
        void when_title_is_invalid_expect_validation_exception() {
            final Integer alertId = 1;
            final CalendarAlert alertToUpdate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 4, 10))
                    .title("")
                    .build();

            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.updateCalendarAlert(alertId, alertToUpdate);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).findByIdAndTeacherId(any(), any());
        }

        @Test
        void when_start_time_and_end_time_are_updated_expect_updated_times() {
            final Integer alertId = 1;
            final CalendarAlert alertToUpdate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 4, 10))
                    .title("Updated title")
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 30))
                    .build();
            final CalendarAlert existingAlert = CalendarAlert.builder()
                    .id(alertId)
                    .teacherId(TEACHER_ID)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Original title")
                    .build();
            final CalendarAlert savedAlert = CalendarAlert.builder()
                    .id(alertId)
                    .teacherId(TEACHER_ID)
                    .date(LocalDate.of(2026, 4, 10))
                    .title("Updated title")
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 30))
                    .build();

            when(calendarAlertRepository.findByIdAndTeacherId(alertId, TEACHER_ID)).thenReturn(Optional.of(existingAlert));
            when(calendarAlertRepository.save(any(CalendarAlert.class))).thenReturn(savedAlert);

            final CalendarAlert result = calendarAlertService.updateCalendarAlert(alertId, alertToUpdate);

            assertThat(result).isNotNull();
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(14, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(15, 30));
        }
    }

    @Nested
    class DeleteCalendarAlert {

        @Test
        void when_alert_exists_expect_alert_to_be_deleted() {
            final Integer alertId = 1;
            final CalendarAlert existingAlert = CalendarAlert.builder()
                    .id(alertId)
                    .teacherId(TEACHER_ID)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("To delete")
                    .build();

            when(calendarAlertRepository.findByIdAndTeacherId(alertId, TEACHER_ID)).thenReturn(Optional.of(existingAlert));
            doNothing().when(calendarAlertRepository).deleteById(alertId);

            calendarAlertService.deleteCalendarAlert(alertId);

            verify(calendarAlertRepository).findByIdAndTeacherId(alertId, TEACHER_ID);
            verify(calendarAlertRepository).deleteById(alertId);
        }

        @Test
        void when_alert_does_not_exist_expect_not_found_exception() {
            final Integer alertId = 999;

            when(calendarAlertRepository.findByIdAndTeacherId(alertId, TEACHER_ID)).thenReturn(Optional.empty());
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Not found");
            final ThrowingCallable callable = () -> calendarAlertService.deleteCalendarAlert(alertId);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertNotFoundException.class);
            verify(calendarAlertRepository, never()).deleteById(any());
        }
    }

    @Nested
    class GetCalendarAlertsByYearAndMonth {

        @Test
        void when_year_and_month_are_valid_expect_alerts() {
            final Integer year = 2026;
            final Integer month = 3;
            final List<CalendarAlert> expectedAlerts = Arrays.asList(
                    CalendarAlert.builder().id(1).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 15)).title("Meeting").build()
            );

            when(calendarAlertRepository.findByTeacherIdAndYearAndMonth(TEACHER_ID, year, month)).thenReturn(expectedAlerts);

            final List<CalendarAlert> result = calendarAlertService.getCalendarAlertsByYearAndMonth(year, month);

            assertThat(result).isNotNull().hasSize(1);
            verify(calendarAlertRepository).findByTeacherIdAndYearAndMonth(TEACHER_ID, year, month);
        }

        @Test
        void when_no_alerts_are_found_expect_empty_list() {
            final Integer year = 2026;
            final Integer month = 6;

            when(calendarAlertRepository.findByTeacherIdAndYearAndMonth(TEACHER_ID, year, month)).thenReturn(Collections.emptyList());

            final List<CalendarAlert> result = calendarAlertService.getCalendarAlertsByYearAndMonth(year, month);

            assertThat(result).isNotNull().isEmpty();
            verify(calendarAlertRepository).findByTeacherIdAndYearAndMonth(TEACHER_ID, year, month);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 13, 14, 100})
        void when_month_is_invalid_expect_validation_exception(int invalidMonth) {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.getCalendarAlertsByYearAndMonth(2026, invalidMonth);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonth(any(), any(), any());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        void when_year_is_invalid_expect_validation_exception(int invalidYear) {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.getCalendarAlertsByYearAndMonth(invalidYear, 3);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonth(any(), any(), any());
        }

        @Test
        void when_both_year_and_month_are_invalid_expect_validation_exception() {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

            final ThrowingCallable call = () -> calendarAlertService.getCalendarAlertsByYearAndMonth(-1, 13);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(CalendarAlertValidationException.class);
            final CalendarAlertValidationException exception = (CalendarAlertValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(2);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonth(any(), any(), any());
        }

        @Test
        void when_year_is_null_expect_validation_exception() {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.getCalendarAlertsByYearAndMonth(null, 3);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonth(any(), any(), any());
        }

        @Test
        void when_month_is_null_expect_validation_exception() {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.getCalendarAlertsByYearAndMonth(2026, null);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonth(any(), any(), any());
        }
    }

    @Nested
    class GetCalendarAlertsByYearAndMonthRange {

        @Test
        void when_parameters_are_valid_expect_alerts() {
            final Integer year = 2026;
            final Integer startMonth = 1;
            final Integer endMonth = 6;
            final List<CalendarAlert> expectedAlerts = Arrays.asList(
                    CalendarAlert.builder().id(1).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 15)).title("Meeting").build(),
                    CalendarAlert.builder().id(2).teacherId(TEACHER_ID).date(LocalDate.of(2026, 5, 10)).title("Exam").build()
            );

            when(calendarAlertRepository.findByTeacherIdAndYearAndMonthRange(TEACHER_ID, year, startMonth, endMonth)).thenReturn(expectedAlerts);

            final List<CalendarAlert> result = calendarAlertService.getCalendarAlertsByYearAndMonthRange(year, startMonth, endMonth);

            assertThat(result).isNotNull().hasSize(2);
            verify(calendarAlertRepository).findByTeacherIdAndYearAndMonthRange(TEACHER_ID, year, startMonth, endMonth);
        }

        @Test
        void when_start_month_equals_end_month_expect_alerts() {
            final Integer year = 2026;
            final Integer month = 3;
            final List<CalendarAlert> expectedAlerts = Arrays.asList(
                    CalendarAlert.builder().id(1).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 15)).title("Meeting").build()
            );

            when(calendarAlertRepository.findByTeacherIdAndYearAndMonthRange(TEACHER_ID, year, month, month)).thenReturn(expectedAlerts);

            final List<CalendarAlert> result = calendarAlertService.getCalendarAlertsByYearAndMonthRange(year, month, month);

            assertThat(result).isNotNull().hasSize(1);
            verify(calendarAlertRepository).findByTeacherIdAndYearAndMonthRange(TEACHER_ID, year, month, month);
        }

        @Test
        void when_no_alerts_are_found_expect_empty_list() {
            final Integer year = 2099;
            final Integer startMonth = 1;
            final Integer endMonth = 12;

            when(calendarAlertRepository.findByTeacherIdAndYearAndMonthRange(TEACHER_ID, year, startMonth, endMonth)).thenReturn(Collections.emptyList());

            final List<CalendarAlert> result = calendarAlertService.getCalendarAlertsByYearAndMonthRange(year, startMonth, endMonth);

            assertThat(result).isNotNull().isEmpty();
            verify(calendarAlertRepository).findByTeacherIdAndYearAndMonthRange(TEACHER_ID, year, startMonth, endMonth);
        }

        @Test
        void when_end_month_is_before_start_month_expect_validation_exception() {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

            final ThrowingCallable call = () -> calendarAlertService.getCalendarAlertsByYearAndMonthRange(2026, 6, 3);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(CalendarAlertValidationException.class);
            final CalendarAlertValidationException exception = (CalendarAlertValidationException) thrown;
            assertThat(exception.getErrors()).isNotEmpty();
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("endMonth");
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonthRange(any(), any(), any(), any());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 13, 14, 100})
        void when_start_month_is_invalid_expect_validation_exception(int invalidStartMonth) {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.getCalendarAlertsByYearAndMonthRange(2026, invalidStartMonth, 6);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonthRange(any(), any(), any(), any());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 13, 14, 100})
        void when_end_month_is_invalid_expect_validation_exception(int invalidEndMonth) {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.getCalendarAlertsByYearAndMonthRange(2026, 1, invalidEndMonth);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonthRange(any(), any(), any(), any());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        void when_year_is_invalid_expect_validation_exception(int invalidYear) {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.getCalendarAlertsByYearAndMonthRange(invalidYear, 1, 6);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonthRange(any(), any(), any(), any());
        }

        @Test
        void when_year_is_null_expect_validation_exception() {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.getCalendarAlertsByYearAndMonthRange(null, 1, 6);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonthRange(any(), any(), any(), any());
        }

        @Test
        void when_start_month_is_null_expect_validation_exception() {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.getCalendarAlertsByYearAndMonthRange(2026, null, 6);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonthRange(any(), any(), any(), any());
        }

        @Test
        void when_end_month_is_null_expect_validation_exception() {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");
            final ThrowingCallable callable = () -> calendarAlertService.getCalendarAlertsByYearAndMonthRange(2026, 1, null);

            assertThatThrownBy(callable).isInstanceOf(CalendarAlertValidationException.class);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonthRange(any(), any(), any(), any());
        }

        @Test
        void when_all_parameters_are_invalid_expect_validation_exception() {
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

            final ThrowingCallable call = () -> calendarAlertService.getCalendarAlertsByYearAndMonthRange(-1, 0, 13);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(CalendarAlertValidationException.class);
            final CalendarAlertValidationException exception = (CalendarAlertValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(3);
            verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonthRange(any(), any(), any(), any());
        }
    }
}

