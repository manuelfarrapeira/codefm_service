package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;

import java.util.List;

/**
 * Service interface for calendar alert business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface CalendarAlertService {

    /**
     * Retrieves all calendar alerts for the authenticated teacher.
     *
     * @return List of calendar alerts belonging to the authenticated teacher
     */
    List<CalendarAlert> getCalendarAlerts();

    /**
     * Retrieves calendar alerts for the authenticated teacher filtered by year and month.
     *
     * @param year  The year to filter by
     * @param month The month to filter by (1-12)
     * @return List of calendar alerts matching the criteria
     * @throws org.web.codefm.domain.exception.teachernotebook.CalendarAlertValidationException if year or month are invalid
     */
    List<CalendarAlert> getCalendarAlertsByYearAndMonth(Integer year, Integer month);

    /**
     * Creates a new calendar alert for the authenticated teacher.
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

