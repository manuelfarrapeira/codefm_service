package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for calendar alert data access operations.
 * Provides methods to retrieve and manage calendar alert information.
 */
public interface CalendarAlertRepository {

    /**
     * Finds all calendar alerts associated with a specific teacher.
     *
     * @param teacherId The unique identifier of the teacher
     * @return List of calendar alerts belonging to the specified teacher, ordered by date ascending
     */
    List<CalendarAlert> findByTeacherId(Integer teacherId);

    /**
     * Finds all calendar alerts for a specific teacher, year and month.
     *
     * @param teacherId The unique identifier of the teacher
     * @param year      The year to filter by
     * @param month     The month to filter by (1-12)
     * @return List of calendar alerts matching the criteria, ordered by date ascending
     */
    List<CalendarAlert> findByTeacherIdAndYearAndMonth(Integer teacherId, Integer year, Integer month);

    /**
     * Finds all calendar alerts for a specific teacher, year and month range.
     *
     * @param teacherId  The unique identifier of the teacher
     * @param year       The year to filter by
     * @param startMonth The start month of the range (1-12)
     * @param endMonth   The end month of the range (1-12)
     * @return List of calendar alerts matching the criteria, ordered by date ascending
     */
    List<CalendarAlert> findByTeacherIdAndYearAndMonthRange(Integer teacherId, Integer year, Integer startMonth, Integer endMonth);

    /**
     * Finds a calendar alert by its ID and validates teacher ownership.
     *
     * @param id        The unique identifier of the calendar alert
     * @param teacherId The unique identifier of the teacher
     * @return An Optional containing the calendar alert if found and owned by the teacher
     */
    Optional<CalendarAlert> findByIdAndTeacherId(Integer id, Integer teacherId);

    /**
     * Saves a new calendar alert or updates an existing one.
     *
     * @param calendarAlert The calendar alert object to save
     * @return The saved calendar alert object with generated ID
     */
    CalendarAlert save(CalendarAlert calendarAlert);

    /**
     * Permanently deletes a calendar alert by its ID (hard delete).
     *
     * @param id The unique identifier of the calendar alert to delete
     */
    void deleteById(Integer id);
}

