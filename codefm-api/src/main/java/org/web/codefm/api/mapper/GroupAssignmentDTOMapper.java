package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.model.GroupAssignmentDTO;
import org.web.codefm.model.GroupAssignmentDocumentSummaryDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupAssignmentDTOMapper {

    GroupAssignmentDTO toDTO(GroupAssignment groupAssignment);

    List<GroupAssignmentDTO> toDTOList(List<GroupAssignment> groupAssignments);

    GroupAssignment toDomain(org.web.codefm.model.GroupAssignmentRequestDTO dto);

    GroupAssignmentDocumentSummaryDTO toDocumentSummaryDTO(GroupAssignmentDocument document);

    List<GroupAssignmentDocumentSummaryDTO> toDocumentSummaryDTOList(List<GroupAssignmentDocument> documents);
}
