package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.model.SubjectRequestDTO;

@Mapper(componentModel = "spring")
public interface SubjectRequestMapper {

    Subject toDomain(SubjectRequestDTO dto);
}
