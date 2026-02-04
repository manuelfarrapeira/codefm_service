package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassEntity;

@Mapper(componentModel = "spring")
public interface StudentClassMapper {
    StudentClass toModel(StudentClassEntity entity);

    StudentClassEntity toEntity(StudentClass studentClass);
}

