package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.RubricCriterionAssignment;
import org.web.codefm.domain.entity.teachernotebook.StudentCriteriaGroup;
import org.web.codefm.model.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentCriteriaGroupDTOMapper {

    @Mapping(target = "student", expression = "java(toStudentSummary(group))")
    StudentCriteriaGroupDTO toDTO(StudentCriteriaGroup group);

    List<StudentCriteriaGroupDTO> toDTOList(List<StudentCriteriaGroup> groups);

    @Mapping(target = "rubric", expression = "java(toRubricSummary(assignment))")
    @Mapping(target = "criterion", expression = "java(toCriterionSummary(assignment))")
    RubricCriterionAssignmentDTO toAssignmentDTO(RubricCriterionAssignment assignment);

    default StudentSummaryDTO toStudentSummary(StudentCriteriaGroup group) {
        final StudentSummaryDTO dto = new StudentSummaryDTO();
        dto.setId(group.getStudentId());
        dto.setName(group.getStudentName());
        dto.setSurnames(group.getStudentSurnames());
        return dto;
    }

    default RubricSummaryDTO toRubricSummary(RubricCriterionAssignment assignment) {
        final RubricSummaryDTO dto = new RubricSummaryDTO();
        dto.setId(assignment.getRubricId());
        dto.setTitle(assignment.getRubricTitle());
        return dto;
    }

    default CriterionSummaryDTO toCriterionSummary(RubricCriterionAssignment assignment) {
        final CriterionSummaryDTO dto = new CriterionSummaryDTO();
        dto.setId(assignment.getCriterionId());
        dto.setDescription(assignment.getCriterionDescription());
        dto.setGradeStart(assignment.getGradeStart());
        dto.setGradeEnd(assignment.getGradeEnd());
        return dto;
    }
}

