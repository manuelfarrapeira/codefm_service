package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentDocumentEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupAssignmentDocumentMapper {

    GroupAssignmentDocument toModel(GroupAssignmentDocumentEntity entity);

    List<GroupAssignmentDocument> toModelList(List<GroupAssignmentDocumentEntity> entities);

    GroupAssignmentDocumentEntity toEntity(GroupAssignmentDocument model);
}

