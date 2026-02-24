package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseStudentGradeEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExerciseStudentGradeMapper {

    ExerciseStudentGrade toModel(ExerciseStudentGradeEntity entity);

    List<ExerciseStudentGrade> toModelList(List<ExerciseStudentGradeEntity> entities);

    ExerciseStudentGradeEntity toEntity(ExerciseStudentGrade model);
}

