package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentEntity;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface StudentMapper {

    Student toModel(StudentEntity entity);

    List<Student> toModelList(List<StudentEntity> entities);

    StudentEntity toEntity(Student student);
}

