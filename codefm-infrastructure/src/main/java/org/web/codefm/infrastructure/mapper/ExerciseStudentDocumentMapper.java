package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseStudentDocumentEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExerciseStudentDocumentMapper {

    ExerciseStudentDocument toModel(ExerciseStudentDocumentEntity entity);

    List<ExerciseStudentDocument> toModelList(List<ExerciseStudentDocumentEntity> entities);

    ExerciseStudentDocumentEntity toEntity(ExerciseStudentDocument document);
}

