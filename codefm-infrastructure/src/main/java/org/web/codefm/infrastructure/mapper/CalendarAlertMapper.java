package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.CalendarAlertEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CalendarAlertMapper {

    CalendarAlert toModel(CalendarAlertEntity entity);

    List<CalendarAlert> toModelList(List<CalendarAlertEntity> entities);

    CalendarAlertEntity toEntity(CalendarAlert calendarAlert);
}

