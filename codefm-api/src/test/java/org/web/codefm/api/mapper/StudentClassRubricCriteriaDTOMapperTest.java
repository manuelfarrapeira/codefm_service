package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.model.StudentClassRubricCriteriaDTO;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentClassRubricCriteriaDTOMapperTest {

    private final StudentClassRubricCriteriaDTOMapper mapper = new StudentClassRubricCriteriaDTOMapperImpl();

    @Nested
    class ToDTO {

        @Test
        void when_all_fields_are_present_expect_mapped_dto() {
            final StudentClassRubricCriteria criteria = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(10).rubricId(20).studentId(30)
                    .studentName("Juan").studentSurnames("García López")
                    .criterionId(40).criterionDescription("Lo hace bien").qualification("Notable")
                    .gradeStart(7).gradeEnd(10)
                    .build();

            final StudentClassRubricCriteriaDTO result = StudentClassRubricCriteriaDTOMapperTest.this.mapper.toDTO(criteria);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getClassRubricId()).isEqualTo(10);
            assertThat(result.getRubricId()).isEqualTo(20);
            assertThat(result.getStudentId()).isEqualTo(30);
            assertThat(result.getStudentName()).isEqualTo("Juan");
            assertThat(result.getStudentSurnames()).isEqualTo("García López");
            assertThat(result.getCriterionId()).isEqualTo(40);
            assertThat(result.getCriterionDescription()).isEqualTo("Lo hace bien");
            assertThat(result.getQualification()).isEqualTo("Notable");
            assertThat(result.getGradeStart()).isEqualTo(7);
            assertThat(result.getGradeEnd()).isEqualTo(10);
        }

        @Test
        void when_input_is_null_expect_null() {
            assertThat(StudentClassRubricCriteriaDTOMapperTest.this.mapper.toDTO(null)).isNull();
        }

        @Test
        void when_optional_fields_are_null_expect_null_optional_values() {
            final StudentClassRubricCriteria criteria = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(10).studentId(30).criterionId(40)
                    .rubricId(null).studentName(null).studentSurnames(null)
                    .criterionDescription(null).qualification(null).gradeStart(null).gradeEnd(null)
                    .build();

            final StudentClassRubricCriteriaDTO result = StudentClassRubricCriteriaDTOMapperTest.this.mapper.toDTO(criteria);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getClassRubricId()).isEqualTo(10);
            assertThat(result.getStudentId()).isEqualTo(30);
            assertThat(result.getCriterionId()).isEqualTo(40);
            assertThat(result.getRubricId()).isNull();
            assertThat(result.getStudentName()).isNull();
            assertThat(result.getStudentSurnames()).isNull();
            assertThat(result.getCriterionDescription()).isNull();
            assertThat(result.getQualification()).isNull();
            assertThat(result.getGradeStart()).isNull();
            assertThat(result.getGradeEnd()).isNull();
        }
    }

    @Nested
    class ToDTOList {

        @Test
        void when_input_has_criteria_expect_mapped_list() {
            final StudentClassRubricCriteria criteria1 = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(10).rubricId(20).studentId(30)
                    .studentName("Juan").studentSurnames("García")
                    .criterionId(40).criterionDescription("Mal").gradeStart(0).gradeEnd(4)
                    .build();
            final StudentClassRubricCriteria criteria2 = StudentClassRubricCriteria.builder()
                    .id(2).classRubricId(11).rubricId(21).studentId(31)
                    .studentName("Ana").studentSurnames("López")
                    .criterionId(41).criterionDescription("Bien").gradeStart(7).gradeEnd(10)
                    .build();

            final List<StudentClassRubricCriteriaDTO> result = StudentClassRubricCriteriaDTOMapperTest.this.mapper.toDTOList(
                    Arrays.asList(criteria1, criteria2));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1);
            assertThat(result.get(0).getStudentName()).isEqualTo("Juan");
            assertThat(result.get(0).getCriterionDescription()).isEqualTo("Mal");
            assertThat(result.get(1).getId()).isEqualTo(2);
            assertThat(result.get(1).getStudentName()).isEqualTo("Ana");
            assertThat(result.get(1).getCriterionDescription()).isEqualTo("Bien");
        }

        @Test
        void when_input_is_empty_expect_empty_list() {
            assertThat(StudentClassRubricCriteriaDTOMapperTest.this.mapper.toDTOList(List.of())).isEmpty();
        }

        @Test
        void when_input_is_null_expect_null() {
            assertThat(StudentClassRubricCriteriaDTOMapperTest.this.mapper.toDTOList(null)).isNull();
        }
    }
}
