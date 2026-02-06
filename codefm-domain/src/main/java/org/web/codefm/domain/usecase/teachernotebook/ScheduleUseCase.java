package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Schedule;

import java.util.List;

/**
 * Interface that defines schedule operations for teachers.
 * Handles class schedule data retrieval and management.
 */
public interface ScheduleUseCase {

    /**
     * Retrieves all schedules for a specific class.
     *
     * @param classId The unique identifier of the class
     * @return List of schedules belonging to the specified class
     */
    List<Schedule> getSchedulesByClassId(Integer classId);

    /**
     * Creates multiple schedules for a class on a specific day.
     *
     * @param classId   The unique identifier of the class
     * @param day       The day of the week (1=Monday, 5=Friday)
     * @param schedules The list of schedules to create
     * @return The list of created schedules
     */
    List<Schedule> createSchedules(Integer classId, Integer day, List<Schedule> schedules);

    /**
     * Updates an existing schedule.
     *
     * @param scheduleId The unique identifier of the schedule to update
     * @param schedule   The schedule object containing the updated data
     * @return The updated schedule
     */
    Schedule updateSchedule(Integer scheduleId, Schedule schedule);

    /**
     * Soft-deletes multiple schedules by setting their deletion date.
     *
     * @param ids The list of schedule IDs to delete
     */
    void softDeleteSchedules(List<Integer> ids);
}
