package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.model.SkillRubricCriteriaDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillRubricCriteriaDTOMapper {

    SkillRubricCriteriaDTO toDTO(SkillRubricCriteria criteria);

    List<SkillRubricCriteriaDTO> toDTOList(List<SkillRubricCriteria> criteriaList);
}

