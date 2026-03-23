package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentGroupEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SavedStudentGroupMapper {

    SavedStudentGroup toModel(StudentGroupEntity entity);

    List<SavedStudentGroup> toModelList(List<StudentGroupEntity> entities);

    StudentGroupEntity toEntity(SavedStudentGroup group);
}
