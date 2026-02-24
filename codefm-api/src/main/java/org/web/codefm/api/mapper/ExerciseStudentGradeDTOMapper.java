package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.model.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ExerciseStudentGradeDTOMapper {

    ExerciseStudentGradeDTO toDTO(ExerciseStudentGrade grade);

    @Mapping(target = "exerciseId", source = "exerciseId")
    @Mapping(target = "exerciseTitle", source = "exerciseTitle")
    @Mapping(target = "maxGrade", source = "maxGrade")
    @Mapping(target = "percentageGrade", source = "percentageGrade")
    @Mapping(target = "grade", source = "grade")
    @Mapping(target = "description", source = "description")
    ExerciseGradeDTO toExerciseGradeDTO(ExerciseStudentGrade grade);

    default List<StudentGradesDTO> toGroupedByStudentDTOList(List<ExerciseStudentGrade> grades) {
        Map<Integer, List<ExerciseStudentGrade>> byStudent = grades.stream()
                .collect(Collectors.groupingBy(
                        ExerciseStudentGrade::getStudentId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<StudentGradesDTO> result = new ArrayList<>();

        byStudent.forEach((studentId, studentGrades) -> {
            StudentGradesDTO studentDTO = new StudentGradesDTO();
            studentDTO.setStudentId(studentId);
            studentDTO.setStudentName(studentGrades.get(0).getStudentName());
            studentDTO.setStudentSurnames(studentGrades.get(0).getStudentSurnames());
            studentDTO.setQuarters(buildQuarterGrades(studentGrades));
            result.add(studentDTO);
        });

        return result;
    }

    default List<QuarterGradesDTO> toGroupedDTOList(List<ExerciseStudentGrade> grades) {
        return buildQuarterGrades(grades);
    }

    private List<QuarterGradesDTO> buildQuarterGrades(List<ExerciseStudentGrade> grades) {
        Map<Integer, Map<Integer, List<ExerciseStudentGrade>>> byQuarterAndSubject = grades.stream()
                .collect(Collectors.groupingBy(
                        ExerciseStudentGrade::getQuarter,
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                ExerciseStudentGrade::getSubjectId,
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                ));

        List<QuarterGradesDTO> quarterList = new ArrayList<>();

        byQuarterAndSubject.forEach((quarter, subjectMap) -> {
            QuarterGradesDTO quarterDTO = new QuarterGradesDTO();
            quarterDTO.setQuarter(quarter);

            List<SubjectGradesDTO> subjectList = new ArrayList<>();

            subjectMap.forEach((subjectId, subjectGrades) -> {
                SubjectGradesDTO subjectDTO = new SubjectGradesDTO();
                subjectDTO.setSubjectId(subjectId);
                subjectDTO.setSubjectName(subjectGrades.get(0).getSubjectName());
                subjectDTO.setExercises(subjectGrades.stream().map(this::toExerciseGradeDTO).toList());
                subjectList.add(subjectDTO);
            });

            quarterDTO.setSubjects(subjectList);
            quarterList.add(quarterDTO);
        });

        return quarterList;
    }
}

