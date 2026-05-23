package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.domain.exception.teachernotebook.SkillForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SkillNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SkillValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.SkillRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;
    @Mock
    private MessageSource messageSource;
    @Mock
    private SessionUser sessionUser;

    private SkillServiceImpl skillService;

    private static final Integer TEACHER_ID = 1;

    @BeforeEach
    void beforeEach() {
        skillService = new SkillServiceImpl(skillRepository, messageSource, sessionUser);
    }

    @Nested
    class GetSkillsByTeacher {

        @Test
        void when_teacher_has_active_skills_expect_return_skills() {
            final List<Skill> expectedSkills = Arrays.asList(
                    Skill.builder().id(1).title("Skill One").description("Critical thinking").teacherId(TEACHER_ID).build(),
                    Skill.builder().id(2).title("Skill Two").description("Problem solving").teacherId(TEACHER_ID).build()
            );
            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(skillRepository.findByTeacherId(TEACHER_ID)).thenReturn(expectedSkills);

            final List<Skill> actualSkills = skillService.getSkillsByTeacher();

            assertThat(actualSkills).isNotNull().hasSize(2);
            verify(skillRepository, times(1)).findByTeacherId(TEACHER_ID);
        }

        @Test
        void when_teacher_has_no_active_skills_expect_return_empty_list() {
            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(skillRepository.findByTeacherId(TEACHER_ID)).thenReturn(Collections.emptyList());

            final List<Skill> actualSkills = skillService.getSkillsByTeacher();

            assertThat(actualSkills).isNotNull().isEmpty();
        }
    }

    @Nested
    class CreateSkill {

        @Test
        void when_data_is_valid_expect_save_skill_and_set_teacher_id() {
            final Skill skillToCreate = Skill.builder().title("Valid Title").description("Valid Description").build();
            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> {
                Skill s = inv.getArgument(0);
                return Skill.builder().id(1).title(s.getTitle()).description(s.getDescription()).teacherId(s.getTeacherId()).build();
            });

            final Skill createdSkill = skillService.createSkill(skillToCreate);

            assertThat(createdSkill).isNotNull();
            assertThat(createdSkill.getTitle()).isEqualTo("Valid Title");
            assertThat(createdSkill.getDescription()).isEqualTo("Valid Description");
            assertThat(createdSkill.getTeacherId()).isEqualTo(TEACHER_ID);
        }

        @ParameterizedTest
        @MethodSource("invalidFieldValues")
        void when_title_is_invalid_expect_throw_validation_exception(String title) {
            final Skill skill = Skill.builder().title(title).description("Valid Description").build();
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_TITLE_REQUIRED), eq(null), any(Locale.class)))
                    .thenReturn("error");
            lenient().when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_TITLE_MIN_LENGTH), eq(null), any(Locale.class)))
                    .thenReturn("error");

            final ThrowingCallable action = () -> skillService.createSkill(skill);
            final SkillValidationException ex = catchThrowableOfType(action, SkillValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> "title".equals(e.getParam()));
            verify(skillRepository, never()).save(any());
        }

        @ParameterizedTest
        @MethodSource("invalidFieldValues")
        void when_description_is_invalid_expect_throw_validation_exception(String description) {
            final Skill skill = Skill.builder().title("Valid Title").description(description).build();
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_DESCRIPTION_REQUIRED), eq(null), any(Locale.class)))
                    .thenReturn("error");
            lenient().when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_DESCRIPTION_MIN_LENGTH), eq(null), any(Locale.class)))
                    .thenReturn("error");

            final ThrowingCallable action = () -> skillService.createSkill(skill);
            final SkillValidationException ex = catchThrowableOfType(action, SkillValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> "description".equals(e.getParam()));
            verify(skillRepository, never()).save(any());
        }

        @Test
        void when_title_is_too_short_expect_throw_validation_exception() {
            final Skill skill = Skill.builder().title("Abc").description("Valid Description").build();
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_TITLE_MIN_LENGTH), eq(null), any(Locale.class)))
                    .thenReturn("min length error");

            final ThrowingCallable action = () -> skillService.createSkill(skill);
            final SkillValidationException ex = catchThrowableOfType(action, SkillValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> "title".equals(e.getParam()));
        }

        @Test
        void when_description_is_too_short_expect_throw_validation_exception() {
            final Skill skill = Skill.builder().title("Valid Title").description("Abc").build();
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_DESCRIPTION_MIN_LENGTH), eq(null), any(Locale.class)))
                    .thenReturn("min length error");

            final ThrowingCallable action = () -> skillService.createSkill(skill);
            final SkillValidationException ex = catchThrowableOfType(action, SkillValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> "description".equals(e.getParam()));
        }

        static Stream<String> invalidFieldValues() {
            return Stream.of(null, "", "   ");
        }
    }

    @Nested
    class UpdateSkill {

        @Test
        void when_data_is_valid_and_owned_by_teacher_expect_update_skill() {
            final Integer skillId = 1;
            final Skill existing = Skill.builder().id(skillId).teacherId(TEACHER_ID).title("Old Title").description("Old Desc").build();
            final Skill saved = Skill.builder().id(skillId).teacherId(TEACHER_ID).title("New Title").description("New Desc").build();

            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(skillRepository.findById(skillId)).thenReturn(Optional.of(existing));
            when(skillRepository.save(any(Skill.class))).thenReturn(saved);

            final Skill result = skillService.updateSkill(skillId, Skill.builder().title("New Title").description("New Desc").build());

            assertThat(result.getTitle()).isEqualTo("New Title");
            assertThat(result.getDescription()).isEqualTo("New Desc");
            verify(skillRepository).save(existing);
        }

        @Test
        void when_skill_does_not_exist_expect_throw_not_found_exception() {
            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(skillRepository.findById(1)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.SKILL_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

            final ThrowingCallable action = () -> skillService.updateSkill(1, Skill.builder().title("Valid Title").description("Valid Desc").build());
            assertThatThrownBy(action).isInstanceOf(SkillNotFoundException.class);
        }

        @Test
        void when_skill_belongs_to_another_teacher_expect_throw_forbidden_exception() {
            final Skill existing = Skill.builder().id(1).teacherId(999).title("Old").description("Old").build();
            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(skillRepository.findById(1)).thenReturn(Optional.of(existing));
            when(messageSource.getMessage(eq(MessageKeys.SKILL_FORBIDDEN), any(), any(Locale.class))).thenReturn("forbidden");

            final ThrowingCallable action = () -> skillService.updateSkill(1, Skill.builder().title("Valid Title").description("Valid Desc").build());
            assertThatThrownBy(action).isInstanceOf(SkillForbiddenException.class);
        }
    }

    @Nested
    class SoftDeleteSkill {

        @Test
        void when_skill_exists_and_owned_by_teacher_expect_call_repository() {
            final Skill skill = Skill.builder().id(1).teacherId(TEACHER_ID).title("Title").description("Desc").build();
            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(skillRepository.findById(1)).thenReturn(Optional.of(skill));
            when(skillRepository.softDeleteSkill(1, TEACHER_ID)).thenReturn(skill);

            skillService.softDeleteSkill(1);

            verify(skillRepository).softDeleteSkill(1, TEACHER_ID);
        }

        @Test
        void when_skill_does_not_exist_expect_throw_not_found_exception() {
            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(skillRepository.findById(1)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.SKILL_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

            final ThrowingCallable action = () -> skillService.softDeleteSkill(1);
            assertThatThrownBy(action).isInstanceOf(SkillNotFoundException.class);
            verify(skillRepository, never()).softDeleteSkill(any(), any());
        }

        @Test
        void when_skill_belongs_to_another_teacher_expect_throw_forbidden_exception() {
            final Skill skill = Skill.builder().id(1).teacherId(999).title("T").description("D").build();
            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(skillRepository.findById(1)).thenReturn(Optional.of(skill));
            when(messageSource.getMessage(eq(MessageKeys.SKILL_FORBIDDEN), any(), any(Locale.class))).thenReturn("forbidden");

            final ThrowingCallable action = () -> skillService.softDeleteSkill(1);
            assertThatThrownBy(action).isInstanceOf(SkillForbiddenException.class);
            verify(skillRepository, never()).softDeleteSkill(any(), any());
        }
    }
}

