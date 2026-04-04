package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentGradeEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupAssignmentGradeMapper {

    GroupAssignmentGrade toModel(GroupAssignmentGradeEntity entity);

    List<GroupAssignmentGrade> toModelList(List<GroupAssignmentGradeEntity> entities);

    GroupAssignmentGradeEntity toEntity(GroupAssignmentGrade model);
}

