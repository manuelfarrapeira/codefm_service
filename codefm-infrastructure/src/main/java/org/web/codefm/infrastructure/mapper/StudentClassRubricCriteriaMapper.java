package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassRubricCriteriaEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentClassRubricCriteriaMapper {

    @Mapping(target = "rubricId", ignore = true)
    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "studentSurnames", ignore = true)
    @Mapping(target = "criterionDescription", ignore = true)
    @Mapping(target = "gradeStart", ignore = true)
    @Mapping(target = "gradeEnd", ignore = true)
    StudentClassRubricCriteria toModel(StudentClassRubricCriteriaEntity entity);

    List<StudentClassRubricCriteria> toModelList(List<StudentClassRubricCriteriaEntity> entities);

    StudentClassRubricCriteriaEntity toEntity(StudentClassRubricCriteria model);
}

