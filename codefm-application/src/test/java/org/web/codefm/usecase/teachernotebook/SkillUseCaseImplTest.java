package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SkillService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillUseCaseImplTest {

    @Mock
    private SkillService skillService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    @InjectMocks
    private SkillUseCaseImpl skillUseCase;

    private static final Integer TEACHER_ID = 1;

    @Test
    void getSkillsByTeacher_shouldReturnSkills_whenTeacherHasSkills() {
        final List<Skill> expectedSkills = Arrays.asList(
                Skill.builder().id(1).title("Skill One").description("Critical thinking").teacherId(TEACHER_ID).build(),
                Skill.builder().id(2).title("Skill Two").description("Problem solving").teacherId(TEACHER_ID).build()
        );
        when(skillService.getSkillsByTeacher()).thenReturn(expectedSkills);

        final List<Skill> result = skillUseCase.getSkillsByTeacher();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(skillService, times(1)).getSkillsByTeacher();
    }

    @Test
    void getSkillsByTeacher_shouldReturnEmptyList_whenTeacherHasNoSkills() {
        when(skillService.getSkillsByTeacher()).thenReturn(Collections.emptyList());

        final List<Skill> result = skillUseCase.getSkillsByTeacher();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(skillService, times(1)).getSkillsByTeacher();
    }

    @Test
    void createSkill_shouldCallServiceWithSkill() {
        final Skill skillToCreate = Skill.builder().title("New Skill").description("New Skill Desc").build();
        final Skill createdSkill = Skill.builder().id(1).title("New Skill").description("New Skill Desc").teacherId(TEACHER_ID).build();
        when(skillService.createSkill(any(Skill.class))).thenReturn(createdSkill);

        final Skill result = skillUseCase.createSkill(skillToCreate);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("New Skill Desc", result.getDescription());
        verify(skillService, times(1)).createSkill(skillToCreate);
    }

    @Test
    void updateSkill_shouldCallServiceWithSkillIdAndSkill() {
        final Integer skillId = 1;
        final Skill skillToUpdate = Skill.builder().title("Updated Title").description("Updated Desc").build();
        final Skill updatedSkill = Skill.builder().id(skillId).teacherId(TEACHER_ID).title("Updated Title").description("Updated Desc").build();
        when(skillService.updateSkill(eq(skillId), any(Skill.class))).thenReturn(updatedSkill);

        final Skill result = skillUseCase.updateSkill(skillId, skillToUpdate);

        assertNotNull(result);
        assertEquals(skillId, result.getId());
        assertEquals("Updated Desc", result.getDescription());
        verify(skillService, times(1)).updateSkill(eq(skillId), any(Skill.class));
    }

    @Test
    void softDeleteSkill_shouldCallCascadeBeforeService() {
        final Integer skillId = 1;
        doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfSkill(skillId);
        doNothing().when(skillService).softDeleteSkill(skillId);

        skillUseCase.softDeleteSkill(skillId);

        var order = inOrder(cascadeSoftDeleteService, skillService);
        order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfSkill(skillId);
        order.verify(skillService).softDeleteSkill(skillId);
    }
}

