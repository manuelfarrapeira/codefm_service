package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SkillService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillUseCaseImplTest {

    private SkillUseCaseImpl skillUseCase;

    @Mock
    private SkillService skillService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    private static final Integer TEACHER_ID = 1;

    @BeforeEach
    void beforeEach() {
        skillUseCase = new SkillUseCaseImpl(skillService, cascadeSoftDeleteService);
    }

    @Nested
    class GetSkillsByTeacher {

        @Test
        void when_teacher_has_skills_expect_list_returned() {
            final List<Skill> expected = List.of(
                    Skill.builder().id(1).title("Skill One").description("Critical thinking").teacherId(TEACHER_ID).build(),
                    Skill.builder().id(2).title("Skill Two").description("Problem solving").teacherId(TEACHER_ID).build());
            when(skillService.getSkillsByTeacher()).thenReturn(expected);

            final List<Skill> result = skillUseCase.getSkillsByTeacher();

            assertThat(result).isNotNull().hasSize(2);
            verify(skillService).getSkillsByTeacher();
        }

        @Test
        void when_teacher_has_no_skills_expect_empty_list() {
            when(skillService.getSkillsByTeacher()).thenReturn(List.of());

            final List<Skill> result = skillUseCase.getSkillsByTeacher();

            assertThat(result).isEmpty();
            verify(skillService).getSkillsByTeacher();
        }
    }

    @Nested
    class CreateSkill {

        @Test
        void when_creating_skill_expect_delegated_to_service() {
            final Skill skillToCreate = Skill.builder().title("New Skill").description("New Skill Desc").build();
            final Skill createdSkill = Skill.builder().id(1).title("New Skill").description("New Skill Desc").teacherId(TEACHER_ID).build();
            when(skillService.createSkill(any(Skill.class))).thenReturn(createdSkill);

            final Skill result = skillUseCase.createSkill(skillToCreate);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getDescription()).isEqualTo("New Skill Desc");
            verify(skillService).createSkill(skillToCreate);
        }
    }

    @Nested
    class UpdateSkill {

        @Test
        void when_updating_skill_expect_delegated_to_service() {
            final Integer skillId = 1;
            final Skill skillToUpdate = Skill.builder().title("Updated Title").description("Updated Desc").build();
            final Skill updatedSkill = Skill.builder().id(skillId).teacherId(TEACHER_ID).title("Updated Title").description("Updated Desc").build();
            when(skillService.updateSkill(eq(skillId), any(Skill.class))).thenReturn(updatedSkill);

            final Skill result = skillUseCase.updateSkill(skillId, skillToUpdate);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(skillId);
            assertThat(result.getDescription()).isEqualTo("Updated Desc");
            verify(skillService).updateSkill(eq(skillId), any(Skill.class));
        }
    }

    @Nested
    class SoftDeleteSkill {

        @Test
        void when_deleting_skill_expect_cascade_before_service() {
            final Integer skillId = 1;
            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfSkill(skillId);
            doNothing().when(skillService).softDeleteSkill(skillId);

            skillUseCase.softDeleteSkill(skillId);

            final var order = inOrder(cascadeSoftDeleteService, skillService);
            order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfSkill(skillId);
            order.verify(skillService).softDeleteSkill(skillId);
        }
    }
}
