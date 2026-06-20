package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.domain.service.teachernotebook.CalendarAlertService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarAlertUseCaseImplTest {

    private CalendarAlertUseCaseImpl calendarAlertUseCase;

    @Mock
    private CalendarAlertService calendarAlertService;

    private static final Integer TEACHER_ID = 1;

    @BeforeEach
    void beforeEach() {
        calendarAlertUseCase = new CalendarAlertUseCaseImpl(calendarAlertService);
    }

    @Nested
    class GetCalendarAlerts {

        @Test
        void when_alerts_found_expect_alerts_returned() {
            final List<CalendarAlert> expectedAlerts = Arrays.asList(
                    CalendarAlert.builder().id(1).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 15)).title("Meeting").build(),
                    CalendarAlert.builder().id(2).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 20)).title("Exam").build()
            );
            when(calendarAlertService.getCalendarAlerts()).thenReturn(expectedAlerts);

            final List<CalendarAlert> result = calendarAlertUseCase.getCalendarAlerts();

            assertThat(result).isNotNull().hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Meeting");
            verify(calendarAlertService).getCalendarAlerts();
        }

        @Test
        void when_no_alerts_found_expect_empty_list() {
            when(calendarAlertService.getCalendarAlerts()).thenReturn(Collections.emptyList());

            final List<CalendarAlert> result = calendarAlertUseCase.getCalendarAlerts();

            assertThat(result).isNotNull().isEmpty();
            verify(calendarAlertService).getCalendarAlerts();
        }
    }

    @Nested
    class CreateCalendarAlert {

        @Test
        void when_creating_alert_expect_service_called() {
            final CalendarAlert alertToCreate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 3, 15)).title("New alert").build();
            final CalendarAlert createdAlert = CalendarAlert.builder()
                    .id(1).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 15)).title("New alert").build();

            when(calendarAlertService.createCalendarAlert(any(CalendarAlert.class))).thenReturn(createdAlert);

            final CalendarAlert result = calendarAlertUseCase.createCalendarAlert(alertToCreate);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getTitle()).isEqualTo("New alert");
            verify(calendarAlertService).createCalendarAlert(alertToCreate);
        }
    }

    @Nested
    class UpdateCalendarAlert {

        @Test
        void when_updating_alert_expect_service_called_with_id() {
            final Integer alertId = 1;
            final CalendarAlert alertToUpdate = CalendarAlert.builder()
                    .date(LocalDate.of(2026, 4, 10)).title("Updated title").build();
            final CalendarAlert updatedAlert = CalendarAlert.builder()
                    .id(alertId).teacherId(TEACHER_ID).date(LocalDate.of(2026, 4, 10)).title("Updated title").build();

            when(calendarAlertService.updateCalendarAlert(eq(alertId), any(CalendarAlert.class))).thenReturn(updatedAlert);

            final CalendarAlert result = calendarAlertUseCase.updateCalendarAlert(alertId, alertToUpdate);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(alertId);
            assertThat(result.getTitle()).isEqualTo("Updated title");
            verify(calendarAlertService).updateCalendarAlert(eq(alertId), any(CalendarAlert.class));
        }
    }

    @Nested
    class DeleteCalendarAlert {

        @Test
        void when_deleting_alert_expect_service_called_with_id() {
            final Integer alertId = 1;
            doNothing().when(calendarAlertService).deleteCalendarAlert(alertId);

            calendarAlertUseCase.deleteCalendarAlert(alertId);

            verify(calendarAlertService).deleteCalendarAlert(alertId);
        }
    }

    @Nested
    class GetCalendarAlertsByYearAndMonth {

        @Test
        void when_filtering_by_year_and_month_expect_delegated_to_service() {
            final Integer year = 2026;
            final Integer month = 3;
            final List<CalendarAlert> expectedAlerts = Arrays.asList(
                    CalendarAlert.builder().id(1).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 15)).title("Meeting").build()
            );
            when(calendarAlertService.getCalendarAlertsByYearAndMonth(year, month)).thenReturn(expectedAlerts);

            final List<CalendarAlert> result = calendarAlertUseCase.getCalendarAlertsByYearAndMonth(year, month);

            assertThat(result).isNotNull().hasSize(1);
            verify(calendarAlertService).getCalendarAlertsByYearAndMonth(year, month);
        }

        @Test
        void when_no_alerts_for_year_month_expect_empty_list() {
            final Integer year = 2026;
            final Integer month = 6;
            when(calendarAlertService.getCalendarAlertsByYearAndMonth(year, month)).thenReturn(Collections.emptyList());

            final List<CalendarAlert> result = calendarAlertUseCase.getCalendarAlertsByYearAndMonth(year, month);

            assertThat(result).isNotNull().isEmpty();
            verify(calendarAlertService).getCalendarAlertsByYearAndMonth(year, month);
        }
    }

    @Nested
    class GetCalendarAlertsByYearAndMonthRange {

        @Test
        void when_filtering_by_year_month_range_expect_delegated_to_service() {
            final Integer year = 2026;
            final Integer startMonth = 1;
            final Integer endMonth = 6;
            final List<CalendarAlert> expectedAlerts = Arrays.asList(
                    CalendarAlert.builder().id(1).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 15)).title("Meeting").build(),
                    CalendarAlert.builder().id(2).teacherId(TEACHER_ID).date(LocalDate.of(2026, 5, 10)).title("Exam").build()
            );
            when(calendarAlertService.getCalendarAlertsByYearAndMonthRange(year, startMonth, endMonth)).thenReturn(expectedAlerts);

            final List<CalendarAlert> result = calendarAlertUseCase.getCalendarAlertsByYearAndMonthRange(year, startMonth, endMonth);

            assertThat(result).isNotNull().hasSize(2);
            verify(calendarAlertService).getCalendarAlertsByYearAndMonthRange(year, startMonth, endMonth);
        }

        @Test
        void when_no_alerts_for_year_month_range_expect_empty_list() {
            final Integer year = 2099;
            final Integer startMonth = 1;
            final Integer endMonth = 12;
            when(calendarAlertService.getCalendarAlertsByYearAndMonthRange(year, startMonth, endMonth)).thenReturn(Collections.emptyList());

            final List<CalendarAlert> result = calendarAlertUseCase.getCalendarAlertsByYearAndMonthRange(year, startMonth, endMonth);

            assertThat(result).isNotNull().isEmpty();
            verify(calendarAlertService).getCalendarAlertsByYearAndMonthRange(year, startMonth, endMonth);
        }
    }
}

