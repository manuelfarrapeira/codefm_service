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
import org.web.codefm.domain.entity.teachernotebook.*;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseStudentGradeNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseStudentGradeValidationException;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExerciseStudentGradeServiceImplTest {

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 1;
    private static final Integer STUDENT_ID = 1;
    private static final Integer EXERCISE_ID = 1;
    private static final Integer GRADE_ID = 1;
    private static final Integer SUBJECT_CLASS_ID = 10;

    @Mock
    private ExerciseStudentGradeRepository exerciseStudentGradeRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentClassRepository studentClassRepository;

    @Mock
    private SubjectClassRepository subjectClassRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    private ExerciseStudentGradeServiceImpl exerciseStudentGradeService;

    @BeforeEach
    void beforeEach() {
        this.exerciseStudentGradeService = new ExerciseStudentGradeServiceImpl(
                this.exerciseStudentGradeRepository,
                this.exerciseRepository,
                this.classRepository,
                this.studentRepository,
                this.studentClassRepository,
                this.subjectClassRepository,
                this.messageSource,
                this.sessionUser
        );
        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(this.messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("error message");
    }

    @Nested
    class GetGradesByClassId {

        @Test
        void when_class_belongs_to_teacher_expect_return_grades() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));
            final List<ExerciseStudentGrade> expected = List.of(ExerciseStudentGrade.builder().id(1).build());
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(expected);

            final List<ExerciseStudentGrade> result = ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.getGradesByClassId(CLASS_ID);

            assertThat(result).hasSize(1);
            verify(ExerciseStudentGradeServiceImplTest.this.classRepository).findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID);
            verify(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository).findByClassId(CLASS_ID);
        }

        @Test
        void when_class_does_not_exist_expect_throw_not_found_exception() {
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.getGradesByClassId(CLASS_ID);

            assertThatThrownBy(action).isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        void when_class_is_not_owned_by_teacher_expect_throw_forbidden_exception() {
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.getGradesByClassId(CLASS_ID);

            assertThatThrownBy(action).isInstanceOf(ClassForbiddenException.class);
        }
    }

    @Nested
    class GetGradesByClassIdAndStudentId {

        @Test
        void when_input_is_valid_expect_return_grades() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));
            final StudentClass sc = StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID).build();
            when(ExerciseStudentGradeServiceImplTest.this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));
            final List<ExerciseStudentGrade> expected = List.of(ExerciseStudentGrade.builder().id(1).build());
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(expected);

            final List<ExerciseStudentGrade> result = ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.getGradesByClassIdAndStudentId(CLASS_ID, STUDENT_ID);

            assertThat(result).hasSize(1);
        }

        @Test
        void when_student_is_not_in_class_expect_throw_validation_exception() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));
            when(ExerciseStudentGradeServiceImplTest.this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.empty());
            when(ExerciseStudentGradeServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.getGradesByClassIdAndStudentId(CLASS_ID, STUDENT_ID);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeValidationException.class);
        }

        @Test
        void when_class_does_not_exist_expect_throw_not_found_exception() {
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.getGradesByClassIdAndStudentId(CLASS_ID, STUDENT_ID);

            assertThatThrownBy(action).isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        void when_class_is_not_owned_by_teacher_expect_throw_forbidden_exception() {
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
            when(ExerciseStudentGradeServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.getGradesByClassIdAndStudentId(CLASS_ID, STUDENT_ID);

            assertThatThrownBy(action).isInstanceOf(ClassForbiddenException.class);
        }
    }

    @Nested
    class CreateGrade {

        @Test
        void when_input_is_valid_expect_create_grade() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            final Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
            when(ExerciseStudentGradeServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));
            final SubjectClass sc = SubjectClass.builder().id(SUBJECT_CLASS_ID).classId(CLASS_ID).build();
            when(ExerciseStudentGradeServiceImplTest.this.subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.of(sc));
            final StudentClass studentClass = StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID).build();
            when(ExerciseStudentGradeServiceImplTest.this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(studentClass));
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(STUDENT_ID, EXERCISE_ID)).thenReturn(false);
            final ExerciseStudentGrade saved = ExerciseStudentGrade.builder().id(1).studentId(STUDENT_ID).exerciseId(EXERCISE_ID).grade(8.0).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.save(any())).thenReturn(saved);

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).description("Good").build();
            final ExerciseStudentGrade result = ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.createGrade(EXERCISE_ID, input);

            assertThat(result).isNotNull();
            assertThat(result.getGrade()).isEqualTo(8.0);
            verify(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository).save(any());
        }

        @Test
        void when_exercise_is_not_found_expect_throw_not_found_exception() {
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).build();
            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.createGrade(EXERCISE_ID, input);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeNotFoundException.class);
        }

        @Test
        void when_grade_is_null_expect_throw_validation_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            final Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
            when(ExerciseStudentGradeServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(null).build();
            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.createGrade(EXERCISE_ID, input);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeValidationException.class);
        }

        @Test
        void when_grade_exceeds_max_expect_throw_validation_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            final Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
            when(ExerciseStudentGradeServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(15.0).build();
            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.createGrade(EXERCISE_ID, input);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeValidationException.class);
        }

        @Test
        void when_student_is_not_in_class_expect_throw_validation_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            final Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
            when(ExerciseStudentGradeServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));
            final SubjectClass sc = SubjectClass.builder().id(SUBJECT_CLASS_ID).classId(CLASS_ID).build();
            when(ExerciseStudentGradeServiceImplTest.this.subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.of(sc));
            when(ExerciseStudentGradeServiceImplTest.this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.empty());

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).build();
            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.createGrade(EXERCISE_ID, input);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeValidationException.class);
        }

        @Test
        void when_grade_is_duplicate_expect_throw_validation_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            final Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
            when(ExerciseStudentGradeServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));
            final SubjectClass sc = SubjectClass.builder().id(SUBJECT_CLASS_ID).classId(CLASS_ID).build();
            when(ExerciseStudentGradeServiceImplTest.this.subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.of(sc));
            final StudentClass studentClass = StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID).build();
            when(ExerciseStudentGradeServiceImplTest.this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(studentClass));
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(STUDENT_ID, EXERCISE_ID)).thenReturn(true);

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).build();
            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.createGrade(EXERCISE_ID, input);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeValidationException.class);
        }

        @Test
        void when_student_exists_but_is_not_in_class_expect_throw_validation_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            final Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
            when(ExerciseStudentGradeServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));
            final SubjectClass sc = SubjectClass.builder().id(SUBJECT_CLASS_ID).classId(CLASS_ID).build();
            when(ExerciseStudentGradeServiceImplTest.this.subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.of(sc));
            when(ExerciseStudentGradeServiceImplTest.this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.empty());

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).build();
            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.createGrade(EXERCISE_ID, input);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeValidationException.class);
        }

        @Test
        void when_student_id_is_null_expect_throw_validation_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(null).grade(8.0).build();
            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.createGrade(EXERCISE_ID, input);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeValidationException.class);
        }

        @Test
        void when_student_does_not_belong_to_teacher_expect_throw_validation_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(ExerciseStudentGradeServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).build();
            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.createGrade(EXERCISE_ID, input);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeValidationException.class);
        }
    }

    @Nested
    class UpdateGrade {

        @Test
        void when_input_is_valid_expect_update_grade() {
            final ExerciseStudentGrade existing = ExerciseStudentGrade.builder().id(GRADE_ID).studentId(STUDENT_ID).exerciseId(EXERCISE_ID).grade(5.0).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(existing));
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).maxGrade(10).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            final ExerciseStudentGrade updated = ExerciseStudentGrade.builder().id(GRADE_ID).studentId(STUDENT_ID).exerciseId(EXERCISE_ID).grade(9.0).description("Updated").build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.update(any())).thenReturn(updated);

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().grade(9.0).description("Updated").build();
            final ExerciseStudentGrade result = ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.updateGrade(GRADE_ID, input);

            assertThat(result.getGrade()).isEqualTo(9.0);
            assertThat(result.getDescription()).isEqualTo("Updated");
        }

        @Test
        void when_grade_is_not_found_expect_throw_not_found_exception() {
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().grade(9.0).build();
            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.updateGrade(GRADE_ID, input);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeNotFoundException.class);
        }

        @Test
        void when_grade_exceeds_max_expect_throw_validation_exception() {
            final ExerciseStudentGrade existing = ExerciseStudentGrade.builder().id(GRADE_ID).studentId(STUDENT_ID).exerciseId(EXERCISE_ID).grade(5.0).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(existing));
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).maxGrade(10).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().grade(15.0).build();
            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.updateGrade(GRADE_ID, input);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeValidationException.class);
        }

        @Test
        void when_exercise_is_not_found_expect_throw_not_found_exception() {
            final ExerciseStudentGrade existing = ExerciseStudentGrade.builder().id(GRADE_ID).studentId(STUDENT_ID).exerciseId(EXERCISE_ID).grade(5.0).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(existing));
            when(ExerciseStudentGradeServiceImplTest.this.exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().grade(9.0).build();
            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.updateGrade(GRADE_ID, input);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeNotFoundException.class);
        }
    }

    @Nested
    class DeleteGrade {

        @Test
        void when_grade_exists_expect_delete_grade() {
            final ExerciseStudentGrade existing = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(existing));

            ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.deleteGrade(GRADE_ID);

            verify(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository).softDelete(GRADE_ID);
        }

        @Test
        void when_grade_is_not_found_expect_throw_not_found_exception() {
            when(ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> ExerciseStudentGradeServiceImplTest.this.exerciseStudentGradeService.deleteGrade(GRADE_ID);

            assertThatThrownBy(action).isInstanceOf(ExerciseStudentGradeNotFoundException.class);
        }
    }
}

