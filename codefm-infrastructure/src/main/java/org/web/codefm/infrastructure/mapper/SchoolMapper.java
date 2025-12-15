package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SchoolEntity;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ClassMapper.class}, builder = @Builder(disableBuilder = false))
public interface SchoolMapper {

    School toModel(SchoolEntity entity);

    List<School> toModelList(List<SchoolEntity> entities);

    SchoolEntity toEntity(School school);
}
