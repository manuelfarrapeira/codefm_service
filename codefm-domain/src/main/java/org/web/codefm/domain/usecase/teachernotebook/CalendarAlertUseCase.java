package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;

import java.util.List;

/**
 * Interface that defines calendar alert operations for teachers.
 * Handles calendar alert data retrieval and management.
 */
public interface CalendarAlertUseCase {

    /**
     * Retrieves all calendar alerts for the authenticated teacher.
     *
     * @return List of calendar alerts belonging to the teacher
     */
    List<CalendarAlert> getCalendarAlerts();

    /**
     * Retrieves calendar alerts for the authenticated teacher filtered by year and month.
     *
     * @param year  The year to filter by
     * @param month The month to filter by (1-12)
     * @return List of calendar alerts matching the criteria
     */
    List<CalendarAlert> getCalendarAlertsByYearAndMonth(Integer year, Integer month);

    /**
     * Retrieves calendar alerts for the authenticated teacher filtered by year and a month range.
     *
     * @param year       The year to filter by
     * @param startMonth The start month of the range (1-12)
     * @param endMonth   The end month of the range (1-12), must be greater than or equal to startMonth
     * @return List of calendar alerts matching the criteria
     */
    List<CalendarAlert> getCalendarAlertsByYearAndMonthRange(Integer year, Integer startMonth, Integer endMonth);

    /**
     * Creates a new calendar alert.
     *
     * @param calendarAlert The calendar alert object to create
     * @return The created calendar alert object
     */
    CalendarAlert createCalendarAlert(CalendarAlert calendarAlert);

    /**
     * Updates an existing calendar alert.
     *
     * @param id            The unique identifier of the calendar alert to update
     * @param calendarAlert The calendar alert object containing the updated data
     * @return The updated calendar alert object
     */
    CalendarAlert updateCalendarAlert(Integer id, CalendarAlert calendarAlert);

    /**
     * Permanently deletes a calendar alert (hard delete).
     *
     * @param id The unique identifier of the calendar alert to delete
     */
    void deleteCalendarAlert(Integer id);
}

