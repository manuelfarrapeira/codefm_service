package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ScheduleEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    Schedule toModel(ScheduleEntity entity);

    List<Schedule> toModelList(List<ScheduleEntity> entities);

    ScheduleEntity toEntity(Schedule schedule);

    List<ScheduleEntity> toEntityList(List<Schedule> schedules);
}
