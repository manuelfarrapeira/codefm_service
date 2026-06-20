package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.model.CalendarAlertDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CalendarAlertDTOMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Mapping(target = "date", expression = "java(formatDate(calendarAlert.getDate()))")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "localTimeToString")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "localTimeToString")
    CalendarAlertDTO toDTO(CalendarAlert calendarAlert);

    List<CalendarAlertDTO> toDTOList(List<CalendarAlert> calendarAlerts);

    default String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    @Named("localTimeToString")
    default String localTimeToString(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : null;
    }
}

