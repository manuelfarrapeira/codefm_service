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

