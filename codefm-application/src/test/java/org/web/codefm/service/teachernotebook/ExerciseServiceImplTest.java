package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.entity.teachernotebook.SubjectClass;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.ExerciseRepository;
import org.web.codefm.domain.repository.teachernotebook.ExerciseStudentGradeRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectClassRepository;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExerciseServiceImplTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private SubjectClassRepository subjectClassRepository;

    @Mock
    private ExerciseDocumentService exerciseDocumentService;

    @Mock
    private ExerciseStudentGradeRepository exerciseStudentGradeRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    private ExerciseServiceImpl exerciseService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 10;
    private static final Integer SUBJECT_CLASS_ID = 5;
    private static final Integer EXERCISE_ID = 100;

    @BeforeEach
    void beforeEach() {
        exerciseService = new ExerciseServiceImpl(
                exerciseRepository, classRepository, subjectClassRepository, messageSource, sessionUser);

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        when(messageSource.getMessage(eq(MessageKeys.CLASS_FORBIDDEN), isNull(), any(Locale.class)))
                .thenReturn("Class forbidden.");
        when(messageSource.getMessage(eq(MessageKeys.EXERCISE_NOT_FOUND), isNull(), any(Locale.class)))
                .thenReturn("Exercise not found.");
        when(messageSource.getMessage(eq(MessageKeys.EXERCISE_VALIDATION_TITLE_REQUIRED), isNull(), any(Locale.class)))
                .thenReturn("Title is required.");
        when(messageSource.getMessage(eq(MessageKeys.EXERCISE_VALIDATION_QUARTER_REQUIRED), isNull(), any(Locale.class)))
                .thenReturn("Quarter is required.");
        when(messageSource.getMessage(eq(MessageKeys.EXERCISE_VALIDATION_QUARTER_INVALID), isNull(), any(Locale.class)))
                .thenReturn("Quarter must be between 1 and 3.");
        when(messageSource.getMessage(eq(MessageKeys.EXERCISE_VALIDATION_SUBJECT_CLASS_NOT_FOUND), isNull(), any(Locale.class)))
                .thenReturn("Subject-class not found.");
        when(messageSource.getMessage(eq(MessageKeys.EXERCISE_VALIDATION_PERCENTAGE_GRADE_REQUIRED), isNull(), any(Locale.class)))
                .thenReturn("Percentage grade is required.");
        when(messageSource.getMessage(eq(MessageKeys.EXERCISE_VALIDATION_PERCENTAGE_GRADE_INVALID), isNull(), any(Locale.class)))
                .thenReturn("Percentage grade must be between 1 and 100.");
        when(messageSource.getMessage(eq(MessageKeys.EXERCISE_VALIDATION_MAX_GRADE_REQUIRED), isNull(), any(Locale.class)))
                .thenReturn("Max grade is required.");
        when(messageSource.getMessage(eq(MessageKeys.EXERCISE_VALIDATION_MAX_GRADE_INVALID), isNull(), any(Locale.class)))
                .thenReturn("Max grade must be between 1 and 15.");
        when(messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), isNull(), any(Locale.class)))
                .thenReturn("Class not found.");
        when(messageSource.getMessage(eq(MessageKeys.EXERCISE_VALIDATION_PERCENTAGE_GRADE_SUM_EXCEEDED), any(Object[].class), any(Locale.class)))
                .thenReturn("Percentage sum exceeded.");

        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
        when(subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.of(SubjectClass.builder().id(SUBJECT_CLASS_ID).build()));
        when(exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarter(anyInt(), anyInt())).thenReturn(0);
        when(exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(anyInt(), anyInt(), anyInt())).thenReturn(0);
    }

    @Nested
    class GetExercisesByClassId {

        @Test
        void when_class_belongs_to_teacher_expect_return_exercises() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final List<Exercise> expectedExercises = List.of(
                    Exercise.builder().id(1).subjectClassId(SUBJECT_CLASS_ID).title("Exam 1").quarter(1).percentageGrade(30).maxGrade(10).build(),
                    Exercise.builder().id(2).subjectClassId(SUBJECT_CLASS_ID).title("Exam 2").quarter(2).percentageGrade(40).maxGrade(10).build()
            );

            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(classEntity));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(exerciseRepository.findByClassId(CLASS_ID)).thenReturn(expectedExercises);

            final List<Exercise> result = exerciseService.getExercisesByClassId(CLASS_ID);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            verify(classRepository).findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID);
            verify(exerciseRepository).findByClassId(CLASS_ID);
        }

        @Test
        void when_class_not_exists_expect_throw_not_found_exception() {
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseService.getExercisesByClassId(CLASS_ID);

            assertThatThrownBy(call).isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        void when_class_not_owned_by_teacher_expect_throw_forbidden_exception() {
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseService.getExercisesByClassId(CLASS_ID);

            assertThatThrownBy(call).isInstanceOf(ClassForbiddenException.class);
        }
    }

    @Nested
    class CreateExercise {

        @Test
        void when_data_is_valid_expect_create_exercise() {
            final Exercise inputExercise = Exercise.builder().title("Exam 1").description("Description").quarter(1).percentageGrade(30).maxGrade(10).build();
            final Exercise savedExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Exam 1").description("Description").quarter(1).percentageGrade(30).maxGrade(10).build();

            when(subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.of(SubjectClass.builder().id(SUBJECT_CLASS_ID).build()));
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);
            when(exerciseRepository.save(any(Exercise.class))).thenReturn(savedExercise);

            final Exercise result = exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(EXERCISE_ID);
            assertThat(result.getSubjectClassId()).isEqualTo(SUBJECT_CLASS_ID);
            verify(exerciseRepository).save(any(Exercise.class));
        }

        @Test
        void when_subject_class_not_exists_expect_throw_not_found_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam 1").quarter(1).percentageGrade(30).maxGrade(10).build();
            when(subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseNotFoundException.class);
        }

        @Test
        void when_subject_class_not_belongs_to_teacher_expect_throw_forbidden_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam 1").quarter(1).percentageGrade(30).maxGrade(10).build();

            when(subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.of(SubjectClass.builder().id(SUBJECT_CLASS_ID).build()));
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(false);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ClassForbiddenException.class);
        }

        @Test
        void when_title_is_empty_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title("").quarter(1).percentageGrade(30).maxGrade(10).build();
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_title_is_null_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title(null).quarter(1).percentageGrade(30).maxGrade(10).build();
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_quarter_is_null_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(null).percentageGrade(30).maxGrade(10).build();
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_quarter_is_zero_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(0).percentageGrade(30).maxGrade(10).build();
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_quarter_is_four_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(4).percentageGrade(30).maxGrade(10).build();
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_percentage_grade_is_null_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(null).maxGrade(10).build();
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_percentage_grade_is_zero_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(0).maxGrade(10).build();
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_percentage_grade_exceeds_100_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(101).maxGrade(10).build();
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_max_grade_is_null_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(30).maxGrade(null).build();
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_max_grade_is_zero_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(30).maxGrade(0).build();
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_max_grade_exceeds_15_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(30).maxGrade(16).build();
            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_percentage_sum_exceeds_100_expect_throw_validation_exception() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(40).maxGrade(10).build();

            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);
            when(exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarter(SUBJECT_CLASS_ID, 1)).thenReturn(70);

            final ThrowingCallable call = () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_percentage_sum_equals_100_expect_succeed() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();
            final Exercise savedExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();

            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);
            when(exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarter(SUBJECT_CLASS_ID, 1)).thenReturn(70);
            when(exerciseRepository.save(any(Exercise.class))).thenReturn(savedExercise);

            final Exercise result = exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(EXERCISE_ID);
        }

        @Test
        void when_no_existing_exercises_expect_succeed() {
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(50).maxGrade(10).build();
            final Exercise savedExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Exam").quarter(1).percentageGrade(50).maxGrade(10).build();

            when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);
            when(exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarter(SUBJECT_CLASS_ID, 1)).thenReturn(0);
            when(exerciseRepository.save(any(Exercise.class))).thenReturn(savedExercise);

            final Exercise result = exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    class UpdateExercise {

        @Test
        void when_data_is_valid_expect_update_exercise() {
            final Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Old Title").description("Old Desc").quarter(1).percentageGrade(30).maxGrade(10).build();
            final Exercise inputExercise = Exercise.builder().title("New Title").description("New Desc").quarter(2).percentageGrade(50).maxGrade(12).build();
            final Exercise updatedExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("New Title").description("New Desc").quarter(2).percentageGrade(50).maxGrade(12).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));
            when(exerciseRepository.update(existingExercise)).thenReturn(updatedExercise);

            final Exercise result = exerciseService.updateExercise(EXERCISE_ID, inputExercise);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("New Title");
            assertThat(result.getDescription()).isEqualTo("New Desc");
            assertThat(result.getQuarter()).isEqualTo(2);
            verify(exerciseRepository).update(existingExercise);
        }

        @Test
        void when_exercise_not_found_expect_throw_not_found_exception() {
            final Exercise inputExercise = Exercise.builder().title("New Title").quarter(1).percentageGrade(30).maxGrade(10).build();
            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseService.updateExercise(EXERCISE_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseNotFoundException.class);
        }

        @Test
        void when_title_is_empty_expect_throw_validation_exception() {
            final Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Old").quarter(1).percentageGrade(30).maxGrade(10).build();
            final Exercise inputExercise = Exercise.builder().title("").quarter(1).percentageGrade(30).maxGrade(10).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));

            final ThrowingCallable call = () -> exerciseService.updateExercise(EXERCISE_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_quarter_invalid_expect_throw_validation_exception() {
            final Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Old").quarter(1).percentageGrade(30).maxGrade(10).build();
            final Exercise inputExercise = Exercise.builder().title("Valid").quarter(5).percentageGrade(30).maxGrade(10).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));

            final ThrowingCallable call = () -> exerciseService.updateExercise(EXERCISE_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_percentage_sum_exceeds_100_expect_throw_validation_exception() {
            final Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Old").quarter(1).percentageGrade(20).maxGrade(10).build();
            final Exercise inputExercise = Exercise.builder().title("New").quarter(1).percentageGrade(50).maxGrade(10).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));
            when(exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(SUBJECT_CLASS_ID, 1, EXERCISE_ID)).thenReturn(60);

            final ThrowingCallable call = () -> exerciseService.updateExercise(EXERCISE_ID, inputExercise);

            assertThatThrownBy(call).isInstanceOf(ExerciseValidationException.class);
        }

        @Test
        void when_percentage_sum_equals_100_after_update_expect_succeed() {
            final Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Old").quarter(1).percentageGrade(20).maxGrade(10).build();
            final Exercise inputExercise = Exercise.builder().title("New").quarter(1).percentageGrade(40).maxGrade(10).build();
            final Exercise updatedExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("New").quarter(1).percentageGrade(40).maxGrade(10).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));
            when(exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(SUBJECT_CLASS_ID, 1, EXERCISE_ID)).thenReturn(60);
            when(exerciseRepository.update(any(Exercise.class))).thenReturn(updatedExercise);

            final Exercise result = exerciseService.updateExercise(EXERCISE_ID, inputExercise);

            assertThat(result).isNotNull();
            assertThat(result.getPercentageGrade()).isEqualTo(40);
        }

        @Test
        void when_calculating_percentage_sum_expect_use_excluding_id() {
            final Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Old").quarter(1).percentageGrade(30).maxGrade(10).build();
            final Exercise inputExercise = Exercise.builder().title("New").quarter(1).percentageGrade(30).maxGrade(10).build();
            final Exercise updatedExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("New").quarter(1).percentageGrade(30).maxGrade(10).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));
            when(exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(SUBJECT_CLASS_ID, 1, EXERCISE_ID)).thenReturn(50);
            when(exerciseRepository.update(any(Exercise.class))).thenReturn(updatedExercise);

            final Exercise result = exerciseService.updateExercise(EXERCISE_ID, inputExercise);

            assertThat(result).isNotNull();
            verify(exerciseRepository).sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(SUBJECT_CLASS_ID, 1, EXERCISE_ID);
        }

        @Test
        void when_percentage_sum_is_valid_expect_allow_quarter_change() {
            final Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Old").quarter(1).percentageGrade(30).maxGrade(10).build();
            final Exercise inputExercise = Exercise.builder().title("New").quarter(2).percentageGrade(30).maxGrade(10).build();
            final Exercise updatedExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("New").quarter(2).percentageGrade(30).maxGrade(10).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));
            when(exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(SUBJECT_CLASS_ID, 2, EXERCISE_ID)).thenReturn(50);
            when(exerciseRepository.update(any(Exercise.class))).thenReturn(updatedExercise);

            final Exercise result = exerciseService.updateExercise(EXERCISE_ID, inputExercise);

            assertThat(result).isNotNull();
            assertThat(result.getQuarter()).isEqualTo(2);
        }
    }

    @Nested
    class DeleteExercise {

        @Test
        void when_exercise_exists_and_belongs_to_teacher_expect_soft_delete() {
            final Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();
            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));

            exerciseService.deleteExercise(EXERCISE_ID);

            verify(exerciseRepository).softDelete(EXERCISE_ID);
        }

        @Test
        void when_exercise_not_found_expect_throw_not_found_exception() {
            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseService.deleteExercise(EXERCISE_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseNotFoundException.class);
        }
    }
}

