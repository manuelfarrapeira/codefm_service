package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SkillRubricService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillRubricUseCaseImplTest {

    @Mock
    private SkillRubricService skillRubricService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    @InjectMocks
    private SkillRubricUseCaseImpl skillRubricUseCase;

    private static final Integer SKILL_ID = 10;
    private static final Integer RUBRIC_ID = 100;

    @Test
    void getRubricsBySkillId_shouldDelegateToService() {
        final List<SkillRubric> expected = List.of(SkillRubric.builder().id(RUBRIC_ID).title("R").skillId(SKILL_ID).build());
        when(this.skillRubricService.getRubricsBySkillId(SKILL_ID)).thenReturn(expected);

        final List<SkillRubric> result = this.skillRubricUseCase.getRubricsBySkillId(SKILL_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(this.skillRubricService).getRubricsBySkillId(SKILL_ID);
    }

    @Test
    void createRubric_shouldDelegateToService() {
        final SkillRubric rubric = SkillRubric.builder().title("New").build();
        final SkillRubric created = SkillRubric.builder().id(RUBRIC_ID).title("New").skillId(SKILL_ID).build();
        when(this.skillRubricService.createRubric(eq(SKILL_ID), any(SkillRubric.class))).thenReturn(created);

        final SkillRubric result = this.skillRubricUseCase.createRubric(SKILL_ID, rubric);

        assertNotNull(result);
        assertEquals(RUBRIC_ID, result.getId());
    }

    @Test
    void updateRubric_shouldDelegateToService() {
        final SkillRubric rubric = SkillRubric.builder().title("Updated").build();
        final SkillRubric updated = SkillRubric.builder().id(RUBRIC_ID).title("Updated").skillId(SKILL_ID).build();
        when(this.skillRubricService.updateRubric(eq(RUBRIC_ID), any(SkillRubric.class))).thenReturn(updated);

        final SkillRubric result = this.skillRubricUseCase.updateRubric(RUBRIC_ID, rubric);

        assertNotNull(result);
        assertEquals("Updated", result.getTitle());
    }

    @Test
    void deleteRubric_shouldCallCascadeBeforeService() {
        doNothing().when(this.cascadeSoftDeleteService).cascadeDeleteChildrenOfRubric(RUBRIC_ID);
        doNothing().when(this.skillRubricService).deleteRubric(RUBRIC_ID);

        this.skillRubricUseCase.deleteRubric(RUBRIC_ID);

        final var order = inOrder(this.cascadeSoftDeleteService, this.skillRubricService);
        order.verify(this.cascadeSoftDeleteService).cascadeDeleteChildrenOfRubric(RUBRIC_ID);
        order.verify(this.skillRubricService).deleteRubric(RUBRIC_ID);
    }

    @Test
    void getCriteriaByRubricId_shouldDelegateToService() {
        final List<SkillRubricCriteria> expected = List.of(
                SkillRubricCriteria.builder().id(1).description("A").gradeStart(0).gradeEnd(4).build()
        );
        when(this.skillRubricService.getCriteriaByRubricId(RUBRIC_ID)).thenReturn(expected);

        final List<SkillRubricCriteria> result = this.skillRubricUseCase.getCriteriaByRubricId(RUBRIC_ID);

        assertEquals(1, result.size());
        verify(this.skillRubricService).getCriteriaByRubricId(RUBRIC_ID);
    }

    @Test
    void createCriterion_shouldDelegateToService() {
        final SkillRubricCriteria criterion = SkillRubricCriteria.builder().description("A").gradeStart(0).gradeEnd(4).build();
        final SkillRubricCriteria created = SkillRubricCriteria.builder().id(1).description("A").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(4).build();
        when(this.skillRubricService.createCriterion(eq(RUBRIC_ID), any())).thenReturn(created);

        final SkillRubricCriteria result = this.skillRubricUseCase.createCriterion(RUBRIC_ID, criterion);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void updateCriterion_shouldDelegateToService() {
        final SkillRubricCriteria criterion = SkillRubricCriteria.builder().description("Updated").gradeStart(0).gradeEnd(5).build();
        final SkillRubricCriteria updated = SkillRubricCriteria.builder().id(1).description("Updated").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(5).build();
        when(this.skillRubricService.updateCriterion(eq(RUBRIC_ID), eq(1), any())).thenReturn(updated);

        final SkillRubricCriteria result = this.skillRubricUseCase.updateCriterion(RUBRIC_ID, 1, criterion);

        assertNotNull(result);
        assertEquals("Updated", result.getDescription());
    }

    @Test
    void deleteCriterion_shouldDelegateToService() {
        doNothing().when(this.skillRubricService).deleteCriterion(RUBRIC_ID, 1);

        this.skillRubricUseCase.deleteCriterion(RUBRIC_ID, 1);

        verify(this.skillRubricService).deleteCriterion(RUBRIC_ID, 1);
    }
}

