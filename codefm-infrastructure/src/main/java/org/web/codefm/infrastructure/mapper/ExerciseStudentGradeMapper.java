package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseStudentGradeEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExerciseStudentGradeMapper {

    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "studentSurnames", ignore = true)
    @Mapping(target = "exerciseTitle", ignore = true)
    @Mapping(target = "subjectId", ignore = true)
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "quarter", ignore = true)
    @Mapping(target = "maxGrade", ignore = true)
    @Mapping(target = "percentageGrade", ignore = true)
    ExerciseStudentGrade toModel(ExerciseStudentGradeEntity entity);

    List<ExerciseStudentGrade> toModelList(List<ExerciseStudentGradeEntity> entities);

    ExerciseStudentGradeEntity toEntity(ExerciseStudentGrade model);
}

