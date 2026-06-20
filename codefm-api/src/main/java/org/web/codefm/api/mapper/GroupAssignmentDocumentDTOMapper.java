package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.model.GroupAssignmentDocumentDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupAssignmentDocumentDTOMapper {

    GroupAssignmentDocumentDTO toDTO(GroupAssignmentDocument document);

    List<GroupAssignmentDocumentDTO> toDTOList(List<GroupAssignmentDocument> documents);
}

