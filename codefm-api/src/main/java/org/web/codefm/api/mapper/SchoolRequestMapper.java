package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.model.CreateSchoolRequestDTO;

@Mapper(componentModel = "spring")
public interface SchoolRequestMapper {

    School toDomain(CreateSchoolRequestDTO dto);
}
