package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.model.ExerciseStudentDocumentDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExerciseStudentDocumentDTOMapper {

    ExerciseStudentDocumentDTO toDTO(ExerciseStudentDocument document);

    List<ExerciseStudentDocumentDTO> toDTOList(List<ExerciseStudentDocument> documents);
}

