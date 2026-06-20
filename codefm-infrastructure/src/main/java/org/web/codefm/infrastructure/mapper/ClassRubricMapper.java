package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassRubricEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClassRubricMapper {

    @Mapping(target = "rubricTitle", ignore = true)
    @Mapping(target = "skillId", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    ClassRubric toModel(ClassRubricEntity entity);

    List<ClassRubric> toModelList(List<ClassRubricEntity> entities);

    ClassRubricEntity toEntity(ClassRubric model);
}

