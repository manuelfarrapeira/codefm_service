package org.web.codefm.application.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.domain.service.teachernotebook.ScheduleService;
import org.web.codefm.domain.usecase.teachernotebook.ScheduleUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleUseCaseImpl implements ScheduleUseCase {

    private final ScheduleService scheduleService;

    @Override
    public List<Schedule> getSchedulesByClassId(Integer classId) {
        return scheduleService.getSchedulesByClassId(classId);
    }

    @Override
    public List<Schedule> createSchedules(Integer classId, Integer day, List<Schedule> schedules) {
        return scheduleService.createSchedules(classId, day, schedules);
    }

    @Override
    public Schedule updateSchedule(Integer scheduleId, Schedule schedule) {
        return scheduleService.updateSchedule(scheduleId, schedule);
    }

    @Override
    public void softDeleteSchedules(List<Integer> ids) {
        scheduleService.softDeleteSchedules(ids);
    }
}
