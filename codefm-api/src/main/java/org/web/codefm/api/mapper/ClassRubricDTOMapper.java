package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.model.ClassRubricDTO;

import java.util.List;

@Mapper(componentModel = "spring", uses = SkillRubricCriteriaDTOMapper.class)
public interface ClassRubricDTOMapper {

    ClassRubricDTO toDTO(ClassRubric classRubric);

    List<ClassRubricDTO> toDTOList(List<ClassRubric> classRubrics);
}

