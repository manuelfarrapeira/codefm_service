package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.model.ClassWithSubjectsDTO;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ClassDTOMapper.class, SubjectDTOMapper.class})
public interface ClassWithSubjectsDTOMapper {

    @Mapping(source = "classData", target = "classData")
    @Mapping(source = "subjects", target = "subjects")
    ClassWithSubjectsDTO toDTO(ClassWithSubjects classWithSubjects);

    List<ClassWithSubjectsDTO> toDTOList(List<ClassWithSubjects> classesWithSubjects);
}

