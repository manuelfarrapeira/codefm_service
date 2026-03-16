package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.model.SkillRubricDTO;

import java.util.List;

@Mapper(componentModel = "spring", uses = SkillRubricCriteriaDTOMapper.class)
public interface SkillRubricDTOMapper {

    SkillRubricDTO toDTO(SkillRubric rubric);

    List<SkillRubricDTO> toDTOList(List<SkillRubric> rubrics);
}

