package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.CalendarAlertEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.CalendarAlertJPARepository;
import org.web.codefm.infrastructure.mapper.CalendarAlertMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarAlertRepositoryImplTest {

    @Mock
    private CalendarAlertJPARepository calendarAlertJPARepository;

    @Mock
    private CalendarAlertMapper calendarAlertMapper;

    @InjectMocks
    private CalendarAlertRepositoryImpl calendarAlertRepository;

    @Test
    void findByTeacherId_shouldReturnAlerts_whenTeacherHasAlerts() {
        Integer teacherId = 1;
        CalendarAlertEntity entity1 = new CalendarAlertEntity(1, teacherId, LocalDate.of(2026, 3, 15), "Meeting", null, null, null);
        CalendarAlertEntity entity2 = new CalendarAlertEntity(2, teacherId, LocalDate.of(2026, 3, 20), "Exam", null, null, null);
        List<CalendarAlertEntity> entities = Arrays.asList(entity1, entity2);

        CalendarAlert alert1 = CalendarAlert.builder().id(1).teacherId(teacherId).date(LocalDate.of(2026, 3, 15)).title("Meeting").build();
        CalendarAlert alert2 = CalendarAlert.builder().id(2).teacherId(teacherId).date(LocalDate.of(2026, 3, 20)).title("Exam").build();
        List<CalendarAlert> expectedAlerts = Arrays.asList(alert1, alert2);

        when(calendarAlertJPARepository.findByTeacherIdOrderByDateAsc(teacherId)).thenReturn(entities);
        when(calendarAlertMapper.toModelList(entities)).thenReturn(expectedAlerts);

        List<CalendarAlert> result = calendarAlertRepository.findByTeacherId(teacherId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(calendarAlertJPARepository).findByTeacherIdOrderByDateAsc(teacherId);
        verify(calendarAlertMapper).toModelList(entities);
    }

    @Test
    void findByTeacherId_shouldReturnEmptyList_whenTeacherHasNoAlerts() {
        Integer teacherId = 1;

        when(calendarAlertJPARepository.findByTeacherIdOrderByDateAsc(teacherId)).thenReturn(Collections.emptyList());
        when(calendarAlertMapper.toModelList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<CalendarAlert> result = calendarAlertRepository.findByTeacherId(teacherId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(calendarAlertJPARepository).findByTeacherIdOrderByDateAsc(teacherId);
    }

    @Test
    void findByIdAndTeacherId_shouldReturnAlert_whenFound() {
        Integer id = 1;
        Integer teacherId = 1;
        CalendarAlertEntity entity = new CalendarAlertEntity(id, teacherId, LocalDate.of(2026, 3, 15), "Meeting", null, null, null);
        CalendarAlert alert = CalendarAlert.builder().id(id).teacherId(teacherId).date(LocalDate.of(2026, 3, 15)).title("Meeting").build();

        when(calendarAlertJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.of(entity));
        when(calendarAlertMapper.toModel(entity)).thenReturn(alert);

        Optional<CalendarAlert> result = calendarAlertRepository.findByIdAndTeacherId(id, teacherId);

        assertTrue(result.isPresent());
        assertEquals("Meeting", result.get().getTitle());
        verify(calendarAlertJPARepository).findByIdAndTeacherId(id, teacherId);
    }

    @Test
    void findByIdAndTeacherId_shouldReturnEmpty_whenNotFound() {
        Integer id = 999;
        Integer teacherId = 1;

        when(calendarAlertJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.empty());

        Optional<CalendarAlert> result = calendarAlertRepository.findByIdAndTeacherId(id, teacherId);

        assertTrue(result.isEmpty());
        verify(calendarAlertJPARepository).findByIdAndTeacherId(id, teacherId);
    }

    @Test
    void save_shouldMapToEntityAndSaveAndMapBackToModel() {
        CalendarAlert alertToSave = CalendarAlert.builder().teacherId(1).date(LocalDate.of(2026, 3, 15)).title("New alert").build();
        CalendarAlertEntity entity = new CalendarAlertEntity();
        CalendarAlertEntity savedEntity = new CalendarAlertEntity(1, 1, LocalDate.of(2026, 3, 15), "New alert", null, null, null);
        CalendarAlert savedAlert = CalendarAlert.builder().id(1).teacherId(1).date(LocalDate.of(2026, 3, 15)).title("New alert").build();

        when(calendarAlertMapper.toEntity(alertToSave)).thenReturn(entity);
        when(calendarAlertJPARepository.save(entity)).thenReturn(savedEntity);
        when(calendarAlertMapper.toModel(savedEntity)).thenReturn(savedAlert);

        CalendarAlert result = calendarAlertRepository.save(alertToSave);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("New alert", result.getTitle());
        verify(calendarAlertMapper).toEntity(alertToSave);
        verify(calendarAlertJPARepository).save(entity);
        verify(calendarAlertMapper).toModel(savedEntity);
    }

    @Test
    void deleteById_shouldCallJpaRepositoryDeleteById() {
        Integer id = 1;

        doNothing().when(calendarAlertJPARepository).deleteById(id);

        calendarAlertRepository.deleteById(id);

        verify(calendarAlertJPARepository).deleteById(id);
    }

    @Test
    void findByTeacherIdAndYearAndMonth_shouldReturnAlerts_whenFound() {
        Integer teacherId = 1;
        Integer year = 2026;
        Integer month = 3;
        CalendarAlertEntity entity1 = new CalendarAlertEntity(1, teacherId, LocalDate.of(2026, 3, 15), "Meeting", null, null, null);
        List<CalendarAlertEntity> entities = List.of(entity1);

        CalendarAlert alert1 = CalendarAlert.builder().id(1).teacherId(teacherId).date(LocalDate.of(2026, 3, 15)).title("Meeting").build();
        List<CalendarAlert> expectedAlerts = List.of(alert1);

        when(calendarAlertJPARepository.findByTeacherIdAndYearAndMonth(teacherId, year, month)).thenReturn(entities);
        when(calendarAlertMapper.toModelList(entities)).thenReturn(expectedAlerts);

        List<CalendarAlert> result = calendarAlertRepository.findByTeacherIdAndYearAndMonth(teacherId, year, month);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(calendarAlertJPARepository).findByTeacherIdAndYearAndMonth(teacherId, year, month);
        verify(calendarAlertMapper).toModelList(entities);
    }

    @Test
    void findByTeacherIdAndYearAndMonth_shouldReturnEmptyList_whenNoAlertsFound() {
        Integer teacherId = 1;
        Integer year = 2026;
        Integer month = 6;

        when(calendarAlertJPARepository.findByTeacherIdAndYearAndMonth(teacherId, year, month)).thenReturn(Collections.emptyList());
        when(calendarAlertMapper.toModelList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<CalendarAlert> result = calendarAlertRepository.findByTeacherIdAndYearAndMonth(teacherId, year, month);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(calendarAlertJPARepository).findByTeacherIdAndYearAndMonth(teacherId, year, month);
    }

    @Test
    void findByTeacherIdAndYearAndMonthRange_shouldReturnAlerts_whenFound() {
        final Integer teacherId = 1;
        final Integer year = 2026;
        final Integer startMonth = 1;
        final Integer endMonth = 6;
        final CalendarAlertEntity entity1 = new CalendarAlertEntity(1, teacherId, LocalDate.of(2026, 3, 15), "Meeting", null, null, null);
        final CalendarAlertEntity entity2 = new CalendarAlertEntity(2, teacherId, LocalDate.of(2026, 5, 10), "Exam", null, null, null);
        final List<CalendarAlertEntity> entities = Arrays.asList(entity1, entity2);

        final CalendarAlert alert1 = CalendarAlert.builder().id(1).teacherId(teacherId).date(LocalDate.of(2026, 3, 15)).title("Meeting").build();
        final CalendarAlert alert2 = CalendarAlert.builder().id(2).teacherId(teacherId).date(LocalDate.of(2026, 5, 10)).title("Exam").build();
        final List<CalendarAlert> expectedAlerts = Arrays.asList(alert1, alert2);

        when(calendarAlertJPARepository.findByTeacherIdAndYearAndMonthRange(teacherId, year, startMonth, endMonth)).thenReturn(entities);
        when(calendarAlertMapper.toModelList(entities)).thenReturn(expectedAlerts);

        List<CalendarAlert> result = calendarAlertRepository.findByTeacherIdAndYearAndMonthRange(teacherId, year, startMonth, endMonth);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(calendarAlertJPARepository).findByTeacherIdAndYearAndMonthRange(teacherId, year, startMonth, endMonth);
        verify(calendarAlertMapper).toModelList(entities);
    }

    @Test
    void findByTeacherIdAndYearAndMonthRange_shouldReturnEmptyList_whenNoAlertsFound() {
        final Integer teacherId = 1;
        final Integer year = 2099;
        final Integer startMonth = 1;
        final Integer endMonth = 12;

        when(calendarAlertJPARepository.findByTeacherIdAndYearAndMonthRange(teacherId, year, startMonth, endMonth)).thenReturn(Collections.emptyList());
        when(calendarAlertMapper.toModelList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<CalendarAlert> result = calendarAlertRepository.findByTeacherIdAndYearAndMonthRange(teacherId, year, startMonth, endMonth);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(calendarAlertJPARepository).findByTeacherIdAndYearAndMonthRange(teacherId, year, startMonth, endMonth);
    }
}

