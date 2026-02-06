package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.model.ScheduleItemDTO;
import org.web.codefm.model.ScheduleUpdateRequestDTO;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ScheduleRequestMapper {

    @Named("stringToLocalTime")
    protected LocalTime stringToLocalTime(String time) {
        return time != null ? LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")) : null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classId", ignore = true)
    @Mapping(target = "deletionDate", ignore = true)
    @Mapping(target = "day", ignore = true)
    @Mapping(target = "start", source = "start", qualifiedByName = "stringToLocalTime")
    @Mapping(target = "end", source = "end", qualifiedByName = "stringToLocalTime")
    public abstract Schedule toDomain(ScheduleItemDTO dto);

    public abstract List<Schedule> toDomainList(List<ScheduleItemDTO> dtos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classId", ignore = true)
    @Mapping(target = "subjectId", ignore = true)
    @Mapping(target = "deletionDate", ignore = true)
    @Mapping(target = "start", source = "start", qualifiedByName = "stringToLocalTime")
    @Mapping(target = "end", source = "end", qualifiedByName = "stringToLocalTime")
    public abstract Schedule toDomainForUpdate(ScheduleUpdateRequestDTO dto);
}
