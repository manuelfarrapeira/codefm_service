package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.model.SubjectDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubjectDTOMapper {

    SubjectDTO toDTO(Subject subject);

    List<SubjectDTO> toDTOList(List<Subject> subjects);
}
