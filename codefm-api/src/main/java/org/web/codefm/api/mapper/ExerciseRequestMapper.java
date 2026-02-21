package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.model.ExerciseRequestDTO;
import org.web.codefm.model.ExerciseUpdateRequestDTO;

@Mapper(componentModel = "spring")
public interface ExerciseRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subjectClassId", ignore = true)
    @Mapping(target = "subjectId", ignore = true)
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "deletionDate", ignore = true)
    Exercise toDomain(ExerciseRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subjectClassId", ignore = true)
    @Mapping(target = "subjectId", ignore = true)
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "deletionDate", ignore = true)
    Exercise toDomainForUpdate(ExerciseUpdateRequestDTO dto);
}

