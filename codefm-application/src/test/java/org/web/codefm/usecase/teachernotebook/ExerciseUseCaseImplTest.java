package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.ExerciseService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseUseCaseImplTest {

    private ExerciseUseCaseImpl exerciseUseCase;

    @Mock
    private ExerciseService exerciseService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    @BeforeEach
    void beforeEach() {
        exerciseUseCase = new ExerciseUseCaseImpl(exerciseService, cascadeSoftDeleteService);
    }

    @Nested
    class GetExercisesByClassId {

        @Test
        void when_exercises_found_expect_delegated_to_service() {
            final Integer classId = 1;
            final Exercise exercise = Exercise.builder().id(1).subjectClassId(5).title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();
            when(exerciseService.getExercisesByClassId(classId)).thenReturn(List.of(exercise));

            final List<Exercise> result = exerciseUseCase.getExercisesByClassId(classId);

            assertThat(result).isNotNull().hasSize(1);
            verify(exerciseService).getExercisesByClassId(classId);
        }
    }

    @Nested
    class CreateExercise {

        @Test
        void when_creating_exercise_expect_delegated_to_service() {
            final Integer subjectClassId = 5;
            final Exercise inputExercise = Exercise.builder().title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();
            final Exercise savedExercise = Exercise.builder().id(1).subjectClassId(subjectClassId).title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();
            when(exerciseService.createExercise(subjectClassId, inputExercise)).thenReturn(savedExercise);

            final Exercise result = exerciseUseCase.createExercise(subjectClassId, inputExercise);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            verify(exerciseService).createExercise(subjectClassId, inputExercise);
        }
    }

    @Nested
    class UpdateExercise {

        @Test
        void when_updating_exercise_expect_delegated_to_service() {
            final Integer id = 1;
            final Exercise inputExercise = Exercise.builder().title("Updated").quarter(2).percentageGrade(50).maxGrade(12).build();
            final Exercise updatedExercise = Exercise.builder().id(id).subjectClassId(5).title("Updated").quarter(2).percentageGrade(50).maxGrade(12).build();
            when(exerciseService.updateExercise(id, inputExercise)).thenReturn(updatedExercise);

            final Exercise result = exerciseUseCase.updateExercise(id, inputExercise);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated");
            verify(exerciseService).updateExercise(id, inputExercise);
        }
    }

    @Nested
    class DeleteExercise {

        @Test
        void when_deleting_exercise_expect_cascade_before_service() {
            final Integer id = 1;
            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfExercise(id);
            doNothing().when(exerciseService).deleteExercise(id);

            exerciseUseCase.deleteExercise(id);

            final var order = inOrder(cascadeSoftDeleteService, exerciseService);
            order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfExercise(id);
            order.verify(exerciseService).deleteExercise(id);
        }
    }
}
