package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.model.ExerciseGradeDTO;
import org.web.codefm.model.ExerciseStudentGradeDTO;
import org.web.codefm.model.QuarterGradesDTO;
import org.web.codefm.model.StudentGradesDTO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExerciseStudentGradeDTOMapperTest {

    private final ExerciseStudentGradeDTOMapper mapper = new ExerciseStudentGradeDTOMapper() {
        @Override
        public ExerciseStudentGradeDTO toDTO(final ExerciseStudentGrade grade) {
            final ExerciseStudentGradeDTO dto = new ExerciseStudentGradeDTO();
            dto.setId(grade.getId());
            dto.setStudentId(grade.getStudentId());
            dto.setExerciseId(grade.getExerciseId());
            dto.setGrade(grade.getGrade());
            dto.setDescription(grade.getDescription());
            return dto;
        }

        @Override
        public ExerciseGradeDTO toExerciseGradeDTO(final ExerciseStudentGrade grade) {
            final ExerciseGradeDTO dto = new ExerciseGradeDTO();
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

    @Nested
    class ToGroupedByStudentDTOList {

        @Test
        void when_grades_have_multiple_students_quarters_and_subjects_expect_grouped_result() {
            final List<ExerciseStudentGrade> grades = Arrays.asList(
                    buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null),
                    buildGrade(2, 1, "Juan", "García", 1, 1, "Math", 102, "Exam 2", 10, 40, 9.0, null),
                    buildGrade(3, 1, "Juan", "García", 2, 2, "Science", 103, "Lab 1", 10, 50, 7.0, "Good"),
                    buildGrade(4, 2, "María", "López", 1, 1, "Math", 101, "Exam 1", 10, 30, 6.0, null)
            );

            final List<StudentGradesDTO> result = ExerciseStudentGradeDTOMapperTest.this.mapper.toGroupedByStudentDTOList(grades);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStudentId()).isEqualTo(1);
            assertThat(result.get(0).getStudentName()).isEqualTo("Juan");
            assertThat(result.get(0).getStudentSurnames()).isEqualTo("García");
            assertThat(result.get(0).getQuarters()).hasSize(2);
            assertThat(result.get(0).getQuarters().get(0).getQuarter()).isEqualTo(1);
            assertThat(result.get(0).getQuarters().get(0).getSubjects()).hasSize(1);
            assertThat(result.get(0).getQuarters().get(0).getSubjects().get(0).getSubjectName()).isEqualTo("Math");
            assertThat(result.get(0).getQuarters().get(0).getSubjects().get(0).getExercises()).hasSize(2);
            assertThat(result.get(0).getQuarters().get(1).getQuarter()).isEqualTo(2);
            assertThat(result.get(0).getQuarters().get(1).getSubjects()).hasSize(1);
            assertThat(result.get(0).getQuarters().get(1).getSubjects().get(0).getSubjectName()).isEqualTo("Science");
            assertThat(result.get(1).getStudentId()).isEqualTo(2);
            assertThat(result.get(1).getStudentName()).isEqualTo("María");
            assertThat(result.get(1).getQuarters()).hasSize(1);
        }

        @Test
        void when_input_is_empty_expect_empty_list() {
            assertThat(ExerciseStudentGradeDTOMapperTest.this.mapper.toGroupedByStudentDTOList(Collections.emptyList())).isEmpty();
        }

        @Test
        void when_input_has_single_grade_expect_single_grouped_entry() {
            final List<ExerciseStudentGrade> grades = List.of(
                    buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, "Well done")
            );

            final List<StudentGradesDTO> result = ExerciseStudentGradeDTOMapperTest.this.mapper.toGroupedByStudentDTOList(grades);
            final ExerciseGradeDTO exercise = result.get(0).getQuarters().get(0).getSubjects().get(0).getExercises().get(0);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getQuarters()).hasSize(1);
            assertThat(result.get(0).getQuarters().get(0).getSubjects()).hasSize(1);
            assertThat(result.get(0).getQuarters().get(0).getSubjects().get(0).getExercises()).hasSize(1);
            assertThat(exercise.getGradeId()).isEqualTo(1);
            assertThat(exercise.getExerciseId()).isEqualTo(101);
            assertThat(exercise.getExerciseTitle()).isEqualTo("Exam 1");
            assertThat(exercise.getMaxGrade()).isEqualTo(10);
            assertThat(exercise.getPercentageGrade()).isEqualTo(30);
            assertThat(exercise.getGrade()).isEqualTo(8.0);
            assertThat(exercise.getDescription()).isEqualTo("Well done");
        }

        @Test
        void when_same_quarter_has_multiple_subjects_expect_subjects_grouped() {
            final List<ExerciseStudentGrade> grades = Arrays.asList(
                    buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null),
                    buildGrade(2, 1, "Juan", "García", 1, 2, "Science", 102, "Lab 1", 10, 40, 7.0, null)
            );

            final List<StudentGradesDTO> result = ExerciseStudentGradeDTOMapperTest.this.mapper.toGroupedByStudentDTOList(grades);
            final QuarterGradesDTO quarter = result.get(0).getQuarters().get(0);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getQuarters()).hasSize(1);
            assertThat(quarter.getQuarter()).isEqualTo(1);
            assertThat(quarter.getSubjects()).hasSize(2);
            assertThat(quarter.getSubjects().get(0).getSubjectId()).isEqualTo(1);
            assertThat(quarter.getSubjects().get(1).getSubjectId()).isEqualTo(2);
        }

        @Test
        void when_insertion_order_changes_expect_student_order_preserved() {
            final List<ExerciseStudentGrade> grades = Arrays.asList(
                    buildGrade(1, 3, "Carlos", "Ruiz", 2, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null),
                    buildGrade(2, 1, "Ana", "López", 1, 2, "Science", 102, "Lab 1", 10, 40, 9.0, null),
                    buildGrade(3, 2, "Bea", "Pérez", 1, 1, "Math", 103, "Exam 2", 10, 50, 7.0, null)
            );

            final List<StudentGradesDTO> result = ExerciseStudentGradeDTOMapperTest.this.mapper.toGroupedByStudentDTOList(grades);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getStudentId()).isEqualTo(3);
            assertThat(result.get(1).getStudentId()).isEqualTo(1);
            assertThat(result.get(2).getStudentId()).isEqualTo(2);
        }

        @Test
        void when_same_subject_and_quarter_has_multiple_exercises_expect_all_exercises_grouped() {
            final List<ExerciseStudentGrade> grades = Arrays.asList(
                    buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null),
                    buildGrade(2, 1, "Juan", "García", 1, 1, "Math", 102, "Exam 2", 10, 40, 9.0, null),
                    buildGrade(3, 1, "Juan", "García", 1, 1, "Math", 103, "Exam 3", 10, 30, 5.0, null)
            );

            final List<StudentGradesDTO> result = ExerciseStudentGradeDTOMapperTest.this.mapper.toGroupedByStudentDTOList(grades);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getQuarters()).hasSize(1);
            assertThat(result.get(0).getQuarters().get(0).getSubjects()).hasSize(1);
            assertThat(result.get(0).getQuarters().get(0).getSubjects().get(0).getExercises()).hasSize(3);
        }
    }

    @Nested
    class ToGroupedDTOList {

        @Test
        void when_grades_have_multiple_quarters_and_subjects_expect_grouped_result() {
            final List<ExerciseStudentGrade> grades = Arrays.asList(
                    buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null),
                    buildGrade(2, 1, "Juan", "García", 1, 1, "Math", 102, "Exam 2", 10, 40, 9.0, null),
                    buildGrade(3, 1, "Juan", "García", 2, 2, "Science", 103, "Lab 1", 10, 50, 7.0, null)
            );

            final List<QuarterGradesDTO> result = ExerciseStudentGradeDTOMapperTest.this.mapper.toGroupedDTOList(grades);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getQuarter()).isEqualTo(1);
            assertThat(result.get(0).getSubjects()).hasSize(1);
            assertThat(result.get(0).getSubjects().get(0).getSubjectName()).isEqualTo("Math");
            assertThat(result.get(0).getSubjects().get(0).getSubjectId()).isEqualTo(1);
            assertThat(result.get(0).getSubjects().get(0).getExercises()).hasSize(2);
            assertThat(result.get(1).getQuarter()).isEqualTo(2);
            assertThat(result.get(1).getSubjects()).hasSize(1);
            assertThat(result.get(1).getSubjects().get(0).getSubjectName()).isEqualTo("Science");
            assertThat(result.get(1).getSubjects().get(0).getExercises()).hasSize(1);
        }

        @Test
        void when_input_is_empty_expect_empty_list() {
            assertThat(ExerciseStudentGradeDTOMapperTest.this.mapper.toGroupedDTOList(Collections.emptyList())).isEmpty();
        }

        @Test
        void when_exercise_fields_are_present_expect_fields_preserved() {
            final List<ExerciseStudentGrade> grades = List.of(
                    buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, "Good job")
            );

            final List<QuarterGradesDTO> result = ExerciseStudentGradeDTOMapperTest.this.mapper.toGroupedDTOList(grades);
            final ExerciseGradeDTO exercise = result.get(0).getSubjects().get(0).getExercises().get(0);

            assertThat(exercise.getGradeId()).isEqualTo(1);
            assertThat(exercise.getExerciseId()).isEqualTo(101);
            assertThat(exercise.getExerciseTitle()).isEqualTo("Exam 1");
            assertThat(exercise.getMaxGrade()).isEqualTo(10);
            assertThat(exercise.getPercentageGrade()).isEqualTo(30);
            assertThat(exercise.getGrade()).isEqualTo(8.0);
            assertThat(exercise.getDescription()).isEqualTo("Good job");
        }

        @Test
        void when_description_is_null_expect_null_description() {
            final List<ExerciseStudentGrade> grades = List.of(
                    buildGrade(1, 1, "Juan", "García", 1, 1, "Math", 101, "Exam 1", 10, 30, 8.0, null)
            );

            final List<QuarterGradesDTO> result = ExerciseStudentGradeDTOMapperTest.this.mapper.toGroupedDTOList(grades);
            final ExerciseGradeDTO exercise = result.get(0).getSubjects().get(0).getExercises().get(0);

            assertThat(exercise.getDescription()).isNull();
        }
    }

    private ExerciseStudentGrade buildGrade(final Integer id,
                                            final Integer studentId,
                                            final String studentName,
                                            final String studentSurnames,
                                            final Integer quarter,
                                            final Integer subjectId,
                                            final String subjectName,
                                            final Integer exerciseId,
                                            final String exerciseTitle,
                                            final Integer maxGrade,
                                            final Integer percentageGrade,
                                            final Double grade,
                                            final String description) {
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
