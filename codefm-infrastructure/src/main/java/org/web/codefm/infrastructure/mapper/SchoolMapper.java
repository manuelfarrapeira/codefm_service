package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SchoolEntity;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface SchoolMapper {

    @Mapping(target = "classes", ignore = true)
    School toModel(SchoolEntity entity);

    List<School> toModelList(List<SchoolEntity> entities);

    SchoolEntity toEntity(School school);
}
