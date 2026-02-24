package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.model.ExerciseDocumentDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExerciseDocumentDTOMapper {

    ExerciseDocumentDTO toDTO(ExerciseDocument exerciseDocument);

    List<ExerciseDocumentDTO> toDTOList(List<ExerciseDocument> exerciseDocuments);
}

