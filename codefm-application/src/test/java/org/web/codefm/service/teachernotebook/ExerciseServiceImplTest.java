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
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.exception.teachernotebook.ExerciseNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.ExerciseRepository;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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
    private ExerciseDocumentService exerciseDocumentService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private ExerciseServiceImpl exerciseService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 10;
    private static final Integer SUBJECT_CLASS_ID = 5;
    private static final Integer EXERCISE_ID = 100;

    @BeforeEach
    void setUp() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), TEACHER_ID.toString());
        when(sessionUser.getParameters()).thenReturn(parameters);
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
    }

    @Test
    void getExercisesByClassId_shouldReturnExercises_whenClassBelongsToTeacher() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        List<Exercise> expectedExercises = List.of(
                Exercise.builder().id(1).subjectClassId(SUBJECT_CLASS_ID).title("Exam 1").quarter(1).percentageGrade(30).maxGrade(10).build(),
                Exercise.builder().id(2).subjectClassId(SUBJECT_CLASS_ID).title("Exam 2").quarter(2).percentageGrade(40).maxGrade(10).build()
        );

        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(exerciseRepository.findByClassId(CLASS_ID)).thenReturn(expectedExercises);

        List<Exercise> result = exerciseService.getExercisesByClassId(CLASS_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(classRepository).findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID);
        verify(exerciseRepository).findByClassId(CLASS_ID);
    }

    @Test
    void getExercisesByClassId_shouldThrowException_whenClassNotFound() {
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseNotFoundException.class, () -> exerciseService.getExercisesByClassId(CLASS_ID));
    }

    @Test
    void createExercise_shouldCreateExercise_whenDataIsValid() {
        Exercise inputExercise = Exercise.builder().title("Exam 1").description("Description").quarter(1).percentageGrade(30).maxGrade(10).build();
        Exercise savedExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Exam 1").description("Description").quarter(1).percentageGrade(30).maxGrade(10).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(savedExercise);

        Exercise result = exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise);

        assertNotNull(result);
        assertEquals(EXERCISE_ID, result.getId());
        assertEquals(SUBJECT_CLASS_ID, result.getSubjectClassId());
        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    void createExercise_shouldThrowException_whenSubjectClassNotBelongsToTeacher() {
        Exercise inputExercise = Exercise.builder().title("Exam 1").quarter(1).percentageGrade(30).maxGrade(10).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(false);

        assertThrows(ExerciseNotFoundException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void createExercise_shouldThrowValidationException_whenTitleIsEmpty() {
        Exercise inputExercise = Exercise.builder().title("").quarter(1).percentageGrade(30).maxGrade(10).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

        assertThrows(ExerciseValidationException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void createExercise_shouldThrowValidationException_whenTitleIsNull() {
        Exercise inputExercise = Exercise.builder().title(null).quarter(1).percentageGrade(30).maxGrade(10).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

        assertThrows(ExerciseValidationException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void createExercise_shouldThrowValidationException_whenQuarterIsNull() {
        Exercise inputExercise = Exercise.builder().title("Exam").quarter(null).percentageGrade(30).maxGrade(10).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

        assertThrows(ExerciseValidationException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void createExercise_shouldThrowValidationException_whenQuarterIsZero() {
        Exercise inputExercise = Exercise.builder().title("Exam").quarter(0).percentageGrade(30).maxGrade(10).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

        assertThrows(ExerciseValidationException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void createExercise_shouldThrowValidationException_whenQuarterIsFour() {
        Exercise inputExercise = Exercise.builder().title("Exam").quarter(4).percentageGrade(30).maxGrade(10).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

        assertThrows(ExerciseValidationException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void createExercise_shouldThrowValidationException_whenPercentageGradeIsNull() {
        Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(null).maxGrade(10).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

        assertThrows(ExerciseValidationException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void createExercise_shouldThrowValidationException_whenPercentageGradeIsZero() {
        Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(0).maxGrade(10).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

        assertThrows(ExerciseValidationException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void createExercise_shouldThrowValidationException_whenPercentageGradeExceeds100() {
        Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(101).maxGrade(10).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

        assertThrows(ExerciseValidationException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void createExercise_shouldThrowValidationException_whenMaxGradeIsNull() {
        Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(30).maxGrade(null).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

        assertThrows(ExerciseValidationException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void createExercise_shouldThrowValidationException_whenMaxGradeIsZero() {
        Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(30).maxGrade(0).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

        assertThrows(ExerciseValidationException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void createExercise_shouldThrowValidationException_whenMaxGradeExceeds15() {
        Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(30).maxGrade(16).build();

        when(exerciseRepository.subjectClassBelongsToTeacher(SUBJECT_CLASS_ID, TEACHER_ID)).thenReturn(true);

        assertThrows(ExerciseValidationException.class, () -> exerciseService.createExercise(SUBJECT_CLASS_ID, inputExercise));
    }

    @Test
    void updateExercise_shouldUpdateExercise_whenDataIsValid() {
        Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Old Title").description("Old Desc").quarter(1).percentageGrade(30).maxGrade(10).build();
        Exercise inputExercise = Exercise.builder().title("New Title").description("New Desc").quarter(2).percentageGrade(50).maxGrade(12).build();
        Exercise updatedExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("New Title").description("New Desc").quarter(2).percentageGrade(50).maxGrade(12).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));
        when(exerciseRepository.update(existingExercise)).thenReturn(updatedExercise);

        Exercise result = exerciseService.updateExercise(EXERCISE_ID, inputExercise);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Desc", result.getDescription());
        assertEquals(2, result.getQuarter());
        verify(exerciseRepository).update(existingExercise);
    }

    @Test
    void updateExercise_shouldThrowNotFoundException_whenExerciseNotFound() {
        Exercise inputExercise = Exercise.builder().title("New Title").quarter(1).percentageGrade(30).maxGrade(10).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseNotFoundException.class, () -> exerciseService.updateExercise(EXERCISE_ID, inputExercise));
    }

    @Test
    void updateExercise_shouldThrowValidationException_whenTitleIsEmpty() {
        Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Old").quarter(1).percentageGrade(30).maxGrade(10).build();
        Exercise inputExercise = Exercise.builder().title("").quarter(1).percentageGrade(30).maxGrade(10).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));

        assertThrows(ExerciseValidationException.class, () -> exerciseService.updateExercise(EXERCISE_ID, inputExercise));
    }

    @Test
    void updateExercise_shouldThrowValidationException_whenQuarterInvalid() {
        Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Old").quarter(1).percentageGrade(30).maxGrade(10).build();
        Exercise inputExercise = Exercise.builder().title("Valid").quarter(5).percentageGrade(30).maxGrade(10).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));

        assertThrows(ExerciseValidationException.class, () -> exerciseService.updateExercise(EXERCISE_ID, inputExercise));
    }

    @Test
    void deleteExercise_shouldSoftDelete_whenExerciseExistsAndBelongsToTeacher() {
        Exercise existingExercise = Exercise.builder().id(EXERCISE_ID).subjectClassId(SUBJECT_CLASS_ID).title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(existingExercise));

        exerciseService.deleteExercise(EXERCISE_ID);

        verify(exerciseRepository).softDelete(EXERCISE_ID);
    }

    @Test
    void deleteExercise_shouldThrowNotFoundException_whenExerciseNotFound() {
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseNotFoundException.class, () -> exerciseService.deleteExercise(EXERCISE_ID));
    }
}

