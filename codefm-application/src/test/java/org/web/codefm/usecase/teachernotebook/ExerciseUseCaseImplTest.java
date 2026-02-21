package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.service.teachernotebook.ExerciseService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseUseCaseImplTest {

    @Mock
    private ExerciseService exerciseService;

    @InjectMocks
    private ExerciseUseCaseImpl exerciseUseCase;

    @Test
    void getExercisesByClassId_shouldDelegateToService() {
        Integer classId = 1;
        Exercise exercise = Exercise.builder().id(1).subjectClassId(5).title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();

        when(exerciseService.getExercisesByClassId(classId)).thenReturn(List.of(exercise));

        List<Exercise> result = exerciseUseCase.getExercisesByClassId(classId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exerciseService).getExercisesByClassId(classId);
    }

    @Test
    void createExercise_shouldDelegateToService() {
        Integer subjectClassId = 5;
        Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();
        Exercise savedExercise = Exercise.builder().id(1).subjectClassId(subjectClassId).title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();

        when(exerciseService.createExercise(subjectClassId, inputExercise)).thenReturn(savedExercise);

        Exercise result = exerciseUseCase.createExercise(subjectClassId, inputExercise);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(exerciseService).createExercise(subjectClassId, inputExercise);
    }

    @Test
    void updateExercise_shouldDelegateToService() {
        Integer id = 1;
        Exercise inputExercise = Exercise.builder().title("Updated").quarter(2).percentageGrade(50).maxGrade(12).build();
        Exercise updatedExercise = Exercise.builder().id(id).subjectClassId(5).title("Updated").quarter(2).percentageGrade(50).maxGrade(12).build();

        when(exerciseService.updateExercise(id, inputExercise)).thenReturn(updatedExercise);

        Exercise result = exerciseUseCase.updateExercise(id, inputExercise);

        assertNotNull(result);
        assertEquals("Updated", result.getTitle());
        verify(exerciseService).updateExercise(id, inputExercise);
    }

    @Test
    void deleteExercise_shouldDelegateToService() {
        Integer id = 1;

        exerciseUseCase.deleteExercise(id);

        verify(exerciseService).deleteExercise(id);
    }
}

