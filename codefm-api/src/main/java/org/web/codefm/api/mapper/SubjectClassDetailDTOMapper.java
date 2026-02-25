package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.SubjectClassDetail;
import org.web.codefm.model.SubjectClassDetailDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubjectClassDetailDTOMapper {

    SubjectClassDetailDTO toDTO(SubjectClassDetail subjectClassDetail);

    List<SubjectClassDetailDTO> toDTOList(List<SubjectClassDetail> subjectClassDetails);
}

