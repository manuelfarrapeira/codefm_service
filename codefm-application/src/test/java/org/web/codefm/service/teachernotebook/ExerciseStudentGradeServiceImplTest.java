package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExerciseStudentGradeServiceImplTest {

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

    @InjectMocks
    private ExerciseStudentGradeServiceImpl exerciseStudentGradeService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 1;
    private static final Integer STUDENT_ID = 1;
    private static final Integer EXERCISE_ID = 1;
    private static final Integer GRADE_ID = 1;
    private static final Integer SUBJECT_CLASS_ID = 10;

    @BeforeEach
    void setUp() {
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("error message");
    }

    @Test
    void getGradesByClassId_shouldReturnGrades_whenClassBelongsToTeacher() {
        Class clazz = Class.builder().id(CLASS_ID).build();
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));
        List<ExerciseStudentGrade> expected = List.of(ExerciseStudentGrade.builder().id(1).build());
        when(exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(expected);

        List<ExerciseStudentGrade> result = exerciseStudentGradeService.getGradesByClassId(CLASS_ID);

        assertEquals(1, result.size());
        verify(classRepository).findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID);
        verify(exerciseStudentGradeRepository).findByClassId(CLASS_ID);
    }

    @Test
    void getGradesByClassId_shouldThrowNotFoundException_whenClassNotExists() {
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

        assertThrows(ClassNotFoundException.class,
                () -> exerciseStudentGradeService.getGradesByClassId(CLASS_ID));
    }

    @Test
    void getGradesByClassId_shouldThrowForbiddenException_whenClassNotOwnedByTeacher() {
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ClassForbiddenException.class,
                () -> exerciseStudentGradeService.getGradesByClassId(CLASS_ID));
    }

    @Test
    void getGradesByClassIdAndStudentId_shouldReturnGrades_whenValid() {
        Class clazz = Class.builder().id(CLASS_ID).build();
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));
        StudentClass sc = StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID).build();
        when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));
        List<ExerciseStudentGrade> expected = List.of(ExerciseStudentGrade.builder().id(1).build());
        when(exerciseStudentGradeRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(expected);

        List<ExerciseStudentGrade> result = exerciseStudentGradeService.getGradesByClassIdAndStudentId(CLASS_ID, STUDENT_ID);

        assertEquals(1, result.size());
    }

    @Test
    void getGradesByClassIdAndStudentId_shouldThrowValidation_whenStudentNotInClass() {
        Class clazz = Class.builder().id(CLASS_ID).build();
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));
        when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.empty());
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseStudentGradeValidationException.class,
                () -> exerciseStudentGradeService.getGradesByClassIdAndStudentId(CLASS_ID, STUDENT_ID));
    }

    @Test
    void getGradesByClassIdAndStudentId_shouldThrowNotFoundException_whenClassNotExists() {
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

        assertThrows(ClassNotFoundException.class,
                () -> exerciseStudentGradeService.getGradesByClassIdAndStudentId(CLASS_ID, STUDENT_ID));
    }

    @Test
    void getGradesByClassIdAndStudentId_shouldThrowForbiddenException_whenClassNotOwnedByTeacher() {
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ClassForbiddenException.class,
                () -> exerciseStudentGradeService.getGradesByClassIdAndStudentId(CLASS_ID, STUDENT_ID));
    }

    @Test
    void createGrade_shouldCreateGrade_whenValid() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));
        SubjectClass sc = SubjectClass.builder().id(SUBJECT_CLASS_ID).classId(CLASS_ID).build();
        when(subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.of(sc));
        StudentClass studentClass = StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID).build();
        when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(studentClass));
        when(exerciseStudentGradeRepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(STUDENT_ID, EXERCISE_ID)).thenReturn(false);
        ExerciseStudentGrade saved = ExerciseStudentGrade.builder().id(1).studentId(STUDENT_ID).exerciseId(EXERCISE_ID).grade(8.0).build();
        when(exerciseStudentGradeRepository.save(any())).thenReturn(saved);

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).description("Good").build();
        ExerciseStudentGrade result = exerciseStudentGradeService.createGrade(EXERCISE_ID, input);

        assertNotNull(result);
        assertEquals(8.0, result.getGrade());
        verify(exerciseStudentGradeRepository).save(any());
    }

    @Test
    void createGrade_shouldThrowNotFoundException_whenExerciseNotFound() {
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).build();

        assertThrows(ExerciseStudentGradeNotFoundException.class,
                () -> exerciseStudentGradeService.createGrade(EXERCISE_ID, input));
    }

    @Test
    void createGrade_shouldThrowValidation_whenGradeIsNull() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(null).build();

        assertThrows(ExerciseStudentGradeValidationException.class,
                () -> exerciseStudentGradeService.createGrade(EXERCISE_ID, input));
    }

    @Test
    void createGrade_shouldThrowValidation_whenGradeExceedsMax() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(15.0).build();

        assertThrows(ExerciseStudentGradeValidationException.class,
                () -> exerciseStudentGradeService.createGrade(EXERCISE_ID, input));
    }

    @Test
    void createGrade_shouldThrowValidation_whenStudentNotInClass() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));
        SubjectClass sc = SubjectClass.builder().id(SUBJECT_CLASS_ID).classId(CLASS_ID).build();
        when(subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.of(sc));
        when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.empty());

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).build();

        assertThrows(ExerciseStudentGradeValidationException.class,
                () -> exerciseStudentGradeService.createGrade(EXERCISE_ID, input));
    }

    @Test
    void createGrade_shouldThrowValidation_whenDuplicate() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));
        SubjectClass sc = SubjectClass.builder().id(SUBJECT_CLASS_ID).classId(CLASS_ID).build();
        when(subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.of(sc));
        StudentClass studentClass = StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID).build();
        when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(studentClass));
        when(exerciseStudentGradeRepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(STUDENT_ID, EXERCISE_ID)).thenReturn(true);

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).build();

        assertThrows(ExerciseStudentGradeValidationException.class,
                () -> exerciseStudentGradeService.createGrade(EXERCISE_ID, input));
    }

    @Test
    void createGrade_shouldShowStudentFullName_whenStudentExistsButNotInClass() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        Student student = Student.builder().id(STUDENT_ID).name("Juan").surnames("García").build();
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.of(student));
        SubjectClass sc = SubjectClass.builder().id(SUBJECT_CLASS_ID).classId(CLASS_ID).build();
        when(subjectClassRepository.findById(SUBJECT_CLASS_ID)).thenReturn(Optional.of(sc));
        when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.empty());

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).build();

        assertThrows(ExerciseStudentGradeValidationException.class,
                () -> exerciseStudentGradeService.createGrade(EXERCISE_ID, input));
    }

    @Test
    void createGrade_shouldThrowValidation_whenStudentIdIsNull() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(null).grade(8.0).build();

        assertThrows(ExerciseStudentGradeValidationException.class,
                () -> exerciseStudentGradeService.createGrade(EXERCISE_ID, input));
    }

    @Test
    void createGrade_shouldThrowValidation_whenStudentNotBelongsToTeacher() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).maxGrade(10).build();
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID)).thenReturn(Optional.empty());

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(STUDENT_ID).grade(8.0).build();

        assertThrows(ExerciseStudentGradeValidationException.class,
                () -> exerciseStudentGradeService.createGrade(EXERCISE_ID, input));
    }

    @Test
    void updateGrade_shouldUpdateGrade_whenValid() {
        ExerciseStudentGrade existing = ExerciseStudentGrade.builder().id(GRADE_ID).studentId(STUDENT_ID).exerciseId(EXERCISE_ID).grade(5.0).build();
        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(existing));
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).maxGrade(10).build();
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        ExerciseStudentGrade updated = ExerciseStudentGrade.builder().id(GRADE_ID).studentId(STUDENT_ID).exerciseId(EXERCISE_ID).grade(9.0).description("Updated").build();
        when(exerciseStudentGradeRepository.update(any())).thenReturn(updated);

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().grade(9.0).description("Updated").build();
        ExerciseStudentGrade result = exerciseStudentGradeService.updateGrade(GRADE_ID, input);

        assertEquals(9.0, result.getGrade());
        assertEquals("Updated", result.getDescription());
    }

    @Test
    void updateGrade_shouldThrowNotFoundException_whenGradeNotFound() {
        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().grade(9.0).build();

        assertThrows(ExerciseStudentGradeNotFoundException.class,
                () -> exerciseStudentGradeService.updateGrade(GRADE_ID, input));
    }

    @Test
    void updateGrade_shouldThrowValidation_whenGradeExceedsMax() {
        ExerciseStudentGrade existing = ExerciseStudentGrade.builder().id(GRADE_ID).studentId(STUDENT_ID).exerciseId(EXERCISE_ID).grade(5.0).build();
        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(existing));
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).maxGrade(10).build();
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().grade(15.0).build();

        assertThrows(ExerciseStudentGradeValidationException.class,
                () -> exerciseStudentGradeService.updateGrade(GRADE_ID, input));
    }

    @Test
    void updateGrade_shouldThrowNotFoundException_whenExerciseNotFound() {
        ExerciseStudentGrade existing = ExerciseStudentGrade.builder().id(GRADE_ID).studentId(STUDENT_ID).exerciseId(EXERCISE_ID).grade(5.0).build();
        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(existing));
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        ExerciseStudentGrade input = ExerciseStudentGrade.builder().grade(9.0).build();

        assertThrows(ExerciseStudentGradeNotFoundException.class,
                () -> exerciseStudentGradeService.updateGrade(GRADE_ID, input));
    }

    @Test
    void deleteGrade_shouldDeleteGrade_whenValid() {
        ExerciseStudentGrade existing = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(existing));

        exerciseStudentGradeService.deleteGrade(GRADE_ID);

        verify(exerciseStudentGradeRepository).softDelete(GRADE_ID);
    }

    @Test
    void deleteGrade_shouldThrowNotFoundException_whenGradeNotFound() {
        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseStudentGradeNotFoundException.class,
                () -> exerciseStudentGradeService.deleteGrade(GRADE_ID));
    }
}

