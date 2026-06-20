package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.domain.exception.teachernotebook.CalendarAlertValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.model.CalendarAlertRequestDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class CalendarAlertRequestMapper {

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    protected SessionUser sessionUser;

    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacherId", ignore = true)
    @Mapping(target = "date", expression = "java(parseDate(dto.getDate()))")
    @Mapping(target = "startTime", expression = "java(parseTime(dto.getStartTime()))")
    @Mapping(target = "endTime", expression = "java(parseTime(dto.getEndTime()))")
    public abstract CalendarAlert toDomain(CalendarAlertRequestDTO dto);

    protected LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            List<ErrorMessage> errors = new ArrayList<>();
            String message = messageSource.getMessage(
                    MessageKeys.CALENDAR_ALERT_VALIDATION_DATE_REQUIRED,
                    null,
                    sessionUser.getLocale()
            );
            errors.add(new ErrorMessage("date", message));
            throw new CalendarAlertValidationException(errors);
        }
    }

    protected LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }

        return LocalTime.parse(timeString, TIME_FORMATTER);
    }
}

