package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

    @Mapping(target = "subjectId", ignore = true)
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "documents", ignore = true)
    Exercise toModel(ExerciseEntity entity);

    List<Exercise> toModelList(List<ExerciseEntity> entities);

    ExerciseEntity toEntity(Exercise exercise);
}

