package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.model.CalendarAlertDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CalendarAlertDTOMapperTest {

    private final CalendarAlertDTOMapper mapper = new CalendarAlertDTOMapperImpl();

    @Test
    void toDTO_shouldMapAllFields_whenAllFieldsArePresent() {
        // Given
        CalendarAlert alert = CalendarAlert.builder()
                .id(1)
                .teacherId(10)
                .date(LocalDate.of(2026, 3, 15))
                .title("Parent meeting")
                .description("Quarterly meeting")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .build();

        // When
        CalendarAlertDTO result = mapper.toDTO(alert);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("15/03/2026", result.getDate());
        assertEquals("Parent meeting", result.getTitle());
        assertEquals("Quarterly meeting", result.getDescription());
        assertEquals("09:00", result.getStartTime());
        assertEquals("10:30", result.getEndTime());
    }

    @Test
    void toDTO_shouldFormatDateCorrectly_withSingleDigitDayAndMonth() {
        // Given
        CalendarAlert alert = CalendarAlert.builder()
                .id(1)
                .teacherId(10)
                .date(LocalDate.of(2026, 1, 5))
                .title("Meeting")
                .build();

        // When
        CalendarAlertDTO result = mapper.toDTO(alert);

        // Then
        assertEquals("05/01/2026", result.getDate());
    }

    @Test
    void toDTO_shouldReturnNullDate_whenDateIsNull() {
        // Given
        CalendarAlert alert = CalendarAlert.builder()
                .id(1)
                .teacherId(10)
                .date(null)
                .title("Meeting")
                .build();

        // When
        CalendarAlertDTO result = mapper.toDTO(alert);

        // Then
        assertNotNull(result);
        assertNull(result.getDate());
    }

    @Test
    void toDTO_shouldReturnNullStartTime_whenStartTimeIsNull() {
        // Given
        CalendarAlert alert = CalendarAlert.builder()
                .id(1)
                .teacherId(10)
                .date(LocalDate.of(2026, 3, 15))
                .title("Meeting")
                .startTime(null)
                .build();

        // When
        CalendarAlertDTO result = mapper.toDTO(alert);

        // Then
        assertNotNull(result);
        assertNull(result.getStartTime());
    }

    @Test
    void toDTO_shouldReturnNullEndTime_whenEndTimeIsNull() {
        // Given
        CalendarAlert alert = CalendarAlert.builder()
                .id(1)
                .teacherId(10)
                .date(LocalDate.of(2026, 3, 15))
                .title("Meeting")
                .endTime(null)
                .build();

        // When
        CalendarAlertDTO result = mapper.toDTO(alert);

        // Then
        assertNotNull(result);
        assertNull(result.getEndTime());
    }

    @Test
    void toDTO_shouldReturnNullDescription_whenDescriptionIsNull() {
        // Given
        CalendarAlert alert = CalendarAlert.builder()
                .id(1)
                .teacherId(10)
                .date(LocalDate.of(2026, 3, 15))
                .title("Meeting")
                .description(null)
                .build();

        // When
        CalendarAlertDTO result = mapper.toDTO(alert);

        // Then
        assertNotNull(result);
        assertNull(result.getDescription());
    }

    @Test
    void toDTO_shouldFormatMidnightTime_whenStartTimeIsMidnight() {
        // Given
        CalendarAlert alert = CalendarAlert.builder()
                .id(1)
                .teacherId(10)
                .date(LocalDate.of(2026, 3, 15))
                .title("Meeting")
                .startTime(LocalTime.of(0, 0))
                .build();

        // When
        CalendarAlertDTO result = mapper.toDTO(alert);

        // Then
        assertEquals("00:00", result.getStartTime());
    }

    @Test
    void toDTO_shouldFormatEndOfDayTime_whenEndTimeIsEndOfDay() {
        // Given
        CalendarAlert alert = CalendarAlert.builder()
                .id(1)
                .teacherId(10)
                .date(LocalDate.of(2026, 3, 15))
                .title("Meeting")
                .startTime(LocalTime.of(22, 0))
                .endTime(LocalTime.of(23, 59))
                .build();

        // When
        CalendarAlertDTO result = mapper.toDTO(alert);

        // Then
        assertEquals("22:00", result.getStartTime());
        assertEquals("23:59", result.getEndTime());
    }

    @Test
    void toDTOList_shouldMapListCorrectly() {
        // Given
        CalendarAlert alert1 = CalendarAlert.builder()
                .id(1)
                .teacherId(10)
                .date(LocalDate.of(2026, 3, 15))
                .title("Meeting A")
                .startTime(LocalTime.of(9, 0))
                .build();

        CalendarAlert alert2 = CalendarAlert.builder()
                .id(2)
                .teacherId(10)
                .date(LocalDate.of(2026, 4, 20))
                .title("Meeting B")
                .startTime(LocalTime.of(14, 30))
                .endTime(LocalTime.of(15, 0))
                .build();

        List<CalendarAlert> alerts = Arrays.asList(alert1, alert2);

        // When
        List<CalendarAlertDTO> result = mapper.toDTOList(alerts);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        CalendarAlertDTO dto1 = result.get(0);
        assertEquals(1, dto1.getId());
        assertEquals("15/03/2026", dto1.getDate());
        assertEquals("Meeting A", dto1.getTitle());
        assertEquals("09:00", dto1.getStartTime());

        CalendarAlertDTO dto2 = result.get(1);
        assertEquals(2, dto2.getId());
        assertEquals("20/04/2026", dto2.getDate());
        assertEquals("Meeting B", dto2.getTitle());
        assertEquals("14:30", dto2.getStartTime());
        assertEquals("15:00", dto2.getEndTime());
    }

    @Test
    void toDTOList_shouldReturnEmptyList_whenInputIsEmpty() {
        // Given
        List<CalendarAlert> alerts = Arrays.asList();

        // When
        List<CalendarAlertDTO> result = mapper.toDTOList(alerts);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

