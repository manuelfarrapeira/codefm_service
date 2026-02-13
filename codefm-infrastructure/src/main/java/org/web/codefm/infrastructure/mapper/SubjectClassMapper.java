package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.SubjectClass;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectClassEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubjectClassMapper {

    SubjectClass toModel(SubjectClassEntity entity);

    List<SubjectClass> toModelList(List<SubjectClassEntity> entities);

    SubjectClassEntity toEntity(SubjectClass subjectClass);

    List<SubjectClassEntity> toEntityList(List<SubjectClass> subjectClasses);
}

