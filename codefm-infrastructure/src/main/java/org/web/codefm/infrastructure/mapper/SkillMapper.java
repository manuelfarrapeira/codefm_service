package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    Skill toModel(SkillEntity entity);

    List<Skill> toModelList(List<SkillEntity> entities);

    SkillEntity toEntity(Skill skill);
}

