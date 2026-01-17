package org.web.codefm.api.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.model.ClassDTO;
import org.web.codefm.model.ClassRequestDTO;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ClassDTOMapper {
    ClassDTO toDTO(Class clazz);

    List<ClassDTO> toDTOList(List<Class> classes);

    Class toDomain(ClassRequestDTO dto);
}
