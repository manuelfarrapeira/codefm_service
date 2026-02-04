package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    Subject toModel(SubjectEntity entity);

    List<Subject> toModelList(List<SubjectEntity> entities);

    SubjectEntity toEntity(Subject subject);
}
