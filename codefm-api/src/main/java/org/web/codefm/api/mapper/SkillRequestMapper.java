package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.model.SkillRequestDTO;

@Mapper(componentModel = "spring")
public interface SkillRequestMapper {

    Skill toDomain(SkillRequestDTO dto);
}

