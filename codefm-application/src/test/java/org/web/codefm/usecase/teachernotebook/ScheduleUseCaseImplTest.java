package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.domain.service.teachernotebook.ScheduleService;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleUseCaseImplTest {

    private ScheduleUseCaseImpl scheduleUseCase;

    @Mock
    private ScheduleService scheduleService;

    @BeforeEach
    void beforeEach() {
        scheduleUseCase = new ScheduleUseCaseImpl(scheduleService);
    }

    @Nested
    class GetSchedulesByClassId {

        @Test
        void when_schedules_found_expect_delegated_to_service() {
            final Integer classId = 1;
            final Schedule schedule = Schedule.builder()
                    .id(1).classId(classId).subjectId(1).day(1)
                    .start(LocalTime.of(8, 30)).end(LocalTime.of(9, 30)).build();
            when(scheduleService.getSchedulesByClassId(classId)).thenReturn(List.of(schedule));

            final List<Schedule> result = scheduleUseCase.getSchedulesByClassId(classId);

            assertThat(result).isNotNull().hasSize(1);
            verify(scheduleService).getSchedulesByClassId(classId);
        }
    }

    @Nested
    class CreateSchedules {

        @Test
        void when_creating_schedules_expect_delegated_to_service() {
            final Integer classId = 1;
            final Integer day = 1;
            final Schedule inputSchedule = Schedule.builder()
                    .subjectId(1).start(LocalTime.of(8, 30)).end(LocalTime.of(9, 30)).build();
            final Schedule savedSchedule = Schedule.builder()
                    .id(1).classId(classId).subjectId(1).day(day)
                    .start(LocalTime.of(8, 30)).end(LocalTime.of(9, 30)).build();
            when(scheduleService.createSchedules(classId, day, List.of(inputSchedule))).thenReturn(List.of(savedSchedule));

            final List<Schedule> result = scheduleUseCase.createSchedules(classId, day, List.of(inputSchedule));

            assertThat(result).isNotNull().hasSize(1);
            verify(scheduleService).createSchedules(classId, day, List.of(inputSchedule));
        }
    }

    @Nested
    class UpdateSchedule {

        @Test
        void when_updating_schedule_expect_delegated_to_service() {
            final Integer scheduleId = 1;
            final Schedule updateData = Schedule.builder()
                    .day(2).start(LocalTime.of(9, 0)).end(LocalTime.of(10, 0)).build();
            final Schedule updatedSchedule = Schedule.builder()
                    .id(scheduleId).classId(1).subjectId(1).day(2)
                    .start(LocalTime.of(9, 0)).end(LocalTime.of(10, 0)).build();
            when(scheduleService.updateSchedule(scheduleId, updateData)).thenReturn(updatedSchedule);

            final Schedule result = scheduleUseCase.updateSchedule(scheduleId, updateData);

            assertThat(result).isNotNull();
            assertThat(result.getDay()).isEqualTo(2);
            verify(scheduleService).updateSchedule(scheduleId, updateData);
        }
    }

    @Nested
    class SoftDeleteSchedules {

        @Test
        void when_deleting_schedules_expect_delegated_to_service() {
            final List<Integer> ids = List.of(1, 2, 3);

            scheduleUseCase.softDeleteSchedules(ids);

            verify(scheduleService).softDeleteSchedules(ids);
        }
    }
}
