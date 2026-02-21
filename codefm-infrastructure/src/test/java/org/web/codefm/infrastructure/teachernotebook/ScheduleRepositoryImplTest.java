package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ScheduleEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ScheduleJPARepository;
import org.web.codefm.infrastructure.mapper.ScheduleMapper;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleRepositoryImplTest {

    @Mock
    private ScheduleJPARepository scheduleJPARepository;

    @Mock
    private ScheduleMapper scheduleMapper;

    @InjectMocks
    private ScheduleRepositoryImpl scheduleRepository;

    @Test
    void findByClassId_shouldReturnSchedules() {
        Integer classId = 1;
        ScheduleEntity entity = new ScheduleEntity(1, classId, 1, 1, LocalTime.of(8, 30), LocalTime.of(9, 30), null);
        Schedule schedule = Schedule.builder().id(1).classId(classId).build();

        when(scheduleJPARepository.findByClassIdAndDeletionDateIsNullOrderByDayAscStartAsc(classId)).thenReturn(List.of(entity));
        when(scheduleMapper.toModelList(List.of(entity))).thenReturn(List.of(schedule));

        List<Schedule> result = scheduleRepository.findByClassId(classId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scheduleJPARepository, times(1)).findByClassIdAndDeletionDateIsNullOrderByDayAscStartAsc(classId);
    }

    @Test
    void findById_shouldReturnSchedule_whenFound() {
        Integer id = 1;
        ScheduleEntity entity = new ScheduleEntity(id, 1, 1, 1, LocalTime.of(8, 30), LocalTime.of(9, 30), null);
        Schedule schedule = Schedule.builder().id(id).build();

        when(scheduleJPARepository.findByIdAndDeletionDateIsNull(id)).thenReturn(Optional.of(entity));
        when(scheduleMapper.toModel(entity)).thenReturn(schedule);

        Optional<Schedule> result = scheduleRepository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        Integer id = 99;

        when(scheduleJPARepository.findByIdAndDeletionDateIsNull(id)).thenReturn(Optional.empty());

        Optional<Schedule> result = scheduleRepository.findById(id);

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnSchedule_whenFoundAndOwned() {
        Integer id = 1;
        Integer teacherId = 1;
        ScheduleEntity entity = new ScheduleEntity(id, 1, 1, 1, LocalTime.of(8, 30), LocalTime.of(9, 30), null);
        Schedule schedule = Schedule.builder().id(id).build();

        when(scheduleJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.of(entity));
        when(scheduleMapper.toModel(entity)).thenReturn(schedule);

        Optional<Schedule> result = scheduleRepository.findByIdAndTeacherId(id, teacherId);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void saveAll_shouldSaveAndReturnSchedules() {
        Schedule schedule = Schedule.builder().subjectId(1).day(1).start(LocalTime.of(8, 30)).end(LocalTime.of(9, 30)).build();
        ScheduleEntity entity = new ScheduleEntity();
        ScheduleEntity savedEntity = new ScheduleEntity(1, 1, 1, 1, LocalTime.of(8, 30), LocalTime.of(9, 30), null);
        Schedule savedSchedule = Schedule.builder().id(1).build();

        when(scheduleMapper.toEntityList(List.of(schedule))).thenReturn(List.of(entity));
        when(scheduleJPARepository.saveAll(List.of(entity))).thenReturn(List.of(savedEntity));
        when(scheduleMapper.toModelList(List.of(savedEntity))).thenReturn(List.of(savedSchedule));

        List<Schedule> result = scheduleRepository.saveAll(List.of(schedule));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
    }

    @Test
    void save_shouldSaveAndReturnSchedule() {
        Schedule schedule = Schedule.builder().subjectId(1).day(1).start(LocalTime.of(8, 30)).end(LocalTime.of(9, 30)).build();
        ScheduleEntity entity = new ScheduleEntity();
        ScheduleEntity savedEntity = new ScheduleEntity(1, 1, 1, 1, LocalTime.of(8, 30), LocalTime.of(9, 30), null);
        Schedule savedSchedule = Schedule.builder().id(1).build();

        when(scheduleMapper.toEntity(schedule)).thenReturn(entity);
        when(scheduleJPARepository.save(entity)).thenReturn(savedEntity);
        when(scheduleMapper.toModel(savedEntity)).thenReturn(savedSchedule);

        Schedule result = scheduleRepository.save(schedule);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void update_shouldUpdateAndReturnSchedule() {
        Schedule schedule = Schedule.builder().id(1).subjectId(1).day(2).start(LocalTime.of(9, 0)).end(LocalTime.of(10, 0)).build();
        ScheduleEntity entity = new ScheduleEntity(1, 1, 1, 2, LocalTime.of(9, 0), LocalTime.of(10, 0), null);
        Schedule updatedSchedule = Schedule.builder().id(1).day(2).build();

        when(scheduleMapper.toEntity(schedule)).thenReturn(entity);
        when(scheduleJPARepository.save(entity)).thenReturn(entity);
        when(scheduleMapper.toModel(entity)).thenReturn(updatedSchedule);

        Schedule result = scheduleRepository.update(schedule);

        assertNotNull(result);
        assertEquals(2, result.getDay());
    }

    @Test
    void allSchedulesBelongToTeacher_shouldReturnTrue_whenAllBelong() {
        List<Integer> ids = List.of(1, 2, 3);
        Integer teacherId = 1;

        when(scheduleJPARepository.countByIdsAndTeacherId(ids, teacherId)).thenReturn(3L);

        boolean result = scheduleRepository.allSchedulesBelongToTeacher(ids, teacherId);

        assertTrue(result);
    }

    @Test
    void allSchedulesBelongToTeacher_shouldReturnFalse_whenNotAllBelong() {
        List<Integer> ids = List.of(1, 2, 3);
        Integer teacherId = 1;

        when(scheduleJPARepository.countByIdsAndTeacherId(ids, teacherId)).thenReturn(2L);

        boolean result = scheduleRepository.allSchedulesBelongToTeacher(ids, teacherId);

        assertFalse(result);
    }

    @Test
    void softDeleteSchedules_shouldCallSoftDelete() {
        List<Integer> ids = List.of(1, 2, 3);
        Integer teacherId = 1;

        scheduleRepository.softDeleteSchedules(ids, teacherId);

        verify(scheduleJPARepository, times(1)).softDeleteByIds(ids);
    }
}
