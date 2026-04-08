package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.domain.service.teachernotebook.CalendarAlertService;
import org.web.codefm.domain.usecase.teachernotebook.CalendarAlertUseCase;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarAlertUseCaseImpl implements CalendarAlertUseCase {

    private final CalendarAlertService calendarAlertService;

    @Override
    public List<CalendarAlert> getCalendarAlerts() {
        return calendarAlertService.getCalendarAlerts();
    }

    @Override
    public List<CalendarAlert> getCalendarAlertsByYearAndMonth(Integer year, Integer month) {
        return calendarAlertService.getCalendarAlertsByYearAndMonth(year, month);
    }

    @Override
    public List<CalendarAlert> getCalendarAlertsByYearAndMonthRange(Integer year, Integer startMonth, Integer endMonth) {
        return this.calendarAlertService.getCalendarAlertsByYearAndMonthRange(year, startMonth, endMonth);
    }

    @Override
    public CalendarAlert createCalendarAlert(CalendarAlert calendarAlert) {
        return calendarAlertService.createCalendarAlert(calendarAlert);
    }

    @Override
    public CalendarAlert updateCalendarAlert(Integer id, CalendarAlert calendarAlert) {
        return calendarAlertService.updateCalendarAlert(id, calendarAlert);
    }

    @Override
    public void deleteCalendarAlert(Integer id) {
        calendarAlertService.deleteCalendarAlert(id);
    }
}

