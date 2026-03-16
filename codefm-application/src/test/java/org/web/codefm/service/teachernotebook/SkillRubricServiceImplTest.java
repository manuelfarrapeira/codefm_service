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
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.domain.exception.teachernotebook.SkillForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SkillNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SkillRubricNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SkillRubricValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.SkillRepository;
import org.web.codefm.domain.repository.teachernotebook.SkillRubricCriteriaRepository;
import org.web.codefm.domain.repository.teachernotebook.SkillRubricRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillRubricServiceImplTest {

    @Mock
    private SkillRubricRepository skillRubricRepository;
    @Mock
    private SkillRubricCriteriaRepository skillRubricCriteriaRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private MessageSource messageSource;
    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private SkillRubricServiceImpl skillRubricService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer SKILL_ID = 10;
    private static final Integer RUBRIC_ID = 100;
    private static final Integer CRITERION_ID = 1;

    private void setupTeacherAndLocale() {
        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    }

    private void setupSkillOwnership() {
        when(this.skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(
                Skill.builder().id(SKILL_ID).teacherId(TEACHER_ID).title("Skill").description("Desc").build()));
    }

    private List<SkillRubricCriteria> validCriteria() {
        return List.of(
                SkillRubricCriteria.builder().description("Poor").gradeStart(0).gradeEnd(4).build(),
                SkillRubricCriteria.builder().description("Average").gradeStart(5).gradeEnd(6).build(),
                SkillRubricCriteria.builder().description("Excellent").gradeStart(7).gradeEnd(10).build()
        );
    }

    @Test
    void getRubricsBySkillId_shouldReturnRubricsWithCriteria_whenSkillIsOwnedByTeacher() {
        this.setupTeacherAndLocale();
        this.setupSkillOwnership();
        final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).title("Rubric 1").skillId(SKILL_ID).build();

        when(this.skillRubricRepository.findBySkillId(SKILL_ID)).thenReturn(List.of(rubric));
        when(this.skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(this.validCriteria());

        final List<SkillRubric> result = this.skillRubricService.getRubricsBySkillId(SKILL_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getCriteria().size());
        verify(this.skillRubricCriteriaRepository).findActiveByRubricId(RUBRIC_ID);
    }

    @Test
    void getRubricsBySkillId_shouldReturnEmptyList_whenNoRubricsExist() {
        this.setupTeacherAndLocale();
        this.setupSkillOwnership();
        when(this.skillRubricRepository.findBySkillId(SKILL_ID)).thenReturn(List.of());

        final List<SkillRubric> result = this.skillRubricService.getRubricsBySkillId(SKILL_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("skillNotFoundSetups")
    void getRubricsBySkillId_shouldThrowException_whenSkillNotAccessible(
            Consumer<SkillRubricServiceImplTest> setup,
            Class<? extends RuntimeException> expectedException) {
        setup.accept(this);
        assertThrows(expectedException, () -> this.skillRubricService.getRubricsBySkillId(SKILL_ID));
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> skillNotFoundSetups() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        (Consumer<SkillRubricServiceImplTest>) t -> {
                            when(t.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
                            when(t.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
                            when(t.skillRepository.findById(SKILL_ID)).thenReturn(Optional.empty());
                            when(t.messageSource.getMessage(eq(MessageKeys.SKILL_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");
                        },
                        SkillNotFoundException.class),
                org.junit.jupiter.params.provider.Arguments.of(
                        (Consumer<SkillRubricServiceImplTest>) t -> {
                            when(t.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
                            when(t.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
                            when(t.skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(
                                    Skill.builder().id(SKILL_ID).teacherId(999).title("S").description("D").build()));
                            when(t.messageSource.getMessage(eq(MessageKeys.SKILL_FORBIDDEN), any(), any(Locale.class))).thenReturn("forbidden");
                        },
                        SkillForbiddenException.class)
        );
    }

    @Test
    void createRubric_shouldCreateRubricWithTitleOnly_whenDataIsValid() {
        this.setupTeacherAndLocale();
        this.setupSkillOwnership();
        final SkillRubric rubric = SkillRubric.builder().title("New Rubric").build();
        final SkillRubric savedRubric = SkillRubric.builder().id(RUBRIC_ID).title("New Rubric").skillId(SKILL_ID).build();

        when(this.skillRubricRepository.save(any(SkillRubric.class))).thenReturn(savedRubric);

        final SkillRubric result = this.skillRubricService.createRubric(SKILL_ID, rubric);

        assertNotNull(result);
        assertEquals(RUBRIC_ID, result.getId());
        verify(this.skillRubricRepository).save(any(SkillRubric.class));
        verifyNoInteractions(this.skillRubricCriteriaRepository);
    }

    @Test
    void createRubric_shouldThrowValidation_whenTitleIsEmpty() {
        this.setupTeacherAndLocale();
        this.setupSkillOwnership();
        final SkillRubric rubric = SkillRubric.builder().title("").build();
        when(this.messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_VALIDATION_TITLE_REQUIRED), any(), any(Locale.class))).thenReturn("error");

        assertThrows(SkillRubricValidationException.class, () -> this.skillRubricService.createRubric(SKILL_ID, rubric));
    }

    @Test
    void updateRubric_shouldUpdateTitle_whenValid() {
        this.setupTeacherAndLocale();
        final SkillRubric existing = SkillRubric.builder().id(RUBRIC_ID).title("Old").skillId(SKILL_ID).build();

        when(this.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(existing));
        this.setupSkillOwnership();
        when(this.skillRubricRepository.save(any(SkillRubric.class))).thenReturn(existing);

        final SkillRubric result = this.skillRubricService.updateRubric(RUBRIC_ID, SkillRubric.builder().title("Updated").build());

        assertNotNull(result);
        verify(this.skillRubricRepository).save(any(SkillRubric.class));
        verifyNoInteractions(this.skillRubricCriteriaRepository);
    }

    @Test
    void updateRubric_shouldThrowNotFoundException_whenRubricNotFound() {
        this.setupTeacherAndLocale();
        final SkillRubric rubric = SkillRubric.builder().title("T").build();
        when(this.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

        assertThrows(SkillRubricNotFoundException.class, () -> this.skillRubricService.updateRubric(RUBRIC_ID, rubric));
    }

    @Test
    void deleteRubric_shouldSoftDeleteRubric_whenOwnedByTeacher() {
        this.setupTeacherAndLocale();
        final SkillRubric existing = SkillRubric.builder().id(RUBRIC_ID).title("R").skillId(SKILL_ID).build();

        when(this.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(existing));
        this.setupSkillOwnership();

        this.skillRubricService.deleteRubric(RUBRIC_ID);

        verify(this.skillRubricRepository).softDeleteById(RUBRIC_ID);
        verifyNoInteractions(this.skillRubricCriteriaRepository);
    }

    @Test
    void deleteRubric_shouldThrowNotFoundException_whenRubricNotFound() {
        this.setupTeacherAndLocale();
        when(this.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

        assertThrows(SkillRubricNotFoundException.class, () -> this.skillRubricService.deleteRubric(RUBRIC_ID));
    }

    @Test
    void getCriteriaByRubricId_shouldReturnCriteria_whenRubricOwnedByTeacher() {
        this.setupTeacherAndLocale();
        final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
        when(this.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
        this.setupSkillOwnership();
        when(this.skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(this.validCriteria());

        final List<SkillRubricCriteria> result = this.skillRubricService.getCriteriaByRubricId(RUBRIC_ID);

        assertEquals(3, result.size());
    }

    @Test
    void createCriterion_shouldCreate_whenNoOverlap() {
        this.setupTeacherAndLocale();
        final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
        final SkillRubricCriteria newCriterion = SkillRubricCriteria.builder().description("New").gradeStart(0).gradeEnd(4).build();

        when(this.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
        this.setupSkillOwnership();
        when(this.skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(List.of());
        when(this.skillRubricCriteriaRepository.save(any())).thenReturn(
                SkillRubricCriteria.builder().id(CRITERION_ID).description("New").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(4).build());

        final SkillRubricCriteria result = this.skillRubricService.createCriterion(RUBRIC_ID, newCriterion);

        assertNotNull(result);
        assertEquals(CRITERION_ID, result.getId());
        verify(this.skillRubricCriteriaRepository).save(any());
    }

    @Test
    void createCriterion_shouldThrowValidation_whenOverlap() {
        this.setupTeacherAndLocale();
        final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
        final List<SkillRubricCriteria> existing = List.of(
                SkillRubricCriteria.builder().id(CRITERION_ID).description("A").gradeStart(0).gradeEnd(4).rubricId(RUBRIC_ID).build()
        );
        final SkillRubricCriteria overlapping = SkillRubricCriteria.builder().description("B").gradeStart(3).gradeEnd(6).build();

        when(this.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
        this.setupSkillOwnership();
        when(this.skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(existing);
        when(this.messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_OVERLAP), any(), any(Locale.class))).thenReturn("error");

        assertThrows(SkillRubricValidationException.class, () -> this.skillRubricService.createCriterion(RUBRIC_ID, overlapping));
    }

    @Test
    void createCriterion_shouldThrowValidation_whenDescriptionIsEmpty() {
        this.setupTeacherAndLocale();
        final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
        when(this.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
        this.setupSkillOwnership();

        final SkillRubricCriteria criterion = SkillRubricCriteria.builder().description("").gradeStart(0).gradeEnd(4).build();
        when(this.messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_DESCRIPTION_REQUIRED), any(), any(Locale.class))).thenReturn("error");

        assertThrows(SkillRubricValidationException.class, () -> this.skillRubricService.createCriterion(RUBRIC_ID, criterion));
    }

    @Test
    void updateCriterion_shouldUpdate_whenNoOverlapExcludingSelf() {
        this.setupTeacherAndLocale();
        final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
        final SkillRubricCriteria existing = SkillRubricCriteria.builder().id(CRITERION_ID).description("Old").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(4).build();
        final List<SkillRubricCriteria> allCriteria = List.of(existing);
        final SkillRubricCriteria update = SkillRubricCriteria.builder().description("Updated").gradeStart(0).gradeEnd(5).build();

        when(this.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
        this.setupSkillOwnership();
        when(this.skillRubricCriteriaRepository.findActiveById(CRITERION_ID)).thenReturn(Optional.of(existing));
        when(this.skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(allCriteria);
        when(this.skillRubricCriteriaRepository.save(any())).thenReturn(existing);

        final SkillRubricCriteria result = this.skillRubricService.updateCriterion(RUBRIC_ID, CRITERION_ID, update);

        assertNotNull(result);
        verify(this.skillRubricCriteriaRepository).save(any());
    }

    @Test
    void deleteCriterion_shouldSoftDelete_whenOwnedByTeacher() {
        this.setupTeacherAndLocale();
        final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
        final SkillRubricCriteria existing = SkillRubricCriteria.builder().id(CRITERION_ID).description("A").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(4).build();

        when(this.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
        this.setupSkillOwnership();
        when(this.skillRubricCriteriaRepository.findActiveById(CRITERION_ID)).thenReturn(Optional.of(existing));

        this.skillRubricService.deleteCriterion(RUBRIC_ID, CRITERION_ID);

        verify(this.skillRubricCriteriaRepository).softDeleteById(CRITERION_ID);
    }

    @Test
    void deleteCriterion_shouldThrowNotFound_whenCriterionNotFound() {
        this.setupTeacherAndLocale();
        final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
        when(this.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
        this.setupSkillOwnership();
        when(this.skillRubricCriteriaRepository.findActiveById(999)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_CRITERIA_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

        assertThrows(SkillRubricNotFoundException.class, () -> this.skillRubricService.deleteCriterion(RUBRIC_ID, 999));
    }
}

