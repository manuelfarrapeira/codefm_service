package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.model.ExerciseGradeDTO;
import org.web.codefm.model.ExerciseStudentGradeDTO;
import org.web.codefm.model.QuarterGradesDTO;
import org.web.codefm.model.StudentGradesDTO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExerciseStudentGradeDTOMapperTest {

    private final ExerciseStudentGradeDTOMapper mapper = new ExerciseStudentGradeDTOMapper() {
        @Override
        public ExerciseStudentGradeDTO toDTO(ExerciseStudentGrade grade) {
            ExerciseStudentGradeDTO dto = new ExerciseStudentGradeDTO();
            dto.setId(grade.getId());
            dto.setStudentId(grade.getStudentId());
            dto.setExerciseId(grade.getExerciseId());
            dto.setGrade(grade.getGrade());
            dto.setDescription(grade.getDescription());
            return dto;
        }

        @Override
        public ExerciseGradeDTO toExerciseGradeDTO(ExerciseStudentGrade grade) {
            ExerciseGradeDTO dto = new ExerciseGradeDTO();
            dto.setGradeId(grade.getId());
            dto.setExerciseId(grade.getExerciseId());
            dto.setExerciseTitle(grade.getExerciseTitle());
            dto.setMaxGrade(grade.getMaxGrade());
            dto.setPercentageGrade(grade.getPercentageGrade());
            dto.setGrade(grade.getGrade());
            dto.setDescription(grade.getDescription());
            return dto;
        }
    };

    @Test
    void toGroupedByStudentDTOList_shouldGroupByStudentAndQuarterAndSubject() {
        List<ExerciseStudentGrade> grades = Arrays.asList(
                buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null),
                buildGrade(2, 1, "Juan", "García", 1, 1, "Math", 102, "Exam 2", 10, 40, 9.0, null),
                buildGrade(3, 1, "Juan", "García", 2, 2, "Science", 103, "Lab 1", 10, 50, 7.0, "Good"),
                buildGrade(4, 2, "María", "López", 1, 1, "Math", 101, "Exam 1", 10, 30, 6.0, null)
        );

        List<StudentGradesDTO> result = mapper.toGroupedByStudentDTOList(grades);

        assertNotNull(result);
        assertEquals(2, result.size());

        StudentGradesDTO student1 = result.get(0);
        assertEquals(1, student1.getStudentId());
        assertEquals("Juan", student1.getStudentName());
        assertEquals("García", student1.getStudentSurnames());
        assertEquals(2, student1.getQuarters().size());

        QuarterGradesDTO q1Student1 = student1.getQuarters().get(0);
        assertEquals(1, q1Student1.getQuarter());
        assertEquals(1, q1Student1.getSubjects().size());
        assertEquals("Math", q1Student1.getSubjects().get(0).getSubjectName());
        assertEquals(2, q1Student1.getSubjects().get(0).getExercises().size());

        QuarterGradesDTO q2Student1 = student1.getQuarters().get(1);
        assertEquals(2, q2Student1.getQuarter());
        assertEquals(1, q2Student1.getSubjects().size());
        assertEquals("Science", q2Student1.getSubjects().get(0).getSubjectName());

        StudentGradesDTO student2 = result.get(1);
        assertEquals(2, student2.getStudentId());
        assertEquals("María", student2.getStudentName());
        assertEquals(1, student2.getQuarters().size());
    }

    @Test
    void toGroupedByStudentDTOList_shouldReturnEmptyList_whenInputIsEmpty() {
        List<StudentGradesDTO> result = mapper.toGroupedByStudentDTOList(Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toGroupedByStudentDTOList_shouldHandleSingleGrade() {
        List<ExerciseStudentGrade> grades = List.of(
                buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, "Well done")
        );

        List<StudentGradesDTO> result = mapper.toGroupedByStudentDTOList(grades);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getQuarters().size());
        assertEquals(1, result.get(0).getQuarters().get(0).getSubjects().size());
        assertEquals(1, result.get(0).getQuarters().get(0).getSubjects().get(0).getExercises().size());

        ExerciseGradeDTO exercise = result.get(0).getQuarters().get(0).getSubjects().get(0).getExercises().get(0);
        assertEquals(1, exercise.getGradeId());
        assertEquals(101, exercise.getExerciseId());
        assertEquals("Exam 1", exercise.getExerciseTitle());
        assertEquals(10, exercise.getMaxGrade());
        assertEquals(30, exercise.getPercentageGrade());
        assertEquals(8.0, exercise.getGrade());
        assertEquals("Well done", exercise.getDescription());
    }

    @Test
    void toGroupedByStudentDTOList_shouldGroupMultipleSubjectsInSameQuarter() {
        List<ExerciseStudentGrade> grades = Arrays.asList(
                buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null),
                buildGrade(2, 1, "Juan", "García", 1, 2, "Science", 102, "Lab 1", 10, 40, 7.0, null)
        );

        List<StudentGradesDTO> result = mapper.toGroupedByStudentDTOList(grades);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getQuarters().size());

        QuarterGradesDTO quarter = result.get(0).getQuarters().get(0);
        assertEquals(1, quarter.getQuarter());
        assertEquals(2, quarter.getSubjects().size());
        assertEquals(1, quarter.getSubjects().get(0).getSubjectId());
        assertEquals(2, quarter.getSubjects().get(1).getSubjectId());
    }

    @Test
    void toGroupedDTOList_shouldGroupByQuarterAndSubject() {
        List<ExerciseStudentGrade> grades = Arrays.asList(
                buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null),
                buildGrade(2, 1, "Juan", "García", 1, 1, "Math", 102, "Exam 2", 10, 40, 9.0, null),
                buildGrade(3, 1, "Juan", "García", 2, 2, "Science", 103, "Lab 1", 10, 50, 7.0, null)
        );

        List<QuarterGradesDTO> result = mapper.toGroupedDTOList(grades);

        assertNotNull(result);
        assertEquals(2, result.size());

        QuarterGradesDTO q1 = result.get(0);
        assertEquals(1, q1.getQuarter());
        assertEquals(1, q1.getSubjects().size());
        assertEquals("Math", q1.getSubjects().get(0).getSubjectName());
        assertEquals(1, q1.getSubjects().get(0).getSubjectId());
        assertEquals(2, q1.getSubjects().get(0).getExercises().size());

        QuarterGradesDTO q2 = result.get(1);
        assertEquals(2, q2.getQuarter());
        assertEquals(1, q2.getSubjects().size());
        assertEquals("Science", q2.getSubjects().get(0).getSubjectName());
        assertEquals(1, q2.getSubjects().get(0).getExercises().size());
    }

    @Test
    void toGroupedDTOList_shouldReturnEmptyList_whenInputIsEmpty() {
        List<QuarterGradesDTO> result = mapper.toGroupedDTOList(Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toGroupedDTOList_shouldPreserveExerciseFields() {
        List<ExerciseStudentGrade> grades = List.of(
                buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, "Good job")
        );

        List<QuarterGradesDTO> result = mapper.toGroupedDTOList(grades);

        ExerciseGradeDTO exercise = result.get(0).getSubjects().get(0).getExercises().get(0);
        assertEquals(1, exercise.getGradeId());
        assertEquals(101, exercise.getExerciseId());
        assertEquals("Exam 1", exercise.getExerciseTitle());
        assertEquals(10, exercise.getMaxGrade());
        assertEquals(30, exercise.getPercentageGrade());
        assertEquals(8.0, exercise.getGrade());
        assertEquals("Good job", exercise.getDescription());
    }

    @Test
    void toGroupedDTOList_shouldHandleNullDescription() {
        List<ExerciseStudentGrade> grades = List.of(
                buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null)
        );

        List<QuarterGradesDTO> result = mapper.toGroupedDTOList(grades);

        ExerciseGradeDTO exercise = result.get(0).getSubjects().get(0).getExercises().get(0);
        assertNull(exercise.getDescription());
    }

    @Test
    void toGroupedByStudentDTOList_shouldPreserveInsertionOrder() {
        List<ExerciseStudentGrade> grades = Arrays.asList(
                buildGrade(1, 3, "Carlos", "Ruiz", 2, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null),
                buildGrade(2, 1, "Ana", "López", 1, 2, "Science", 102, "Lab 1", 10, 40, 9.0, null),
                buildGrade(3, 2, "Bea", "Pérez", 1, 1, "Math", 103, "Exam 2", 10, 50, 7.0, null)
        );

        List<StudentGradesDTO> result = mapper.toGroupedByStudentDTOList(grades);

        assertEquals(3, result.size());
        assertEquals(3, result.get(0).getStudentId());
        assertEquals(1, result.get(1).getStudentId());
        assertEquals(2, result.get(2).getStudentId());
    }

    @Test
    void toGroupedByStudentDTOList_shouldMapMultipleExercisesForSameSubjectAndQuarter() {
        List<ExerciseStudentGrade> grades = Arrays.asList(
                buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null),
                buildGrade(2, 1, "Juan", "García", 1, 1, "Math", 102, "Exam 2", 10, 40, 9.0, null),
                buildGrade(3, 1, "Juan", "García", 1, 1, "Math", 103, "Exam 3", 10, 30, 5.0, null)
        );

        List<StudentGradesDTO> result = mapper.toGroupedByStudentDTOList(grades);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getQuarters().size());
        assertEquals(1, result.get(0).getQuarters().get(0).getSubjects().size());
        assertEquals(3, result.get(0).getQuarters().get(0).getSubjects().get(0).getExercises().size());
    }

    private ExerciseStudentGrade buildGrade(Integer id, Integer studentId, String studentName,
                                            String studentSurnames, Integer quarter, Integer subjectId,
                                            String subjectName, Integer exerciseId, String exerciseTitle,
                                            Integer maxGrade, Integer percentageGrade, Double grade,
                                            String description) {
        return ExerciseStudentGrade.builder()
                .id(id)
                .studentId(studentId)
                .studentName(studentName)
                .studentSurnames(studentSurnames)
                .quarter(quarter)
                .subjectId(subjectId)
                .subjectName(subjectName)
                .exerciseId(exerciseId)
                .exerciseTitle(exerciseTitle)
                .maxGrade(maxGrade)
                .percentageGrade(percentageGrade)
                .grade(grade)
                .description(description)
                .build();
    }
}

