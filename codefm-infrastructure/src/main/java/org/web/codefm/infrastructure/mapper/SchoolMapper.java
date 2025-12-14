package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.School;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SchoolEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SchoolMapper {

    School toModel(SchoolEntity entity);

    List<School> toModelList(List<SchoolEntity> entities);
}
