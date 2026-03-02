package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.domain.service.teachernotebook.CalendarAlertService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarAlertUseCaseImplTest {

    @Mock
    private CalendarAlertService calendarAlertService;

    @InjectMocks
    private CalendarAlertUseCaseImpl calendarAlertUseCase;

    private static final Integer TEACHER_ID = 1;

    @Test
    void getCalendarAlerts_shouldReturnAlerts_whenFound() {
        List<CalendarAlert> expectedAlerts = Arrays.asList(
                CalendarAlert.builder().id(1).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 15)).title("Meeting").build(),
                CalendarAlert.builder().id(2).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 20)).title("Exam").build()
        );

        when(calendarAlertService.getCalendarAlerts()).thenReturn(expectedAlerts);

        List<CalendarAlert> result = calendarAlertUseCase.getCalendarAlerts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Meeting", result.get(0).getTitle());
        verify(calendarAlertService).getCalendarAlerts();
    }

    @Test
    void getCalendarAlerts_shouldReturnEmptyList_whenNoAlertsFound() {
        when(calendarAlertService.getCalendarAlerts()).thenReturn(Collections.emptyList());

        List<CalendarAlert> result = calendarAlertUseCase.getCalendarAlerts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(calendarAlertService).getCalendarAlerts();
    }

    @Test
    void createCalendarAlert_shouldCallServiceWithAlert() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 3, 15))
                .title("New alert")
                .build();
        CalendarAlert createdAlert = CalendarAlert.builder()
                .id(1)
                .teacherId(TEACHER_ID)
                .date(LocalDate.of(2026, 3, 15))
                .title("New alert")
                .build();

        when(calendarAlertService.createCalendarAlert(any(CalendarAlert.class))).thenReturn(createdAlert);

        CalendarAlert result = calendarAlertUseCase.createCalendarAlert(alertToCreate);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("New alert", result.getTitle());
        verify(calendarAlertService).createCalendarAlert(alertToCreate);
    }

    @Test
    void updateCalendarAlert_shouldCallServiceWithIdAndAlert() {
        Integer alertId = 1;
        CalendarAlert alertToUpdate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 4, 10))
                .title("Updated title")
                .build();
        CalendarAlert updatedAlert = CalendarAlert.builder()
                .id(alertId)
                .teacherId(TEACHER_ID)
                .date(LocalDate.of(2026, 4, 10))
                .title("Updated title")
                .build();

        when(calendarAlertService.updateCalendarAlert(eq(alertId), any(CalendarAlert.class))).thenReturn(updatedAlert);

        CalendarAlert result = calendarAlertUseCase.updateCalendarAlert(alertId, alertToUpdate);

        assertNotNull(result);
        assertEquals(alertId, result.getId());
        assertEquals("Updated title", result.getTitle());
        verify(calendarAlertService).updateCalendarAlert(eq(alertId), any(CalendarAlert.class));
    }

    @Test
    void deleteCalendarAlert_shouldCallServiceWithId() {
        Integer alertId = 1;

        doNothing().when(calendarAlertService).deleteCalendarAlert(alertId);

        calendarAlertUseCase.deleteCalendarAlert(alertId);

        verify(calendarAlertService).deleteCalendarAlert(alertId);
    }
}

