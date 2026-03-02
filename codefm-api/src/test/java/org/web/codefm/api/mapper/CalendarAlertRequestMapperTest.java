package org.web.codefm.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.domain.exception.teachernotebook.CalendarAlertValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.model.CalendarAlertRequestDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
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
    void setUp() {
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.CALENDAR_ALERT_VALIDATION_DATE_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("Alert date is required.");
    }

    @Test
    void toDomain_shouldMapAllFields_whenAllFieldsAreValid() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate("15/03/2026");
        dto.setTitle("Parent meeting");
        dto.setDescription("Quarterly parent meeting");
        dto.setStartTime("09:00");
        dto.setEndTime("10:30");

        // When
        CalendarAlert result = mapper.toDomain(dto);

        // Then
        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 3, 15), result.getDate());
        assertEquals("Parent meeting", result.getTitle());
        assertEquals("Quarterly parent meeting", result.getDescription());
        assertEquals(LocalTime.of(9, 0), result.getStartTime());
        assertEquals(LocalTime.of(10, 30), result.getEndTime());
        assertNull(result.getId());
        assertNull(result.getTeacherId());
    }

    @Test
    void toDomain_shouldMapCorrectly_whenOptionalFieldsAreNull() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate("15/03/2026");
        dto.setTitle("Parent meeting");
        dto.setDescription(null);
        dto.setStartTime(null);
        dto.setEndTime(null);

        // When
        CalendarAlert result = mapper.toDomain(dto);

        // Then
        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 3, 15), result.getDate());
        assertEquals("Parent meeting", result.getTitle());
        assertNull(result.getDescription());
        assertNull(result.getStartTime());
        assertNull(result.getEndTime());
    }

    @Test
    void toDomain_shouldReturnNullDate_whenDateIsNull() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate(null);
        dto.setTitle("Parent meeting");

        // When
        CalendarAlert result = mapper.toDomain(dto);

        // Then
        assertNotNull(result);
        assertNull(result.getDate());
    }

    @Test
    void toDomain_shouldReturnNullDate_whenDateIsEmpty() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate("");
        dto.setTitle("Parent meeting");

        // When
        CalendarAlert result = mapper.toDomain(dto);

        // Then
        assertNotNull(result);
        assertNull(result.getDate());
    }

    @Test
    void toDomain_shouldThrowValidationException_whenDateFormatIsInvalid() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate("2026-03-15");
        dto.setTitle("Parent meeting");

        // When & Then
        CalendarAlertValidationException exception = assertThrows(CalendarAlertValidationException.class, () ->
                mapper.toDomain(dto));

        assertNotNull(exception);
        assertFalse(exception.getErrors().isEmpty());
        ErrorMessage error = exception.getErrors().get(0);
        assertEquals("date", error.getParam());
    }

    @Test
    void toDomain_shouldThrowValidationException_whenDateHasInvalidDay() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate("32/03/2026");
        dto.setTitle("Parent meeting");

        // When & Then
        CalendarAlertValidationException exception = assertThrows(CalendarAlertValidationException.class, () ->
                mapper.toDomain(dto));

        assertNotNull(exception);
        assertFalse(exception.getErrors().isEmpty());
        ErrorMessage error = exception.getErrors().get(0);
        assertEquals("date", error.getParam());
    }

    @Test
    void toDomain_shouldThrowValidationException_whenDateHasInvalidMonth() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate("15/13/2026");
        dto.setTitle("Parent meeting");

        // When & Then
        CalendarAlertValidationException exception = assertThrows(CalendarAlertValidationException.class, () ->
                mapper.toDomain(dto));

        assertNotNull(exception);
        assertFalse(exception.getErrors().isEmpty());
        ErrorMessage error = exception.getErrors().get(0);
        assertEquals("date", error.getParam());
    }

    @Test
    void toDomain_shouldThrowValidationException_whenDateIsInvalidText() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate("not-a-date");
        dto.setTitle("Parent meeting");

        // When & Then
        CalendarAlertValidationException exception = assertThrows(CalendarAlertValidationException.class, () ->
                mapper.toDomain(dto));

        assertNotNull(exception);
        assertFalse(exception.getErrors().isEmpty());
        ErrorMessage error = exception.getErrors().get(0);
        assertEquals("date", error.getParam());
    }

    @Test
    void toDomain_shouldReturnNullStartTime_whenStartTimeIsNull() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate("15/03/2026");
        dto.setTitle("Parent meeting");
        dto.setStartTime(null);

        // When
        CalendarAlert result = mapper.toDomain(dto);

        // Then
        assertNull(result.getStartTime());
    }

    @Test
    void toDomain_shouldReturnNullStartTime_whenStartTimeIsEmpty() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate("15/03/2026");
        dto.setTitle("Parent meeting");
        dto.setStartTime("");

        // When
        CalendarAlert result = mapper.toDomain(dto);

        // Then
        assertNull(result.getStartTime());
    }

    @Test
    void toDomain_shouldMapMidnightTime_whenStartTimeIsMidnight() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate("15/03/2026");
        dto.setTitle("Parent meeting");
        dto.setStartTime("00:00");

        // When
        CalendarAlert result = mapper.toDomain(dto);

        // Then
        assertEquals(LocalTime.of(0, 0), result.getStartTime());
    }

    @Test
    void toDomain_shouldIgnoreIdAndTeacherId() {
        // Given
        CalendarAlertRequestDTO dto = new CalendarAlertRequestDTO();
        dto.setDate("15/03/2026");
        dto.setTitle("Parent meeting");

        // When
        CalendarAlert result = mapper.toDomain(dto);

        // Then
        assertNull(result.getId());
        assertNull(result.getTeacherId());
    }
}

