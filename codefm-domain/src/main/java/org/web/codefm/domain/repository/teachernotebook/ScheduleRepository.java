package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Schedule;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for schedule data access operations. Provides methods to
 * retrieve and manage class schedule information.
 */
public interface ScheduleRepository {

	/**
	 * Finds all schedules for a specific class.
	 *
	 * @param classId
	 *            The unique identifier of the class
	 * @return List of schedules belonging to the specified class
	 */
	List<Schedule> findByClassId(Integer classId);

	/**
	 * Finds a schedule by its ID.
	 *
	 * @param id
	 *            The unique identifier of the schedule
	 * @return An Optional containing the schedule if found
	 */
	Optional<Schedule> findById(Integer id);

	/**
	 * Finds a schedule by its ID and validates teacher ownership.
	 *
	 * @param id
	 *            The unique identifier of the schedule
	 * @param teacherId
	 *            The unique identifier of the teacher
	 * @return An Optional containing the schedule if found and owned by the teacher
	 */
	Optional<Schedule> findByIdAndTeacherId(Integer id, Integer teacherId);

	/**
	 * Saves multiple schedules.
	 *
	 * @param schedules
	 *            The list of schedules to save
	 * @return The list of saved schedules with generated IDs
	 */
	List<Schedule> saveAll(List<Schedule> schedules);

	/**
	 * Saves a single schedule.
	 *
	 * @param schedule
	 *            The schedule to save
	 * @return The saved schedule with generated ID
	 */
	Schedule save(Schedule schedule);

	/**
	 * Updates an existing schedule.
	 *
	 * @param schedule
	 *            The schedule with updated data
	 * @return The updated schedule
	 */
	Schedule update(Schedule schedule);

	/**
	 * Soft-deletes multiple schedules by setting their deletion date.
	 *
	 * @param ids
	 *            The list of schedule IDs to delete
	 * @param teacherId
	 *            The unique identifier of the teacher (for validation)
	 */
	void softDeleteSchedules(List<Integer> ids, Integer teacherId);

	/**
	 * Checks if all schedule IDs belong to the specified teacher.
	 *
	 * @param ids
	 *            The list of schedule IDs to check
	 * @param teacherId
	 *            The unique identifier of the teacher
	 * @return true if all schedules belong to the teacher, false otherwise
	 */
	boolean allSchedulesBelongToTeacher(List<Integer> ids, Integer teacherId);

	/**
	 * Checks if there is any schedule that overlaps with the given time range.
	 *
	 * @param classId
	 *            The unique identifier of the class
	 * @param day
	 *            The day of the week (1-5)
	 * @param start
	 *            The start time
	 * @param end
	 *            The end time
	 * @param excludeScheduleId
	 *            Optional schedule ID to exclude from the check (for updates)
	 * @return true if there is an overlapping schedule, false otherwise
	 */
	boolean existsOverlappingSchedule(Integer classId, Integer day, java.time.LocalTime start, java.time.LocalTime end,
			Integer excludeScheduleId);

	/**
	 * Soft-deletes all schedules for a specific class.
	 *
	 * @param classId
	 *            The unique identifier of the class
	 */
	void softDeleteByClassId(Integer classId);

	/**
	 * Soft-deletes all schedules for a specific subject.
	 *
	 * @param subjectId
	 *            The unique identifier of the subject
	 */
	void softDeleteBySubjectId(Integer subjectId);

	/**
	 * Finds distinct subject IDs that have schedules for a specific class on a
	 * given day of the week.
	 *
	 * @param classId
	 *            The unique identifier of the class
	 * @param day
	 *            The day of the week (1=Monday, 5=Friday)
	 * @return List of distinct subject IDs scheduled for that class on that day
	 */
	List<Integer> findSubjectIdsByClassIdAndDay(Integer classId, Integer day);
}
