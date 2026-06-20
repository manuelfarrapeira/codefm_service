package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.model.SkillRubricCriteriaRequestDTO;
import org.web.codefm.model.SkillRubricRequestDTO;

@Mapper(componentModel = "spring")
public interface SkillRubricRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skillId", ignore = true)
    @Mapping(target = "deletionDate", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    SkillRubric toDomain(SkillRubricRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rubricId", ignore = true)
    @Mapping(target = "deletionDate", ignore = true)
    SkillRubricCriteria criteriaToDomain(SkillRubricCriteriaRequestDTO dto);
}

