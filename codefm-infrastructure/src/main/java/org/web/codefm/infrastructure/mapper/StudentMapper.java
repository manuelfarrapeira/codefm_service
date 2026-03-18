package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentEntity;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface StudentMapper {

    @Mapping(target = "classIds", ignore = true)
    Student toModel(StudentEntity entity);

    List<Student> toModelList(List<StudentEntity> entities);

    StudentEntity toEntity(Student student);
}

