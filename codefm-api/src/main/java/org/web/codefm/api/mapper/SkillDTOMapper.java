package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.model.SkillDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillDTOMapper {

    SkillDTO toDTO(Skill skill);

    List<SkillDTO> toDTOList(List<Skill> skills);
}

