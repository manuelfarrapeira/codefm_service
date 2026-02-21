package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.model.ScheduleDTO;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ScheduleDTOMapper {

    @Mapping(target = "start", source = "start", qualifiedByName = "localTimeToString")
    @Mapping(target = "end", source = "end", qualifiedByName = "localTimeToString")
    ScheduleDTO toDTO(Schedule schedule);

    List<ScheduleDTO> toDTOList(List<Schedule> schedules);

    @Named("localTimeToString")
    default String localTimeToString(LocalTime time) {
        return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm")) : null;
    }
}
