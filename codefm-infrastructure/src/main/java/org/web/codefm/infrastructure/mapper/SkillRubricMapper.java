package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillRubricMapper {

    SkillRubric toModel(SkillRubricEntity entity);

    List<SkillRubric> toModelList(List<SkillRubricEntity> entities);

    SkillRubricEntity toEntity(SkillRubric rubric);
}

