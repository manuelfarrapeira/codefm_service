package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.web.codefm.domain.entity.teachernotebook.RubricCriterionAssignment;
import org.web.codefm.domain.entity.teachernotebook.StudentCriteriaGroup;
import org.web.codefm.model.*;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentCriteriaGroupDTOMapperTest {

    private final StudentCriteriaGroupDTOMapper mapper = new StudentCriteriaGroupDTOMapperImpl();

    @Nested
    class ToDTO {

        @Test
        void when_all_fields_are_present_expect_student_and_rubric_criteria_mapped() {
            final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                    .id(1).classRubricId(10).rubricId(20).rubricTitle("Rubric A")
                    .criterionId(30).criterionDescription("Lo hace bien").qualification("Notable").gradeStart(7).gradeEnd(10)
                    .build();
            final StudentCriteriaGroup group = StudentCriteriaGroup.builder()
                    .studentId(100).studentName("Juan").studentSurnames("García López")
                    .rubricCriteria(List.of(assignment))
                    .build();

            final StudentCriteriaGroupDTO result = StudentCriteriaGroupDTOMapperTest.this.mapper.toDTO(group);

            assertThat(result).isNotNull();
            assertThat(result.getStudent()).isNotNull();
            assertThat(result.getStudent().getId()).isEqualTo(100);
            assertThat(result.getStudent().getName()).isEqualTo("Juan");
            assertThat(result.getStudent().getSurnames()).isEqualTo("García López");
            assertThat(result.getRubricCriteria()).hasSize(1);
            assertThat(result.getRubricCriteria().get(0).getId()).isEqualTo(1);
            assertThat(result.getRubricCriteria().get(0).getClassRubricId()).isEqualTo(10);
            assertThat(result.getRubricCriteria().get(0).getRubric().getId()).isEqualTo(20);
            assertThat(result.getRubricCriteria().get(0).getRubric().getTitle()).isEqualTo("Rubric A");
            assertThat(result.getRubricCriteria().get(0).getCriterion().getId()).isEqualTo(30);
            assertThat(result.getRubricCriteria().get(0).getCriterion().getDescription()).isEqualTo("Lo hace bien");
            assertThat(result.getRubricCriteria().get(0).getCriterion().getQualification()).isEqualTo("Notable");
            assertThat(result.getRubricCriteria().get(0).getCriterion().getGradeStart()).isEqualTo(7);
            assertThat(result.getRubricCriteria().get(0).getCriterion().getGradeEnd()).isEqualTo(10);
        }

        @Test
        void when_group_is_null_expect_null() {
            assertThat(StudentCriteriaGroupDTOMapperTest.this.mapper.toDTO(null)).isNull();
        }

        @Test
        void when_rubric_criteria_is_null_expect_null_rubric_criteria() {
            final StudentCriteriaGroup group = StudentCriteriaGroup.builder()
                    .studentId(100).studentName("Juan").studentSurnames("García")
                    .rubricCriteria(null)
                    .build();

            final StudentCriteriaGroupDTO result = StudentCriteriaGroupDTOMapperTest.this.mapper.toDTO(group);

            assertThat(result).isNotNull();
            assertThat(result.getStudent()).isNotNull();
            assertThat(result.getRubricCriteria()).isNull();
        }

        @Test
        void when_rubric_criteria_is_empty_expect_empty_rubric_criteria() {
            final StudentCriteriaGroup group = StudentCriteriaGroup.builder()
                    .studentId(100).studentName("Juan").studentSurnames("García")
                    .rubricCriteria(List.of())
                    .build();

            final StudentCriteriaGroupDTO result = StudentCriteriaGroupDTOMapperTest.this.mapper.toDTO(group);

            assertThat(result).isNotNull();
            assertThat(result.getRubricCriteria()).isEmpty();
        }

        @Test
        void when_multiple_rubric_criteria_are_present_expect_all_mapped() {
            final RubricCriterionAssignment assignment1 = RubricCriterionAssignment.builder()
                    .id(1).classRubricId(10).rubricId(20).rubricTitle("Rubric A")
                    .criterionId(30).criterionDescription("Mal").gradeStart(0).gradeEnd(4)
                    .build();
            final RubricCriterionAssignment assignment2 = RubricCriterionAssignment.builder()
                    .id(2).classRubricId(11).rubricId(21).rubricTitle("Rubric B")
                    .criterionId(31).criterionDescription("Bien").gradeStart(7).gradeEnd(10)
                    .build();
            final StudentCriteriaGroup group = StudentCriteriaGroup.builder()
                    .studentId(100).studentName("Ana").studentSurnames("López")
                    .rubricCriteria(Arrays.asList(assignment1, assignment2))
                    .build();

            final StudentCriteriaGroupDTO result = StudentCriteriaGroupDTOMapperTest.this.mapper.toDTO(group);

            assertThat(result).isNotNull();
            assertThat(result.getRubricCriteria()).hasSize(2);
            assertThat(result.getRubricCriteria().get(0).getRubric().getTitle()).isEqualTo("Rubric A");
            assertThat(result.getRubricCriteria().get(1).getRubric().getTitle()).isEqualTo("Rubric B");
        }
    }

    @Nested
    class ToDTOList {

        @Test
        void when_input_has_groups_expect_mapped_list() {
            final StudentCriteriaGroup group1 = StudentCriteriaGroup.builder()
                    .studentId(1).studentName("Juan").studentSurnames("García")
                    .rubricCriteria(List.of(RubricCriterionAssignment.builder()
                            .id(10).classRubricId(100).rubricId(20).rubricTitle("R1")
                            .criterionId(30).criterionDescription("D1").gradeStart(0).gradeEnd(4).build()))
                    .build();
            final StudentCriteriaGroup group2 = StudentCriteriaGroup.builder()
                    .studentId(2).studentName("Ana").studentSurnames("López")
                    .rubricCriteria(List.of(RubricCriterionAssignment.builder()
                            .id(11).classRubricId(101).rubricId(21).rubricTitle("R2")
                            .criterionId(31).criterionDescription("D2").gradeStart(5).gradeEnd(6).build()))
                    .build();

            final List<StudentCriteriaGroupDTO> result = StudentCriteriaGroupDTOMapperTest.this.mapper.toDTOList(
                    Arrays.asList(group1, group2));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStudent().getId()).isEqualTo(1);
            assertThat(result.get(0).getStudent().getName()).isEqualTo("Juan");
            assertThat(result.get(1).getStudent().getId()).isEqualTo(2);
            assertThat(result.get(1).getStudent().getName()).isEqualTo("Ana");
        }

        @Test
        void when_input_is_empty_expect_empty_list() {
            assertThat(StudentCriteriaGroupDTOMapperTest.this.mapper.toDTOList(List.of())).isEmpty();
        }

        @Test
        void when_input_is_null_expect_null() {
            assertThat(StudentCriteriaGroupDTOMapperTest.this.mapper.toDTOList(null)).isNull();
        }
    }

    @Nested
    class ToAssignmentDTO {

        @Test
        void when_all_fields_are_present_expect_mapped_assignment() {
            final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                    .id(5).classRubricId(50).rubricId(60).rubricTitle("Expresión oral")
                    .criterionId(70).criterionDescription("Se expresa con fluidez").qualification("Notable").gradeStart(7).gradeEnd(10)
                    .build();

            final RubricCriterionAssignmentDTO result = StudentCriteriaGroupDTOMapperTest.this.mapper.toAssignmentDTO(assignment);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(5);
            assertThat(result.getClassRubricId()).isEqualTo(50);
            assertThat(result.getRubric()).isNotNull();
            assertThat(result.getRubric().getId()).isEqualTo(60);
            assertThat(result.getRubric().getTitle()).isEqualTo("Expresión oral");
            assertThat(result.getCriterion()).isNotNull();
            assertThat(result.getCriterion().getId()).isEqualTo(70);
            assertThat(result.getCriterion().getDescription()).isEqualTo("Se expresa con fluidez");
            assertThat(result.getCriterion().getQualification()).isEqualTo("Notable");
            assertThat(result.getCriterion().getGradeStart()).isEqualTo(7);
            assertThat(result.getCriterion().getGradeEnd()).isEqualTo(10);
        }

        @Test
        void when_assignment_is_null_expect_null() {
            assertThat(StudentCriteriaGroupDTOMapperTest.this.mapper.toAssignmentDTO(null)).isNull();
        }

        @Test
        void when_optional_fields_are_null_expect_nested_null_values() {
            final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                    .id(5).classRubricId(50)
                    .rubricId(null).rubricTitle(null)
                    .criterionId(null).criterionDescription(null).gradeStart(null).gradeEnd(null)
                    .build();

            final RubricCriterionAssignmentDTO result = StudentCriteriaGroupDTOMapperTest.this.mapper.toAssignmentDTO(assignment);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(5);
            assertThat(result.getClassRubricId()).isEqualTo(50);
            assertThat(result.getRubric()).isNotNull();
            assertThat(result.getRubric().getId()).isNull();
            assertThat(result.getRubric().getTitle()).isNull();
            assertThat(result.getCriterion()).isNotNull();
            assertThat(result.getCriterion().getId()).isNull();
            assertThat(result.getCriterion().getDescription()).isNull();
            assertThat(result.getCriterion().getQualification()).isNull();
            assertThat(result.getCriterion().getGradeStart()).isNull();
            assertThat(result.getCriterion().getGradeEnd()).isNull();
        }
    }

    @Nested
    class ToStudentSummary {

        @Test
        void when_group_has_student_fields_expect_mapped_summary() {
            final StudentCriteriaGroup group = StudentCriteriaGroup.builder()
                    .studentId(42).studentName("María").studentSurnames("Pérez Sánchez")
                    .build();

            final StudentSummaryDTO result = StudentCriteriaGroupDTOMapperTest.this.mapper.toStudentSummary(group);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(42);
            assertThat(result.getName()).isEqualTo("María");
            assertThat(result.getSurnames()).isEqualTo("Pérez Sánchez");
        }
    }

    @Nested
    class ToRubricSummary {

        @Test
        void when_assignment_has_rubric_fields_expect_mapped_summary() {
            final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                    .rubricId(99).rubricTitle("Competencia lingüística")
                    .build();

            final RubricSummaryDTO result = StudentCriteriaGroupDTOMapperTest.this.mapper.toRubricSummary(assignment);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(99);
            assertThat(result.getTitle()).isEqualTo("Competencia lingüística");
        }
    }

    @Nested
    class ToCriterionSummary {

        @Test
        void when_criterion_fields_are_present_expect_mapped_summary() {
            final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                    .criterionId(77).criterionDescription("Lo hace regular").qualification("Suficiente").gradeStart(5).gradeEnd(6)
                    .build();

            final CriterionSummaryDTO result = StudentCriteriaGroupDTOMapperTest.this.mapper.toCriterionSummary(assignment);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(77);
            assertThat(result.getDescription()).isEqualTo("Lo hace regular");
            assertThat(result.getQualification()).isEqualTo("Suficiente");
            assertThat(result.getGradeStart()).isEqualTo(5);
            assertThat(result.getGradeEnd()).isEqualTo(6);
        }

        @Test
        void when_grade_range_is_zero_expect_zero_values() {
            final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                    .criterionId(1).criterionDescription("Mínimo").gradeStart(0).gradeEnd(0)
                    .build();

            final CriterionSummaryDTO result = StudentCriteriaGroupDTOMapperTest.this.mapper.toCriterionSummary(assignment);

            assertThat(result).isNotNull();
            assertThat(result.getGradeStart()).isZero();
            assertThat(result.getGradeEnd()).isZero();
        }

        @Test
        void when_grade_range_is_full_expect_full_values() {
            final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                    .criterionId(1).criterionDescription("Todo el rango").gradeStart(0).gradeEnd(10)
                    .build();

            final CriterionSummaryDTO result = StudentCriteriaGroupDTOMapperTest.this.mapper.toCriterionSummary(assignment);

            assertThat(result).isNotNull();
            assertThat(result.getGradeStart()).isZero();
            assertThat(result.getGradeEnd()).isEqualTo(10);
        }
    }
}
