package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;
import org.web.codefm.model.GroupAssignmentDocumentSummaryDTO;
import org.web.codefm.model.GroupAssignmentGradeDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupAssignmentGradeDTOMapper {

    GroupAssignmentGradeDTO toDTO(GroupAssignmentGrade grade);

    List<GroupAssignmentGradeDTO> toDTOList(List<GroupAssignmentGrade> grades);

    GroupAssignmentDocumentSummaryDTO toDocumentSummaryDTO(GroupAssignmentDocument document);

    List<GroupAssignmentDocumentSummaryDTO> toDocumentSummaryDTOList(List<GroupAssignmentDocument> documents);
}
