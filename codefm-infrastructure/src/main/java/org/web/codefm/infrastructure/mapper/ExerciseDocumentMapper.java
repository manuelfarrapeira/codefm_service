package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseDocumentEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExerciseDocumentMapper {

    ExerciseDocument toModel(ExerciseDocumentEntity entity);

    List<ExerciseDocument> toModelList(List<ExerciseDocumentEntity> entities);

    ExerciseDocumentEntity toEntity(ExerciseDocument exerciseDocument);
}

