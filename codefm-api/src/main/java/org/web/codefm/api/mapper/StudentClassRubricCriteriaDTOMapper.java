package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.model.StudentClassRubricCriteriaDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentClassRubricCriteriaDTOMapper {

    StudentClassRubricCriteriaDTO toDTO(StudentClassRubricCriteria criteria);

    List<StudentClassRubricCriteriaDTO> toDTOList(List<StudentClassRubricCriteria> criteriaList);
}

