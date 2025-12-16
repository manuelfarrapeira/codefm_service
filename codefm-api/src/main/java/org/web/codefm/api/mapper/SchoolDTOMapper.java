package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.model.SchoolDTO;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ClassDTOMapper.class})
public interface SchoolDTOMapper {

    SchoolDTO toDTO(School school);

    List<SchoolDTO> toDTOList(List<School> schools);
}
