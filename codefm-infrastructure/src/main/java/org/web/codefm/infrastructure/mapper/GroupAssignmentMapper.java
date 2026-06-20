package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupAssignmentMapper {

    GroupAssignment toModel(GroupAssignmentEntity entity);

    List<GroupAssignment> toModelList(List<GroupAssignmentEntity> entities);

    GroupAssignmentEntity toEntity(GroupAssignment model);
}

