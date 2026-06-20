package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroupMember;
import org.web.codefm.model.SavedStudentGroupDTO;
import org.web.codefm.model.SavedStudentGroupMemberDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SavedStudentGroupDTOMapper {

    SavedStudentGroupDTO toDTO(SavedStudentGroup group);

    List<SavedStudentGroupDTO> toDTOList(List<SavedStudentGroup> groups);

    SavedStudentGroupMemberDTO toMemberDTO(SavedStudentGroupMember member);

    List<SavedStudentGroupMemberDTO> toMemberDTOList(List<SavedStudentGroupMember> members);
}
