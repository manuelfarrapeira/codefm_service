package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
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

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private SkillServiceImpl skillService;

    private static final Integer TEACHER_ID = 1;

    @Test
    void getSkillsByTeacher_shouldReturnSkills_whenTeacherHasActiveSkills() {
        final List<Skill> expectedSkills = Arrays.asList(
                Skill.builder().id(1).title("Skill One").description("Critical thinking").teacherId(TEACHER_ID).build(),
                Skill.builder().id(2).title("Skill Two").description("Problem solving").teacherId(TEACHER_ID).build()
        );
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(skillRepository.findByTeacherId(TEACHER_ID)).thenReturn(expectedSkills);

        final List<Skill> actualSkills = skillService.getSkillsByTeacher();

        assertNotNull(actualSkills);
        assertEquals(2, actualSkills.size());
        verify(skillRepository, times(1)).findByTeacherId(TEACHER_ID);
    }

    @Test
    void getSkillsByTeacher_shouldReturnEmptyList_whenTeacherHasNoActiveSkills() {
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(skillRepository.findByTeacherId(TEACHER_ID)).thenReturn(Collections.emptyList());

        final List<Skill> actualSkills = skillService.getSkillsByTeacher();

        assertNotNull(actualSkills);
        assertTrue(actualSkills.isEmpty());
    }

    @Test
    void createSkill_shouldSaveSkillAndSetTeacherId_whenDataIsValid() {
        final Skill skillToCreate = Skill.builder().title("Valid Title").description("Valid Description").build();
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> {
            Skill s = inv.getArgument(0);
            return Skill.builder().id(1).title(s.getTitle()).description(s.getDescription()).teacherId(s.getTeacherId()).build();
        });

        final Skill createdSkill = skillService.createSkill(skillToCreate);

        assertNotNull(createdSkill);
        assertEquals("Valid Title", createdSkill.getTitle());
        assertEquals("Valid Description", createdSkill.getDescription());
        assertEquals(TEACHER_ID, createdSkill.getTeacherId());
    }

    @ParameterizedTest
    @MethodSource("invalidFieldValues")
    void createSkill_shouldThrowValidationException_whenTitleIsInvalid(String title) {
        final Skill skill = Skill.builder().title(title).description("Valid Description").build();
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_TITLE_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("error");
        lenient().when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_TITLE_MIN_LENGTH), eq(null), any(Locale.class)))
                .thenReturn("error");

        final SkillValidationException ex = assertThrows(SkillValidationException.class,
                () -> skillService.createSkill(skill));

        assertTrue(ex.getErrors().stream().anyMatch(e -> "title".equals(e.getParam())));
        verify(skillRepository, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("invalidFieldValues")
    void createSkill_shouldThrowValidationException_whenDescriptionIsInvalid(String description) {
        final Skill skill = Skill.builder().title("Valid Title").description(description).build();
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_DESCRIPTION_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("error");
        lenient().when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_DESCRIPTION_MIN_LENGTH), eq(null), any(Locale.class)))
                .thenReturn("error");

        final SkillValidationException ex = assertThrows(SkillValidationException.class,
                () -> skillService.createSkill(skill));

        assertTrue(ex.getErrors().stream().anyMatch(e -> "description".equals(e.getParam())));
        verify(skillRepository, never()).save(any());
    }

    @Test
    void createSkill_shouldThrowValidationException_whenTitleIsTooShort() {
        final Skill skill = Skill.builder().title("Abc").description("Valid Description").build();
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_TITLE_MIN_LENGTH), eq(null), any(Locale.class)))
                .thenReturn("min length error");

        final SkillValidationException ex = assertThrows(SkillValidationException.class,
                () -> skillService.createSkill(skill));

        assertTrue(ex.getErrors().stream().anyMatch(e -> "title".equals(e.getParam())));
    }

    @Test
    void createSkill_shouldThrowValidationException_whenDescriptionIsTooShort() {
        final Skill skill = Skill.builder().title("Valid Title").description("Abc").build();
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.SKILL_VALIDATION_DESCRIPTION_MIN_LENGTH), eq(null), any(Locale.class)))
                .thenReturn("min length error");

        final SkillValidationException ex = assertThrows(SkillValidationException.class,
                () -> skillService.createSkill(skill));

        assertTrue(ex.getErrors().stream().anyMatch(e -> "description".equals(e.getParam())));
    }

    @Test
    void updateSkill_shouldUpdateSkill_whenDataIsValidAndOwnedByTeacher() {
        final Integer skillId = 1;
        final Skill existing = Skill.builder().id(skillId).teacherId(TEACHER_ID).title("Old Title").description("Old Desc").build();
        final Skill saved = Skill.builder().id(skillId).teacherId(TEACHER_ID).title("New Title").description("New Desc").build();

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(existing));
        when(skillRepository.save(any(Skill.class))).thenReturn(saved);

        final Skill result = skillService.updateSkill(skillId, Skill.builder().title("New Title").description("New Desc").build());

        assertEquals("New Title", result.getTitle());
        assertEquals("New Desc", result.getDescription());
        verify(skillRepository).save(existing);
    }

    @Test
    void updateSkill_shouldThrowNotFoundException_whenSkillDoesNotExist() {
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(skillRepository.findById(1)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.SKILL_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

        assertThrows(SkillNotFoundException.class,
                () -> skillService.updateSkill(1, Skill.builder().title("Valid Title").description("Valid Desc").build()));
    }

    @Test
    void updateSkill_shouldThrowForbiddenException_whenSkillBelongsToAnotherTeacher() {
        final Skill existing = Skill.builder().id(1).teacherId(999).title("Old").description("Old").build();
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(skillRepository.findById(1)).thenReturn(Optional.of(existing));
        when(messageSource.getMessage(eq(MessageKeys.SKILL_FORBIDDEN), any(), any(Locale.class))).thenReturn("forbidden");

        assertThrows(SkillForbiddenException.class,
                () -> skillService.updateSkill(1, Skill.builder().title("Valid Title").description("Valid Desc").build()));
    }

    @Test
    void softDeleteSkill_shouldCallRepository_whenSkillExistsAndOwnedByTeacher() {
        final Skill skill = Skill.builder().id(1).teacherId(TEACHER_ID).title("Title").description("Desc").build();
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(skillRepository.findById(1)).thenReturn(Optional.of(skill));
        when(skillRepository.softDeleteSkill(1, TEACHER_ID)).thenReturn(skill);

        skillService.softDeleteSkill(1);

        verify(skillRepository).softDeleteSkill(1, TEACHER_ID);
    }

    @Test
    void softDeleteSkill_shouldThrowNotFoundException_whenSkillDoesNotExist() {
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(skillRepository.findById(1)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.SKILL_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

        assertThrows(SkillNotFoundException.class, () -> skillService.softDeleteSkill(1));
        verify(skillRepository, never()).softDeleteSkill(any(), any());
    }

    @Test
    void softDeleteSkill_shouldThrowForbiddenException_whenSkillBelongsToAnotherTeacher() {
        final Skill skill = Skill.builder().id(1).teacherId(999).title("T").description("D").build();
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(skillRepository.findById(1)).thenReturn(Optional.of(skill));
        when(messageSource.getMessage(eq(MessageKeys.SKILL_FORBIDDEN), any(), any(Locale.class))).thenReturn("forbidden");

        assertThrows(SkillForbiddenException.class, () -> skillService.softDeleteSkill(1));
        verify(skillRepository, never()).softDeleteSkill(any(), any());
    }

    static Stream<String> invalidFieldValues() {
        return Stream.of(null, "", "   ");
    }
}

