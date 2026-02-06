package org.web.codefm.application.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.domain.service.teachernotebook.ScheduleService;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleUseCaseImplTest {

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ScheduleUseCaseImpl scheduleUseCase;

    @Test
    void getSchedulesByClassId_shouldDelegateToService() {
        Integer classId = 1;
        Schedule schedule = Schedule.builder()
                .id(1)
                .classId(classId)
                .subjectId(1)
                .day(1)
                .start(LocalTime.of(8, 30))
                .end(LocalTime.of(9, 30))
                .build();

        when(scheduleService.getSchedulesByClassId(classId)).thenReturn(List.of(schedule));

        List<Schedule> result = scheduleUseCase.getSchedulesByClassId(classId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scheduleService, times(1)).getSchedulesByClassId(classId);
    }

    @Test
    void createSchedules_shouldDelegateToService() {
        Integer classId = 1;
        Integer day = 1;
        Schedule inputSchedule = Schedule.builder()
                .subjectId(1)
                .start(LocalTime.of(8, 30))
                .end(LocalTime.of(9, 30))
                .build();

        Schedule savedSchedule = Schedule.builder()
                .id(1)
                .classId(classId)
                .subjectId(1)
                .day(day)
                .start(LocalTime.of(8, 30))
                .end(LocalTime.of(9, 30))
                .build();

        when(scheduleService.createSchedules(classId, day, List.of(inputSchedule)))
                .thenReturn(List.of(savedSchedule));

        List<Schedule> result = scheduleUseCase.createSchedules(classId, day, List.of(inputSchedule));

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scheduleService, times(1)).createSchedules(classId, day, List.of(inputSchedule));
    }

    @Test
    void updateSchedule_shouldDelegateToService() {
        Integer scheduleId = 1;
        Schedule updateData = Schedule.builder()
                .day(2)
                .start(LocalTime.of(9, 0))
                .end(LocalTime.of(10, 0))
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(scheduleId)
                .classId(1)
                .subjectId(1)
                .day(2)
                .start(LocalTime.of(9, 0))
                .end(LocalTime.of(10, 0))
                .build();

        when(scheduleService.updateSchedule(scheduleId, updateData)).thenReturn(updatedSchedule);

        Schedule result = scheduleUseCase.updateSchedule(scheduleId, updateData);

        assertNotNull(result);
        assertEquals(2, result.getDay());
        verify(scheduleService, times(1)).updateSchedule(scheduleId, updateData);
    }

    @Test
    void softDeleteSchedules_shouldDelegateToService() {
        List<Integer> ids = List.of(1, 2, 3);

        scheduleUseCase.softDeleteSchedules(ids);

        verify(scheduleService, times(1)).softDeleteSchedules(ids);
    }
}
