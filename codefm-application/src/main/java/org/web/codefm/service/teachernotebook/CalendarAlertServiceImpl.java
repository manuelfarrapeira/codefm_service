package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.domain.exception.teachernotebook.CalendarAlertNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.CalendarAlertValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.CalendarAlertRepository;
import org.web.codefm.domain.service.teachernotebook.CalendarAlertService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarAlertServiceImpl implements CalendarAlertService {

    private final CalendarAlertRepository calendarAlertRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public List<CalendarAlert> getCalendarAlerts() {
        Integer teacherId = getTeacherId();
        return calendarAlertRepository.findByTeacherId(teacherId);
    }

    @Override
    public CalendarAlert createCalendarAlert(CalendarAlert calendarAlert) {
        List<ErrorMessage> errors = new ArrayList<>();
        Locale locale = sessionUser.getLocale();

        validateCalendarAlert(calendarAlert, errors, locale);

        if (!errors.isEmpty()) {
            throw new CalendarAlertValidationException(errors);
        }

        calendarAlert.setTeacherId(getTeacherId());
        return calendarAlertRepository.save(calendarAlert);
    }

    @Override
    public CalendarAlert updateCalendarAlert(Integer id, CalendarAlert calendarAlert) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();
        List<ErrorMessage> errors = new ArrayList<>();

        validateCalendarAlert(calendarAlert, errors, locale);

        if (!errors.isEmpty()) {
            throw new CalendarAlertValidationException(errors);
        }

        CalendarAlert existingAlert = calendarAlertRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new CalendarAlertNotFoundException(
                        messageSource.getMessage(MessageKeys.CALENDAR_ALERT_NOT_FOUND, null, locale)));

        existingAlert.setDate(calendarAlert.getDate());
        existingAlert.setTitle(calendarAlert.getTitle());
        existingAlert.setDescription(calendarAlert.getDescription());
        existingAlert.setStartTime(calendarAlert.getStartTime());
        existingAlert.setEndTime(calendarAlert.getEndTime());

        return calendarAlertRepository.save(existingAlert);
    }

    @Override
    public void deleteCalendarAlert(Integer id) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();

        calendarAlertRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new CalendarAlertNotFoundException(
                        messageSource.getMessage(MessageKeys.CALENDAR_ALERT_NOT_FOUND, null, locale)));

        calendarAlertRepository.deleteById(id);
    }

    private Integer getTeacherId() {
        return sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);
    }

    private void validateCalendarAlert(CalendarAlert calendarAlert, List<ErrorMessage> errors, Locale locale) {
        if (calendarAlert.getTitle() == null || calendarAlert.getTitle().trim().isEmpty()) {
            String message = messageSource.getMessage(MessageKeys.CALENDAR_ALERT_VALIDATION_TITLE_REQUIRED, null, locale);
            errors.add(new ErrorMessage("title", message));
        } else if (calendarAlert.getTitle().trim().length() > 100) {
            String message = messageSource.getMessage(MessageKeys.CALENDAR_ALERT_VALIDATION_TITLE_MAX_LENGTH, null, locale);
            errors.add(new ErrorMessage("title", message));
        }

        if (calendarAlert.getDate() == null) {
            String message = messageSource.getMessage(MessageKeys.CALENDAR_ALERT_VALIDATION_DATE_REQUIRED, null, locale);
            errors.add(new ErrorMessage("date", message));
        }

        if (calendarAlert.getEndTime() != null && calendarAlert.getStartTime() == null) {
            String message = messageSource.getMessage(MessageKeys.CALENDAR_ALERT_VALIDATION_END_TIME_WITHOUT_START_TIME, null, locale);
            errors.add(new ErrorMessage("endTime", message));
        }

        if (calendarAlert.getStartTime() != null && calendarAlert.getEndTime() != null
                && !calendarAlert.getEndTime().isAfter(calendarAlert.getStartTime())) {
            String message = messageSource.getMessage(MessageKeys.CALENDAR_ALERT_VALIDATION_END_TIME_BEFORE_START_TIME, null, locale);
            errors.add(new ErrorMessage("endTime", message));
        }
    }
}

