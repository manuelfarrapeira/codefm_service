package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
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

import static org.assertj.core.api.Assertions.*;
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

    private SkillRubricServiceImpl skillRubricService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer SKILL_ID = 10;
    private static final Integer RUBRIC_ID = 100;
    private static final Integer CRITERION_ID = 1;

    @BeforeEach
    void beforeEach() {
        skillRubricService = new SkillRubricServiceImpl(skillRubricRepository, skillRubricCriteriaRepository, skillRepository, messageSource, sessionUser);
    }

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

    @Nested
    class GetRubricsBySkillId {

        @Test
        void when_skill_is_owned_by_teacher_expect_return_rubrics_with_criteria() {
            setupTeacherAndLocale();
            setupSkillOwnership();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).title("Rubric 1").skillId(SKILL_ID).build();

            when(skillRubricRepository.findBySkillId(SKILL_ID)).thenReturn(List.of(rubric));
            when(skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(validCriteria());

            final List<SkillRubric> result = skillRubricService.getRubricsBySkillId(SKILL_ID);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getCriteria()).hasSize(3);
            verify(skillRubricCriteriaRepository).findActiveByRubricId(RUBRIC_ID);
        }

        @Test
        void when_no_rubrics_exist_expect_return_empty_list() {
            setupTeacherAndLocale();
            setupSkillOwnership();
            when(skillRubricRepository.findBySkillId(SKILL_ID)).thenReturn(List.of());

            final List<SkillRubric> result = skillRubricService.getRubricsBySkillId(SKILL_ID);

            assertThat(result).isNotNull().isEmpty();
        }

        @ParameterizedTest
        @MethodSource("skillNotFoundSetups")
        void when_skill_not_accessible_expect_throw_exception(
                Consumer<SkillRubricServiceImplTest> setup,
                Class<? extends RuntimeException> expectedException) {
            setup.accept(SkillRubricServiceImplTest.this);
            final ThrowingCallable action = () -> skillRubricService.getRubricsBySkillId(SKILL_ID);
            assertThatThrownBy(action).isInstanceOf(expectedException);
        }

        static Stream<Arguments> skillNotFoundSetups() {
            return Stream.of(
                    Arguments.of(
                            (Consumer<SkillRubricServiceImplTest>) t -> {
                                when(t.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
                                when(t.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
                                when(t.skillRepository.findById(SKILL_ID)).thenReturn(Optional.empty());
                                when(t.messageSource.getMessage(eq(MessageKeys.SKILL_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");
                            },
                            SkillNotFoundException.class),
                    Arguments.of(
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
    }

    @Nested
    class CreateRubric {

        @Test
        void when_data_is_valid_expect_create_rubric_with_title_only() {
            setupTeacherAndLocale();
            setupSkillOwnership();
            final SkillRubric rubric = SkillRubric.builder().title("New Rubric").build();
            final SkillRubric savedRubric = SkillRubric.builder().id(RUBRIC_ID).title("New Rubric").skillId(SKILL_ID).build();

            when(skillRubricRepository.save(any(SkillRubric.class))).thenReturn(savedRubric);

            final SkillRubric result = skillRubricService.createRubric(SKILL_ID, rubric);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(RUBRIC_ID);
            verify(skillRubricRepository).save(any(SkillRubric.class));
            verifyNoInteractions(skillRubricCriteriaRepository);
        }

        @Test
        void when_title_is_empty_expect_throw_validation_exception() {
            setupTeacherAndLocale();
            setupSkillOwnership();
            final SkillRubric rubric = SkillRubric.builder().title("").build();
            when(messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_VALIDATION_TITLE_REQUIRED), any(), any(Locale.class))).thenReturn("error");

            final ThrowingCallable action = () -> skillRubricService.createRubric(SKILL_ID, rubric);
            assertThatThrownBy(action).isInstanceOf(SkillRubricValidationException.class);
        }
    }

    @Nested
    class UpdateRubric {

        @Test
        void when_data_is_valid_expect_update_title() {
            setupTeacherAndLocale();
            final SkillRubric existing = SkillRubric.builder().id(RUBRIC_ID).title("Old").skillId(SKILL_ID).build();

            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(existing));
            setupSkillOwnership();
            when(skillRubricRepository.save(any(SkillRubric.class))).thenReturn(existing);

            final SkillRubric result = skillRubricService.updateRubric(RUBRIC_ID, SkillRubric.builder().title("Updated").build());

            assertThat(result).isNotNull();
            verify(skillRubricRepository).save(any(SkillRubric.class));
            verifyNoInteractions(skillRubricCriteriaRepository);
        }

        @Test
        void when_rubric_not_found_expect_throw_not_found_exception() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().title("T").build();
            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

            final ThrowingCallable action = () -> skillRubricService.updateRubric(RUBRIC_ID, rubric);
            assertThatThrownBy(action).isInstanceOf(SkillRubricNotFoundException.class);
        }

        @Test
        void when_title_is_empty_expect_throw_validation_exception() {
            setupTeacherAndLocale();
            final SkillRubric existing = SkillRubric.builder().id(RUBRIC_ID).title("Old").skillId(SKILL_ID).build();
            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(existing));
            setupSkillOwnership();
            when(messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_VALIDATION_TITLE_REQUIRED), any(), any(Locale.class))).thenReturn("error");

            final SkillRubric rubricWithEmptyTitle = SkillRubric.builder().title("").build();

            final ThrowingCallable action = () -> skillRubricService.updateRubric(RUBRIC_ID, rubricWithEmptyTitle);
            assertThatThrownBy(action).isInstanceOf(SkillRubricValidationException.class);
        }
    }

    @Nested
    class DeleteRubric {

        @Test
        void when_rubric_owned_by_teacher_expect_soft_delete() {
            setupTeacherAndLocale();
            final SkillRubric existing = SkillRubric.builder().id(RUBRIC_ID).title("R").skillId(SKILL_ID).build();

            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(existing));
            setupSkillOwnership();

            skillRubricService.deleteRubric(RUBRIC_ID);

            verify(skillRubricRepository).softDeleteById(RUBRIC_ID);
            verifyNoInteractions(skillRubricCriteriaRepository);
        }

        @Test
        void when_rubric_not_found_expect_throw_not_found_exception() {
            setupTeacherAndLocale();
            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

            final ThrowingCallable action = () -> skillRubricService.deleteRubric(RUBRIC_ID);
            assertThatThrownBy(action).isInstanceOf(SkillRubricNotFoundException.class);
        }
    }

    @Nested
    class GetCriteriaByRubricId {

        @Test
        void when_rubric_owned_by_teacher_expect_return_criteria() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(validCriteria());

            final List<SkillRubricCriteria> result = skillRubricService.getCriteriaByRubricId(RUBRIC_ID);

            assertThat(result).hasSize(3);
        }
    }

    @Nested
    class CreateCriterion {

        @Test
        void when_no_overlap_exists_expect_create_criterion() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            final SkillRubricCriteria newCriterion = SkillRubricCriteria.builder().description("New").gradeStart(0).gradeEnd(4).build();

            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(List.of());
            when(skillRubricCriteriaRepository.save(any())).thenReturn(
                    SkillRubricCriteria.builder().id(CRITERION_ID).description("New").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(4).build());

            final SkillRubricCriteria result = skillRubricService.createCriterion(RUBRIC_ID, newCriterion);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CRITERION_ID);
            verify(skillRubricCriteriaRepository).save(any());
        }

        @Test
        void when_boundary_touches_existing_range_expect_create_criterion() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            final List<SkillRubricCriteria> existing = List.of(
                    SkillRubricCriteria.builder().id(CRITERION_ID).description("A").gradeStart(0).gradeEnd(5).rubricId(RUBRIC_ID).build()
            );
            final SkillRubricCriteria touchingBoundary = SkillRubricCriteria.builder().description("B").gradeStart(5).gradeEnd(7).build();
            final SkillRubricCriteria saved = SkillRubricCriteria.builder().id(2).description("B").rubricId(RUBRIC_ID).gradeStart(5).gradeEnd(7).build();

            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(existing);
            when(skillRubricCriteriaRepository.save(any())).thenReturn(saved);

            final SkillRubricCriteria result = skillRubricService.createCriterion(RUBRIC_ID, touchingBoundary);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2);
            verify(skillRubricCriteriaRepository).save(any());
        }

        @Test
        void when_overlap_exists_expect_throw_validation_exception() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            final List<SkillRubricCriteria> existing = List.of(
                    SkillRubricCriteria.builder().id(CRITERION_ID).description("A").gradeStart(0).gradeEnd(4).rubricId(RUBRIC_ID).build()
            );
            final SkillRubricCriteria overlapping = SkillRubricCriteria.builder().description("B").gradeStart(3).gradeEnd(6).build();

            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(existing);
            when(messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_OVERLAP), any(), any(Locale.class))).thenReturn("error");

            final ThrowingCallable action = () -> skillRubricService.createCriterion(RUBRIC_ID, overlapping);
            assertThatThrownBy(action).isInstanceOf(SkillRubricValidationException.class);
        }

        @Test
        void when_description_is_empty_expect_throw_validation_exception() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();

            final SkillRubricCriteria criterion = SkillRubricCriteria.builder().description("").gradeStart(0).gradeEnd(4).build();
            when(messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_DESCRIPTION_REQUIRED), any(), any(Locale.class))).thenReturn("error");

            final ThrowingCallable action = () -> skillRubricService.createCriterion(RUBRIC_ID, criterion);
            assertThatThrownBy(action).isInstanceOf(SkillRubricValidationException.class);
        }

        @ParameterizedTest
        @MethodSource("nullGradeFieldCriteria")
        void when_grade_field_is_null_expect_throw_validation_exception(SkillRubricCriteria criterion, String expectedParam) {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(messageSource.getMessage(any(String.class), any(), any(Locale.class))).thenReturn("error");

            final ThrowingCallable action = () -> skillRubricService.createCriterion(RUBRIC_ID, criterion);
            final SkillRubricValidationException exception = catchThrowableOfType(action, SkillRubricValidationException.class);

            assertThat(exception.getErrors()).isNotEmpty();
            assertThat(exception.getErrors()).anyMatch(e -> expectedParam.equals(e.getParam()));
        }

        @ParameterizedTest
        @CsvSource({
                "-1, 5",
                "5, 11",
                "5, 3",
                "11, 5"
        })
        void when_grade_range_is_invalid_expect_throw_validation_exception(int gradeStart, int gradeEnd) {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_GRADE_RANGE_INVALID), any(), any(Locale.class))).thenReturn("error");

            final SkillRubricCriteria criterion = SkillRubricCriteria.builder()
                    .description("desc").gradeStart(gradeStart).gradeEnd(gradeEnd).build();

            final ThrowingCallable action = () -> skillRubricService.createCriterion(RUBRIC_ID, criterion);
            assertThatThrownBy(action).isInstanceOf(SkillRubricValidationException.class);
        }

        @Test
        void when_criterion_is_null_expect_throw_validation_exception_with_three_errors() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(messageSource.getMessage(any(String.class), any(), any(Locale.class))).thenReturn("error");

            final ThrowingCallable action = () -> skillRubricService.createCriterion(RUBRIC_ID, null);
            final SkillRubricValidationException exception = catchThrowableOfType(action, SkillRubricValidationException.class);

            assertThat(exception.getErrors()).hasSize(3);
            assertThat(exception.getErrors()).anyMatch(e -> "description".equals(e.getParam()));
            assertThat(exception.getErrors()).anyMatch(e -> "gradeStart".equals(e.getParam()));
            assertThat(exception.getErrors()).anyMatch(e -> "gradeEnd".equals(e.getParam()));
        }

        static Stream<Arguments> nullGradeFieldCriteria() {
            return Stream.of(
                    Arguments.of(SkillRubricCriteria.builder().description("desc").gradeStart(null).gradeEnd(5).build(), "gradeStart"),
                    Arguments.of(SkillRubricCriteria.builder().description("desc").gradeStart(0).gradeEnd(null).build(), "gradeEnd")
            );
        }
    }

    @Nested
    class UpdateCriterion {

        @Test
        void when_no_overlap_excluding_self_expect_update_criterion() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            final SkillRubricCriteria existing = SkillRubricCriteria.builder().id(CRITERION_ID).description("Old").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(4).build();
            final List<SkillRubricCriteria> allCriteria = List.of(existing);
            final SkillRubricCriteria update = SkillRubricCriteria.builder().description("Updated").gradeStart(0).gradeEnd(5).build();

            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(skillRubricCriteriaRepository.findActiveById(CRITERION_ID)).thenReturn(Optional.of(existing));
            when(skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(allCriteria);
            when(skillRubricCriteriaRepository.save(any())).thenReturn(existing);

            final SkillRubricCriteria result = skillRubricService.updateCriterion(RUBRIC_ID, CRITERION_ID, update);

            assertThat(result).isNotNull();
            verify(skillRubricCriteriaRepository).save(any());
        }

        @Test
        void when_criterion_data_is_invalid_expect_throw_validation_exception() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            final SkillRubricCriteria existing = SkillRubricCriteria.builder().id(CRITERION_ID).description("Old").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(4).build();

            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(skillRubricCriteriaRepository.findActiveById(CRITERION_ID)).thenReturn(Optional.of(existing));
            when(messageSource.getMessage(any(String.class), any(), any(Locale.class))).thenReturn("error");

            final SkillRubricCriteria invalidCriterion = SkillRubricCriteria.builder().description("").gradeStart(null).gradeEnd(null).build();

            final ThrowingCallable action = () -> skillRubricService.updateCriterion(RUBRIC_ID, CRITERION_ID, invalidCriterion);
            assertThatThrownBy(action).isInstanceOf(SkillRubricValidationException.class);
        }

        @Test
        void when_overlap_exists_expect_throw_validation_exception() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            final SkillRubricCriteria existing = SkillRubricCriteria.builder().id(CRITERION_ID).description("A").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(4).build();
            final SkillRubricCriteria other = SkillRubricCriteria.builder().id(2).description("B").rubricId(RUBRIC_ID).gradeStart(5).gradeEnd(8).build();

            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(skillRubricCriteriaRepository.findActiveById(CRITERION_ID)).thenReturn(Optional.of(existing));
            when(skillRubricCriteriaRepository.findActiveByRubricId(RUBRIC_ID)).thenReturn(List.of(existing, other));
            when(messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_OVERLAP), any(), any(Locale.class))).thenReturn("error");

            final SkillRubricCriteria overlapping = SkillRubricCriteria.builder().description("Updated").gradeStart(4).gradeEnd(7).build();

            final ThrowingCallable action = () -> skillRubricService.updateCriterion(RUBRIC_ID, CRITERION_ID, overlapping);
            assertThatThrownBy(action).isInstanceOf(SkillRubricValidationException.class);
        }

        @Test
        void when_criterion_belongs_to_different_rubric_expect_throw_not_found_exception() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();

            final SkillRubricCriteria wrongRubricCriterion = SkillRubricCriteria.builder()
                    .id(CRITERION_ID).description("A").rubricId(999).gradeStart(0).gradeEnd(4).build();
            when(skillRubricCriteriaRepository.findActiveById(CRITERION_ID)).thenReturn(Optional.of(wrongRubricCriterion));
            when(messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_CRITERIA_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

            final SkillRubricCriteria update = SkillRubricCriteria.builder().description("X").gradeStart(0).gradeEnd(5).build();

            final ThrowingCallable action = () -> skillRubricService.updateCriterion(RUBRIC_ID, CRITERION_ID, update);
            assertThatThrownBy(action).isInstanceOf(SkillRubricNotFoundException.class);
        }
    }

    @Nested
    class DeleteCriterion {

        @Test
        void when_criterion_owned_by_teacher_expect_soft_delete() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            final SkillRubricCriteria existing = SkillRubricCriteria.builder().id(CRITERION_ID).description("A").rubricId(RUBRIC_ID).gradeStart(0).gradeEnd(4).build();

            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(skillRubricCriteriaRepository.findActiveById(CRITERION_ID)).thenReturn(Optional.of(existing));

            skillRubricService.deleteCriterion(RUBRIC_ID, CRITERION_ID);

            verify(skillRubricCriteriaRepository).softDeleteById(CRITERION_ID);
        }

        @Test
        void when_criterion_not_found_expect_throw_not_found_exception() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();
            when(skillRubricCriteriaRepository.findActiveById(999)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_CRITERIA_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

            final ThrowingCallable action = () -> skillRubricService.deleteCriterion(RUBRIC_ID, 999);
            assertThatThrownBy(action).isInstanceOf(SkillRubricNotFoundException.class);
        }

        @Test
        void when_criterion_belongs_to_different_rubric_expect_throw_not_found_exception() {
            setupTeacherAndLocale();
            final SkillRubric rubric = SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).title("R").build();
            when(skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.of(rubric));
            setupSkillOwnership();

            final SkillRubricCriteria wrongRubricCriterion = SkillRubricCriteria.builder()
                    .id(CRITERION_ID).description("A").rubricId(999).gradeStart(0).gradeEnd(4).build();
            when(skillRubricCriteriaRepository.findActiveById(CRITERION_ID)).thenReturn(Optional.of(wrongRubricCriterion));
            when(messageSource.getMessage(eq(MessageKeys.SKILL_RUBRIC_CRITERIA_NOT_FOUND), any(), any(Locale.class))).thenReturn("not found");

            final ThrowingCallable action = () -> skillRubricService.deleteCriterion(RUBRIC_ID, CRITERION_ID);
            assertThatThrownBy(action).isInstanceOf(SkillRubricNotFoundException.class);
        }
    }
}

