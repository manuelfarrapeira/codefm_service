package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SkillRubricService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillRubricUseCaseImplTest {

    private SkillRubricUseCaseImpl skillRubricUseCase;

    @Mock
    private SkillRubricService skillRubricService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    private static final Integer SKILL_ID = 10;
    private static final Integer RUBRIC_ID = 100;

    @BeforeEach
    void beforeEach() {
        skillRubricUseCase = new SkillRubricUseCaseImpl(skillRubricService, cascadeSoftDeleteService);
    }

    @Nested
    class GetRubricsBySkillId {

        @Test
        void when_rubrics_found_expect_delegated_to_service() {
            final List<SkillRubric> expected = List.of(SkillRubric.builder().id(RUBRIC_ID).title("R").skillId(SKILL_ID).build());
            when(skillRubricService.getRubricsBySkillId(SKILL_ID)).thenReturn(expected);

            final List<SkillRubric> result = skillRubricUseCase.getRubricsBySkillId(SKILL_ID);

            assertThat(result).isNotNull().hasSize(1);
            verify(skillRubricService).getRubricsBySkillId(SKILL_ID);
        }
    }

    @Nested
    class CreateRubric {

        @Test
        void when_creating_rubric_expect_delegated_to_service() {
            final SkillRubric rubric = SkillRubric.builder().title("New").build();
            final SkillRubric created = SkillRubric.builder().id(RUBRIC_ID).title("New").skillId(SKILL_ID).build();
            when(skillRubricService.createRubric(eq(SKILL_ID), any(SkillRubric.class))).thenReturn(created);

            final SkillRubric result = skillRubricUseCase.createRubric(SKILL_ID, rubric);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(RUBRIC_ID);
        }
    }

    @Nested
    class UpdateRubric {

        @Test
        void when_updating_rubric_expect_delegated_to_service() {
            final SkillRubric rubric = SkillRubric.builder().title("Updated").build();
            final SkillRubric updated = SkillRubric.builder().id(RUBRIC_ID).title("Updated").skillId(SKILL_ID).build();
            when(skillRubricService.updateRubric(eq(RUBRIC_ID), any(SkillRubric.class))).thenReturn(updated);

            final SkillRubric result = skillRubricUseCase.updateRubric(RUBRIC_ID, rubric);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated");
        }
    }

    @Nested
    class DeleteRubric {

        @Test
        void when_deleting_rubric_expect_cascade_before_service() {
            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfRubric(RUBRIC_ID);
            doNothing().when(skillRubricService).deleteRubric(RUBRIC_ID);

            skillRubricUseCase.deleteRubric(RUBRIC_ID);

            final var order = inOrder(cascadeSoftDeleteService, skillRubricService);
            order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfRubric(RUBRIC_ID);
            order.verify(skillRubricService).deleteRubric(RUBRIC_ID);
        }
    }

    @Nested
    class GetCriteriaByRubricId {

        @Test
        void when_criteria_found_expect_delegated_to_service() {
            final List<SkillRubricCriteria> expected = List.of(
                    SkillRubricCriteria.builder().id(1).description("A").gradeStart(0).gradeEnd(4).build());
            when(skillRubricService.getCriteriaByRubricId(RUBRIC_ID)).thenReturn(expected);

            final List<SkillRubricCriteria> result = skillRubricUseCase.getCriteriaByRubricId(RUBRIC_ID);

            assertThat(result).hasSize(1);
            verify(skillRubricService).getCriteriaByRubricId(RUBRIC_ID);
        }
    }

    @Nested
    class CreateCriterion {

        @Test
        void when_creating_criterion_expect_delegated_to_service() {
            final SkillRubricCriteria criterion = SkillRubricCriteria.builder().description("A").gradeStart(0).gradeEnd(4).build();
            final SkillRubricCriteria created = SkillRubricCriteria.builder().id(1).description("A").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(4).build();
            when(skillRubricService.createCriterion(eq(RUBRIC_ID), any())).thenReturn(created);

            final SkillRubricCriteria result = skillRubricUseCase.createCriterion(RUBRIC_ID, criterion);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
        }
    }

    @Nested
    class UpdateCriterion {

        @Test
        void when_updating_criterion_expect_delegated_to_service() {
            final SkillRubricCriteria criterion = SkillRubricCriteria.builder().description("Updated").gradeStart(0).gradeEnd(5).build();
            final SkillRubricCriteria updated = SkillRubricCriteria.builder().id(1).description("Updated").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(5).build();
            when(skillRubricService.updateCriterion(eq(RUBRIC_ID), eq(1), any())).thenReturn(updated);

            final SkillRubricCriteria result = skillRubricUseCase.updateCriterion(RUBRIC_ID, 1, criterion);

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEqualTo("Updated");
        }
    }

    @Nested
    class DeleteCriterion {

        @Test
        void when_deleting_criterion_expect_cascade_before_service() {
            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfSkillRubricCriteria(1);
            doNothing().when(skillRubricService).deleteCriterion(RUBRIC_ID, 1);

            skillRubricUseCase.deleteCriterion(RUBRIC_ID, 1);

            final var order = inOrder(cascadeSoftDeleteService, skillRubricService);
            order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfSkillRubricCriteria(1);
            order.verify(skillRubricService).deleteCriterion(RUBRIC_ID, 1);
        }
    }
}

