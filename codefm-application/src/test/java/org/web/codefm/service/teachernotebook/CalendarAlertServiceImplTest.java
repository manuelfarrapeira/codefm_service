package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private CalendarAlertServiceImpl calendarAlertService;

    private static final Integer TEACHER_ID = 1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        calendarAlertService = new CalendarAlertServiceImpl(calendarAlertRepository, messageSource, sessionUser);
        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    }

    @Test
    void getCalendarAlerts_shouldReturnAlerts_whenFound() {
        List<CalendarAlert> expectedAlerts = Arrays.asList(
                CalendarAlert.builder().id(1).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 15)).title("Meeting").build(),
                CalendarAlert.builder().id(2).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 20)).title("Exam").build()
        );

        when(calendarAlertRepository.findByTeacherId(TEACHER_ID)).thenReturn(expectedAlerts);

        List<CalendarAlert> result = calendarAlertService.getCalendarAlerts();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(calendarAlertRepository).findByTeacherId(TEACHER_ID);
    }

    @Test
    void getCalendarAlerts_shouldReturnEmptyList_whenNoAlertsFound() {
        when(calendarAlertRepository.findByTeacherId(TEACHER_ID)).thenReturn(Collections.emptyList());

        List<CalendarAlert> result = calendarAlertService.getCalendarAlerts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(calendarAlertRepository).findByTeacherId(TEACHER_ID);
    }

    @Test
    void createCalendarAlert_shouldCreateAlert_whenDataIsValid() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 3, 15))
                .title("Parent meeting")
                .build();
        CalendarAlert savedAlert = CalendarAlert.builder()
                .id(1)
                .teacherId(TEACHER_ID)
                .date(LocalDate.of(2026, 3, 15))
                .title("Parent meeting")
                .build();

        when(calendarAlertRepository.save(any(CalendarAlert.class))).thenReturn(savedAlert);

        CalendarAlert result = calendarAlertService.createCalendarAlert(alertToCreate);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Parent meeting", result.getTitle());
        assertEquals(TEACHER_ID, result.getTeacherId());
        verify(calendarAlertRepository).save(any(CalendarAlert.class));
    }

    @Test
    void createCalendarAlert_shouldThrowValidationException_whenTitleIsNull() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 3, 15))
                .title(null)
                .build();

        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.createCalendarAlert(alertToCreate));
        verify(calendarAlertRepository, never()).save(any());
    }

    @Test
    void createCalendarAlert_shouldThrowValidationException_whenTitleIsEmpty() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 3, 15))
                .title("")
                .build();

        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.createCalendarAlert(alertToCreate));
        verify(calendarAlertRepository, never()).save(any());
    }

    @Test
    void createCalendarAlert_shouldThrowValidationException_whenTitleExceeds100Characters() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 3, 15))
                .title("a".repeat(101))
                .build();

        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.createCalendarAlert(alertToCreate));
        verify(calendarAlertRepository, never()).save(any());
    }

    @Test
    void createCalendarAlert_shouldThrowValidationException_whenDateIsNull() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(null)
                .title("Valid title")
                .build();

        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.createCalendarAlert(alertToCreate));
        verify(calendarAlertRepository, never()).save(any());
    }

    @Test
    void createCalendarAlert_shouldThrowValidationException_whenTitleAndDateAreNull() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(null)
                .title(null)
                .build();

        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        CalendarAlertValidationException exception = assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.createCalendarAlert(alertToCreate));

        assertEquals(2, exception.getErrors().size());
        verify(calendarAlertRepository, never()).save(any());
    }

    @Test
    void updateCalendarAlert_shouldUpdateAlert_whenDataIsValid() {
        Integer alertId = 1;
        CalendarAlert alertToUpdate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 4, 10))
                .title("Updated title")
                .description("Updated description")
                .build();
        CalendarAlert existingAlert = CalendarAlert.builder()
                .id(alertId)
                .teacherId(TEACHER_ID)
                .date(LocalDate.of(2026, 3, 15))
                .title("Original title")
                .build();
        CalendarAlert savedAlert = CalendarAlert.builder()
                .id(alertId)
                .teacherId(TEACHER_ID)
                .date(LocalDate.of(2026, 4, 10))
                .title("Updated title")
                .description("Updated description")
                .build();

        when(calendarAlertRepository.findByIdAndTeacherId(alertId, TEACHER_ID)).thenReturn(Optional.of(existingAlert));
        when(calendarAlertRepository.save(any(CalendarAlert.class))).thenReturn(savedAlert);

        CalendarAlert result = calendarAlertService.updateCalendarAlert(alertId, alertToUpdate);

        assertNotNull(result);
        assertEquals("Updated title", result.getTitle());
        assertEquals(LocalDate.of(2026, 4, 10), result.getDate());
        verify(calendarAlertRepository).findByIdAndTeacherId(alertId, TEACHER_ID);
        verify(calendarAlertRepository).save(any(CalendarAlert.class));
    }

    @Test
    void updateCalendarAlert_shouldThrowNotFoundException_whenAlertDoesNotExist() {
        Integer alertId = 999;
        CalendarAlert alertToUpdate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 4, 10))
                .title("Updated title")
                .build();

        when(calendarAlertRepository.findByIdAndTeacherId(alertId, TEACHER_ID)).thenReturn(Optional.empty());
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Not found");

        assertThrows(CalendarAlertNotFoundException.class,
                () -> calendarAlertService.updateCalendarAlert(alertId, alertToUpdate));
        verify(calendarAlertRepository, never()).save(any());
    }

    @Test
    void updateCalendarAlert_shouldThrowValidationException_whenTitleIsInvalid() {
        Integer alertId = 1;
        CalendarAlert alertToUpdate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 4, 10))
                .title("")
                .build();

        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.updateCalendarAlert(alertId, alertToUpdate));
        verify(calendarAlertRepository, never()).findByIdAndTeacherId(any(), any());
    }

    @Test
    void deleteCalendarAlert_shouldDeleteAlert_whenAlertExists() {
        Integer alertId = 1;
        CalendarAlert existingAlert = CalendarAlert.builder()
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
    void deleteCalendarAlert_shouldThrowNotFoundException_whenAlertDoesNotExist() {
        Integer alertId = 999;

        when(calendarAlertRepository.findByIdAndTeacherId(alertId, TEACHER_ID)).thenReturn(Optional.empty());
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Not found");

        assertThrows(CalendarAlertNotFoundException.class,
                () -> calendarAlertService.deleteCalendarAlert(alertId));
        verify(calendarAlertRepository, never()).deleteById(any());
    }

    @Test
    void createCalendarAlert_shouldThrowValidationException_whenEndTimeWithoutStartTime() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 3, 15))
                .title("Valid title")
                .endTime(LocalTime.of(10, 0))
                .build();

        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.createCalendarAlert(alertToCreate));
        verify(calendarAlertRepository, never()).save(any());
    }

    @Test
    void createCalendarAlert_shouldThrowValidationException_whenEndTimeBeforeStartTime() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 3, 15))
                .title("Valid title")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .build();

        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.createCalendarAlert(alertToCreate));
        verify(calendarAlertRepository, never()).save(any());
    }

    @Test
    void createCalendarAlert_shouldThrowValidationException_whenEndTimeEqualsStartTime() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 3, 15))
                .title("Valid title")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.createCalendarAlert(alertToCreate));
        verify(calendarAlertRepository, never()).save(any());
    }

    @Test
    void createCalendarAlert_shouldCreateAlert_whenStartTimeAndEndTimeAreValid() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 3, 15))
                .title("Parent meeting")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();
        CalendarAlert savedAlert = CalendarAlert.builder()
                .id(1)
                .teacherId(TEACHER_ID)
                .date(LocalDate.of(2026, 3, 15))
                .title("Parent meeting")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        when(calendarAlertRepository.save(any(CalendarAlert.class))).thenReturn(savedAlert);

        CalendarAlert result = calendarAlertService.createCalendarAlert(alertToCreate);

        assertNotNull(result);
        assertEquals(LocalTime.of(9, 0), result.getStartTime());
        assertEquals(LocalTime.of(10, 0), result.getEndTime());
        verify(calendarAlertRepository).save(any(CalendarAlert.class));
    }

    @Test
    void createCalendarAlert_shouldCreateAlert_whenOnlyStartTimeProvided() {
        CalendarAlert alertToCreate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 3, 15))
                .title("Parent meeting")
                .startTime(LocalTime.of(9, 0))
                .build();
        CalendarAlert savedAlert = CalendarAlert.builder()
                .id(1)
                .teacherId(TEACHER_ID)
                .date(LocalDate.of(2026, 3, 15))
                .title("Parent meeting")
                .startTime(LocalTime.of(9, 0))
                .build();

        when(calendarAlertRepository.save(any(CalendarAlert.class))).thenReturn(savedAlert);

        CalendarAlert result = calendarAlertService.createCalendarAlert(alertToCreate);

        assertNotNull(result);
        assertEquals(LocalTime.of(9, 0), result.getStartTime());
        assertNull(result.getEndTime());
        verify(calendarAlertRepository).save(any(CalendarAlert.class));
    }

    @Test
    void updateCalendarAlert_shouldUpdateStartTimeAndEndTime() {
        Integer alertId = 1;
        CalendarAlert alertToUpdate = CalendarAlert.builder()
                .date(LocalDate.of(2026, 4, 10))
                .title("Updated title")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 30))
                .build();
        CalendarAlert existingAlert = CalendarAlert.builder()
                .id(alertId)
                .teacherId(TEACHER_ID)
                .date(LocalDate.of(2026, 3, 15))
                .title("Original title")
                .build();
        CalendarAlert savedAlert = CalendarAlert.builder()
                .id(alertId)
                .teacherId(TEACHER_ID)
                .date(LocalDate.of(2026, 4, 10))
                .title("Updated title")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 30))
                .build();

        when(calendarAlertRepository.findByIdAndTeacherId(alertId, TEACHER_ID)).thenReturn(Optional.of(existingAlert));
        when(calendarAlertRepository.save(any(CalendarAlert.class))).thenReturn(savedAlert);

        CalendarAlert result = calendarAlertService.updateCalendarAlert(alertId, alertToUpdate);

        assertNotNull(result);
        assertEquals(LocalTime.of(14, 0), result.getStartTime());
        assertEquals(LocalTime.of(15, 30), result.getEndTime());
    }

    @Test
    void getCalendarAlertsByYearAndMonth_shouldReturnAlerts_whenYearAndMonthAreValid() {
        Integer year = 2026;
        Integer month = 3;
        List<CalendarAlert> expectedAlerts = Arrays.asList(
                CalendarAlert.builder().id(1).teacherId(TEACHER_ID).date(LocalDate.of(2026, 3, 15)).title("Meeting").build()
        );

        when(calendarAlertRepository.findByTeacherIdAndYearAndMonth(TEACHER_ID, year, month)).thenReturn(expectedAlerts);

        List<CalendarAlert> result = calendarAlertService.getCalendarAlertsByYearAndMonth(year, month);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(calendarAlertRepository).findByTeacherIdAndYearAndMonth(TEACHER_ID, year, month);
    }

    @Test
    void getCalendarAlertsByYearAndMonth_shouldReturnEmptyList_whenNoAlertsFound() {
        Integer year = 2026;
        Integer month = 6;

        when(calendarAlertRepository.findByTeacherIdAndYearAndMonth(TEACHER_ID, year, month)).thenReturn(Collections.emptyList());

        List<CalendarAlert> result = calendarAlertService.getCalendarAlertsByYearAndMonth(year, month);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(calendarAlertRepository).findByTeacherIdAndYearAndMonth(TEACHER_ID, year, month);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 13, 14, 100})
    void getCalendarAlertsByYearAndMonth_shouldThrowValidationException_whenMonthIsInvalid(int invalidMonth) {
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.getCalendarAlertsByYearAndMonth(2026, invalidMonth));
        verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonth(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void getCalendarAlertsByYearAndMonth_shouldThrowValidationException_whenYearIsInvalid(int invalidYear) {
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.getCalendarAlertsByYearAndMonth(invalidYear, 3));
        verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonth(any(), any(), any());
    }

    @Test
    void getCalendarAlertsByYearAndMonth_shouldThrowValidationException_whenBothYearAndMonthAreInvalid() {
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        CalendarAlertValidationException exception = assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.getCalendarAlertsByYearAndMonth(-1, 13));

        assertEquals(2, exception.getErrors().size());
        verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonth(any(), any(), any());
    }

    @Test
    void getCalendarAlertsByYearAndMonth_shouldThrowValidationException_whenYearIsNull() {
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.getCalendarAlertsByYearAndMonth(null, 3));
        verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonth(any(), any(), any());
    }

    @Test
    void getCalendarAlertsByYearAndMonth_shouldThrowValidationException_whenMonthIsNull() {
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(CalendarAlertValidationException.class,
                () -> calendarAlertService.getCalendarAlertsByYearAndMonth(2026, null));
        verify(calendarAlertRepository, never()).findByTeacherIdAndYearAndMonth(any(), any(), any());
    }
}

