package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.model.ExerciseStudentGradeRequestDTO;
import org.web.codefm.model.ExerciseStudentGradeUpdateRequestDTO;

@Mapper(componentModel = "spring")
public interface ExerciseStudentGradeRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exerciseId", ignore = true)
    @Mapping(target = "deletionDate", ignore = true)
    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "studentSurnames", ignore = true)
    @Mapping(target = "exerciseTitle", ignore = true)
    @Mapping(target = "subjectId", ignore = true)
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "quarter", ignore = true)
    @Mapping(target = "maxGrade", ignore = true)
    @Mapping(target = "percentageGrade", ignore = true)
    ExerciseStudentGrade toDomain(ExerciseStudentGradeRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "studentId", ignore = true)
    @Mapping(target = "exerciseId", ignore = true)
    @Mapping(target = "deletionDate", ignore = true)
    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "studentSurnames", ignore = true)
    @Mapping(target = "exerciseTitle", ignore = true)
    @Mapping(target = "subjectId", ignore = true)
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "quarter", ignore = true)
    @Mapping(target = "maxGrade", ignore = true)
    @Mapping(target = "percentageGrade", ignore = true)
    ExerciseStudentGrade toDomainForUpdate(ExerciseStudentGradeUpdateRequestDTO dto);
}

