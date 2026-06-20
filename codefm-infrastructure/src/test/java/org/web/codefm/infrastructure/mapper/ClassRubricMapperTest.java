package org.web.codefm.infrastructure.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassRubricEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ClassRubricMapperTest {

    private final ClassRubricMapper mapper = new ClassRubricMapperImpl();

    @Nested
    class ToModel {

        @Test
        void when_all_fields_are_present_expect_all_fields_mapped() {
            final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, null);

            final ClassRubric result = mapper.toModel(entity);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getClassId()).isEqualTo(10);
            assertThat(result.getRubricId()).isEqualTo(50);
            assertThat(result.getDeletionDate()).isNull();
        }

        @Test
        void when_deletion_date_is_present_expect_deletion_date_mapped() {
            final LocalDate deletionDate = LocalDate.of(2026, 3, 15);
            final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, deletionDate);

            final ClassRubric result = mapper.toModel(entity);

            assertThat(result).isNotNull();
            assertThat(result.getDeletionDate()).isEqualTo(deletionDate);
        }

        @Test
        void when_entity_contains_only_persistent_fields_expect_transient_fields_ignored() {
            final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, null);

            final ClassRubric result = mapper.toModel(entity);

            assertThat(result).isNotNull();
            assertThat(result.getRubricTitle()).isNull();
            assertThat(result.getSkillId()).isNull();
            assertThat(result.getCriteria()).isNull();
        }

        @Test
        void when_entity_is_null_expect_null_returned() {
            final ClassRubric result = mapper.toModel(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    class ToModelList {

        @Test
        void when_entities_are_provided_expect_all_entities_mapped() {
            final ClassRubricEntity entity1 = new ClassRubricEntity(1, 10, 50, null);
            final ClassRubricEntity entity2 = new ClassRubricEntity(2, 10, 60, null);

            final List<ClassRubric> result = mapper.toModelList(Arrays.asList(entity1, entity2));

            assertThat(result).isNotNull().hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1);
            assertThat(result.get(0).getRubricId()).isEqualTo(50);
            assertThat(result.get(1).getId()).isEqualTo(2);
            assertThat(result.get(1).getRubricId()).isEqualTo(60);
        }

        @Test
        void when_input_is_empty_expect_empty_list_returned() {
            final List<ClassRubric> result = mapper.toModelList(List.of());

            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        void when_input_is_null_expect_null_returned() {
            final List<ClassRubric> result = mapper.toModelList(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    class ToEntity {

        @Test
        void when_all_fields_are_present_expect_all_fields_mapped() {
            final ClassRubric model = ClassRubric.builder()
                    .id(1).classId(10).rubricId(50).deletionDate(null).build();

            final ClassRubricEntity result = mapper.toEntity(model);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getClassId()).isEqualTo(10);
            assertThat(result.getRubricId()).isEqualTo(50);
            assertThat(result.getDeletionDate()).isNull();
        }

        @Test
        void when_deletion_date_is_present_expect_deletion_date_mapped() {
            final LocalDate deletionDate = LocalDate.of(2026, 3, 15);
            final ClassRubric model = ClassRubric.builder()
                    .id(1).classId(10).rubricId(50).deletionDate(deletionDate).build();

            final ClassRubricEntity result = mapper.toEntity(model);

            assertThat(result).isNotNull();
            assertThat(result.getDeletionDate()).isEqualTo(deletionDate);
        }

        @Test
        void when_model_is_null_expect_null_returned() {
            final ClassRubricEntity result = mapper.toEntity(null);

            assertThat(result).isNull();
        }

        @Test
        void when_transient_domain_fields_are_set_expect_only_persistent_fields_mapped() {
            final ClassRubric model = ClassRubric.builder()
                    .id(1).classId(10).rubricId(50)
                    .rubricTitle("Rubric Title").skillId(5).criteria(List.of())
                    .build();

            final ClassRubricEntity result = mapper.toEntity(model);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getClassId()).isEqualTo(10);
            assertThat(result.getRubricId()).isEqualTo(50);
        }

        @Test
        void when_entity_is_mapped_to_model_and_back_expect_persistent_fields_preserved() {
            final ClassRubricEntity original = new ClassRubricEntity(1, 10, 50, LocalDate.of(2026, 1, 1));

            final ClassRubric model = mapper.toModel(original);
            final ClassRubricEntity roundTrip = mapper.toEntity(model);

            assertThat(roundTrip.getId()).isEqualTo(original.getId());
            assertThat(roundTrip.getClassId()).isEqualTo(original.getClassId());
            assertThat(roundTrip.getRubricId()).isEqualTo(original.getRubricId());
            assertThat(roundTrip.getDeletionDate()).isEqualTo(original.getDeletionDate());
        }
    }
}
