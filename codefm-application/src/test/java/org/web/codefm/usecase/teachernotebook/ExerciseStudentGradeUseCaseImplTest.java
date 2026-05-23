package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentDocumentService;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentGradeService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseStudentGradeUseCaseImplTest {

    private ExerciseStudentGradeUseCaseImpl exerciseStudentGradeUseCase;

    @Mock
    private ExerciseStudentGradeService exerciseStudentGradeService;

    @Mock
    private ExerciseStudentDocumentService exerciseStudentDocumentService;

    @BeforeEach
    void beforeEach() {
        exerciseStudentGradeUseCase = new ExerciseStudentGradeUseCaseImpl(exerciseStudentGradeService, exerciseStudentDocumentService);
    }

    @Nested
    class GetGradesByClassId {

        @Test
        void when_grades_found_expect_delegated_to_service() {
            final List<ExerciseStudentGrade> expected = List.of(ExerciseStudentGrade.builder().id(1).build());
            when(exerciseStudentGradeService.getGradesByClassId(1)).thenReturn(expected);

            final List<ExerciseStudentGrade> result = exerciseStudentGradeUseCase.getGradesByClassId(1);

            assertThat(result).isEqualTo(expected);
            verify(exerciseStudentGradeService).getGradesByClassId(1);
        }
    }

    @Nested
    class GetGradesByClassIdAndStudentId {

        @Test
        void when_grades_found_expect_delegated_to_service() {
            final List<ExerciseStudentGrade> expected = List.of(ExerciseStudentGrade.builder().id(1).build());
            when(exerciseStudentGradeService.getGradesByClassIdAndStudentId(1, 2)).thenReturn(expected);

            final List<ExerciseStudentGrade> result = exerciseStudentGradeUseCase.getGradesByClassIdAndStudentId(1, 2);

            assertThat(result).isEqualTo(expected);
            verify(exerciseStudentGradeService).getGradesByClassIdAndStudentId(1, 2);
        }
    }

    @Nested
    class CreateGrade {

        @Test
        void when_creating_grade_expect_delegated_to_service() {
            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(1).grade(8.0).build();
            final ExerciseStudentGrade saved = ExerciseStudentGrade.builder().id(1).studentId(1).exerciseId(1).grade(8.0).build();
            when(exerciseStudentGradeService.createGrade(1, input)).thenReturn(saved);

            final ExerciseStudentGrade result = exerciseStudentGradeUseCase.createGrade(1, input);

            assertThat(result).isEqualTo(saved);
            verify(exerciseStudentGradeService).createGrade(1, input);
        }
    }

    @Nested
    class UpdateGrade {

        @Test
        void when_updating_grade_expect_delegated_to_service() {
            final ExerciseStudentGrade input = ExerciseStudentGrade.builder().grade(9.0).build();
            final ExerciseStudentGrade updated = ExerciseStudentGrade.builder().id(1).grade(9.0).build();
            when(exerciseStudentGradeService.updateGrade(1, input)).thenReturn(updated);

            final ExerciseStudentGrade result = exerciseStudentGradeUseCase.updateGrade(1, input);

            assertThat(result).isEqualTo(updated);
            verify(exerciseStudentGradeService).updateGrade(1, input);
        }
    }

    @Nested
    class DeleteGrade {

        @Test
        void when_deleting_grade_expect_documents_deleted_before_grade() {
            exerciseStudentGradeUseCase.deleteGrade(1);

            final var order = inOrder(exerciseStudentDocumentService, exerciseStudentGradeService);
            order.verify(exerciseStudentDocumentService).deleteDocumentsByGradeId(1);
            order.verify(exerciseStudentGradeService).deleteGrade(1);
        }
    }
}
