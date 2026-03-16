package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricCriteriaEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillRubricCriteriaMapper {

    SkillRubricCriteria toModel(SkillRubricCriteriaEntity entity);

    List<SkillRubricCriteria> toModelList(List<SkillRubricCriteriaEntity> entities);

    SkillRubricCriteriaEntity toEntity(SkillRubricCriteria criteria);

    List<SkillRubricCriteriaEntity> toEntityList(List<SkillRubricCriteria> criteriaList);
}

