package org.web.codefm.infrastructure.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassRubricCriteriaEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StudentClassRubricCriteriaMapperTest {

    private final StudentClassRubricCriteriaMapper mapper = new StudentClassRubricCriteriaMapperImpl();

    @Nested
    class ToModel {

        @Test
        void when_all_fields_are_present_expect_persistent_fields_mapped() {
            final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);

            final StudentClassRubricCriteria result = mapper.toModel(entity);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getClassRubricId()).isEqualTo(100);
            assertThat(result.getStudentId()).isEqualTo(200);
            assertThat(result.getCriterionId()).isEqualTo(300);
            assertThat(result.getDeletionDate()).isNull();
        }

        @Test
        void when_deletion_date_is_present_expect_deletion_date_mapped() {
            final LocalDate deletionDate = LocalDate.of(2026, 3, 15);
            final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, deletionDate);

            final StudentClassRubricCriteria result = mapper.toModel(entity);

            assertThat(result).isNotNull();
            assertThat(result.getDeletionDate()).isEqualTo(deletionDate);
        }

        @Test
        void when_entity_contains_only_persistent_fields_expect_enrichment_fields_ignored() {
            final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);

            final StudentClassRubricCriteria result = mapper.toModel(entity);

            assertThat(result).isNotNull();
            assertThat(result.getRubricId()).isNull();
            assertThat(result.getStudentName()).isNull();
            assertThat(result.getStudentSurnames()).isNull();
            assertThat(result.getCriterionDescription()).isNull();
            assertThat(result.getQualification()).isNull();
            assertThat(result.getGradeStart()).isNull();
            assertThat(result.getGradeEnd()).isNull();
        }

        @Test
        void when_entity_is_null_expect_null_returned() {
            final StudentClassRubricCriteria result = mapper.toModel(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    class ToModelList {

        @Test
        void when_entities_are_provided_expect_all_entities_mapped() {
            final StudentClassRubricCriteriaEntity entity1 = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
            final StudentClassRubricCriteriaEntity entity2 = new StudentClassRubricCriteriaEntity(2, 101, 201, 301, null);

            final List<StudentClassRubricCriteria> result = mapper.toModelList(Arrays.asList(entity1, entity2));

            assertThat(result).isNotNull().hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1);
            assertThat(result.get(0).getClassRubricId()).isEqualTo(100);
            assertThat(result.get(1).getId()).isEqualTo(2);
            assertThat(result.get(1).getClassRubricId()).isEqualTo(101);
        }

        @Test
        void when_input_is_empty_expect_empty_list_returned() {
            final List<StudentClassRubricCriteria> result = mapper.toModelList(List.of());

            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        void when_input_is_null_expect_null_returned() {
            final List<StudentClassRubricCriteria> result = mapper.toModelList(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    class ToEntity {

        @Test
        void when_persistent_fields_are_present_expect_persistent_fields_mapped() {
            final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(100).studentId(200).criterionId(300).deletionDate(null).build();

            final StudentClassRubricCriteriaEntity result = mapper.toEntity(model);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getClassRubricId()).isEqualTo(100);
            assertThat(result.getStudentId()).isEqualTo(200);
            assertThat(result.getCriterionId()).isEqualTo(300);
            assertThat(result.getDeletionDate()).isNull();
        }

        @Test
        void when_deletion_date_is_present_expect_deletion_date_mapped() {
            final LocalDate deletionDate = LocalDate.of(2026, 3, 15);
            final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(100).studentId(200).criterionId(300).deletionDate(deletionDate).build();

            final StudentClassRubricCriteriaEntity result = mapper.toEntity(model);

            assertThat(result).isNotNull();
            assertThat(result.getDeletionDate()).isEqualTo(deletionDate);
        }

        @Test
        void when_model_is_null_expect_null_returned() {
            final StudentClassRubricCriteriaEntity result = mapper.toEntity(null);

            assertThat(result).isNull();
        }

        @Test
        void when_domain_enrichment_fields_are_set_expect_only_persistent_fields_mapped() {
            final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(100).studentId(200).criterionId(300)
                    .rubricId(50).rubricTitle("Rubric").studentName("Juan").studentSurnames("García")
                    .criterionDescription("Lo hace bien").qualification("Notable").gradeStart(7).gradeEnd(10)
                    .build();

            final StudentClassRubricCriteriaEntity result = mapper.toEntity(model);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getClassRubricId()).isEqualTo(100);
            assertThat(result.getStudentId()).isEqualTo(200);
            assertThat(result.getCriterionId()).isEqualTo(300);
        }

        @Test
        void when_entity_is_mapped_to_model_and_back_expect_persistent_fields_preserved() {
            final StudentClassRubricCriteriaEntity original =
                    new StudentClassRubricCriteriaEntity(1, 100, 200, 300, LocalDate.of(2026, 1, 1));

            final StudentClassRubricCriteria model = mapper.toModel(original);
            final StudentClassRubricCriteriaEntity roundTrip = mapper.toEntity(model);

            assertThat(roundTrip.getId()).isEqualTo(original.getId());
            assertThat(roundTrip.getClassRubricId()).isEqualTo(original.getClassRubricId());
            assertThat(roundTrip.getStudentId()).isEqualTo(original.getStudentId());
            assertThat(roundTrip.getCriterionId()).isEqualTo(original.getCriterionId());
            assertThat(roundTrip.getDeletionDate()).isEqualTo(original.getDeletionDate());
        }
    }
}
