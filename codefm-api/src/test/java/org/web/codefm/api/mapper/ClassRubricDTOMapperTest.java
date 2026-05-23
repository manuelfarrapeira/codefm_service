package org.web.codefm.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.model.ClassRubricDTO;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ClassRubricDTOMapperTest {

    @Spy
    private SkillRubricCriteriaDTOMapperImpl skillRubricCriteriaDTOMapper;

    private ClassRubricDTOMapperImpl mapper;

    @BeforeEach
    void beforeEach() {
        this.mapper = new ClassRubricDTOMapperImpl();
        ReflectionTestUtils.setField(this.mapper, "skillRubricCriteriaDTOMapper", this.skillRubricCriteriaDTOMapper);
    }

    @Nested
    class ToDTO {

        @Test
        void when_all_fields_are_present_expect_mapped_dto() {
            final SkillRubricCriteria criterion = SkillRubricCriteria.builder()
                    .id(10).description("Lo hace bien").gradeStart(7).gradeEnd(10).build();
            final ClassRubric classRubric = ClassRubric.builder()
                    .id(1).classId(20).rubricId(30).rubricTitle("Rubric A").skillId(5)
                    .criteria(List.of(criterion))
                    .build();

            final ClassRubricDTO result = ClassRubricDTOMapperTest.this.mapper.toDTO(classRubric);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getClassId()).isEqualTo(20);
            assertThat(result.getRubricId()).isEqualTo(30);
            assertThat(result.getRubricTitle()).isEqualTo("Rubric A");
            assertThat(result.getSkillId()).isEqualTo(5);
            assertThat(result.getCriteria()).hasSize(1);
            assertThat(result.getCriteria().get(0).getId()).isEqualTo(10);
            assertThat(result.getCriteria().get(0).getDescription()).isEqualTo("Lo hace bien");
            assertThat(result.getCriteria().get(0).getGradeStart()).isEqualTo(7);
            assertThat(result.getCriteria().get(0).getGradeEnd()).isEqualTo(10);
        }

        @Test
        void when_input_is_null_expect_null() {
            assertThat(ClassRubricDTOMapperTest.this.mapper.toDTO(null)).isNull();
        }

        @Test
        void when_criteria_is_null_expect_null_criteria() {
            final ClassRubric classRubric = ClassRubric.builder()
                    .id(1).classId(20).rubricId(30).rubricTitle("Rubric A").skillId(5)
                    .criteria(null)
                    .build();

            final ClassRubricDTO result = ClassRubricDTOMapperTest.this.mapper.toDTO(classRubric);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getCriteria()).isNull();
        }

        @Test
        void when_criteria_is_empty_expect_empty_criteria() {
            final ClassRubric classRubric = ClassRubric.builder()
                    .id(1).classId(20).rubricId(30).rubricTitle("Rubric A").skillId(5)
                    .criteria(List.of())
                    .build();

            final ClassRubricDTO result = ClassRubricDTOMapperTest.this.mapper.toDTO(classRubric);

            assertThat(result).isNotNull();
            assertThat(result.getCriteria()).isEmpty();
        }

        @Test
        void when_multiple_criteria_are_present_expect_all_mapped() {
            final SkillRubricCriteria criterion1 = SkillRubricCriteria.builder()
                    .id(10).description("Mal").gradeStart(0).gradeEnd(4).build();
            final SkillRubricCriteria criterion2 = SkillRubricCriteria.builder()
                    .id(11).description("Regular").gradeStart(5).gradeEnd(6).build();
            final SkillRubricCriteria criterion3 = SkillRubricCriteria.builder()
                    .id(12).description("Bien").gradeStart(7).gradeEnd(10).build();
            final ClassRubric classRubric = ClassRubric.builder()
                    .id(1).classId(20).rubricId(30).rubricTitle("Rubric A").skillId(5)
                    .criteria(Arrays.asList(criterion1, criterion2, criterion3))
                    .build();

            final ClassRubricDTO result = ClassRubricDTOMapperTest.this.mapper.toDTO(classRubric);

            assertThat(result).isNotNull();
            assertThat(result.getCriteria()).hasSize(3);
            assertThat(result.getCriteria().get(0).getDescription()).isEqualTo("Mal");
            assertThat(result.getCriteria().get(1).getDescription()).isEqualTo("Regular");
            assertThat(result.getCriteria().get(2).getDescription()).isEqualTo("Bien");
        }

        @Test
        void when_optional_fields_are_null_expect_null_optional_values() {
            final ClassRubric classRubric = ClassRubric.builder()
                    .id(1).classId(20).rubricId(30)
                    .rubricTitle(null).skillId(null).criteria(null)
                    .build();

            final ClassRubricDTO result = ClassRubricDTOMapperTest.this.mapper.toDTO(classRubric);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getClassId()).isEqualTo(20);
            assertThat(result.getRubricId()).isEqualTo(30);
            assertThat(result.getRubricTitle()).isNull();
            assertThat(result.getSkillId()).isNull();
            assertThat(result.getCriteria()).isNull();
        }
    }

    @Nested
    class ToDTOList {

        @Test
        void when_input_has_class_rubrics_expect_mapped_list() {
            final ClassRubric rubric1 = ClassRubric.builder()
                    .id(1).classId(20).rubricId(30).rubricTitle("Rubric A").skillId(5)
                    .criteria(List.of(SkillRubricCriteria.builder()
                            .id(10).description("Desc A").gradeStart(0).gradeEnd(4).build()))
                    .build();
            final ClassRubric rubric2 = ClassRubric.builder()
                    .id(2).classId(20).rubricId(40).rubricTitle("Rubric B").skillId(6)
                    .criteria(List.of(SkillRubricCriteria.builder()
                            .id(20).description("Desc B").gradeStart(5).gradeEnd(10).build()))
                    .build();

            final List<ClassRubricDTO> result = ClassRubricDTOMapperTest.this.mapper.toDTOList(Arrays.asList(rubric1, rubric2));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1);
            assertThat(result.get(0).getRubricTitle()).isEqualTo("Rubric A");
            assertThat(result.get(0).getCriteria()).hasSize(1);
            assertThat(result.get(1).getId()).isEqualTo(2);
            assertThat(result.get(1).getRubricTitle()).isEqualTo("Rubric B");
            assertThat(result.get(1).getCriteria()).hasSize(1);
        }

        @Test
        void when_input_is_empty_expect_empty_list() {
            assertThat(ClassRubricDTOMapperTest.this.mapper.toDTOList(List.of())).isEmpty();
        }

        @Test
        void when_input_is_null_expect_null() {
            assertThat(ClassRubricDTOMapperTest.this.mapper.toDTOList(null)).isNull();
        }
    }
}
