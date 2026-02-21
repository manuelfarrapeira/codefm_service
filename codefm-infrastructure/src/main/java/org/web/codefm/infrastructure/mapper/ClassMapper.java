package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassEntity;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface ClassMapper {
    Class toModel(ClassEntity entity);

    List<Class> toModelList(List<ClassEntity> entities);

    ClassEntity toEntity(Class clazz);
}
