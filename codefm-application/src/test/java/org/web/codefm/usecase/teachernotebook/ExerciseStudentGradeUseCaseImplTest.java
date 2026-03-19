package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentDocumentService;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentGradeService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseStudentGradeUseCaseImplTest {

    @Mock
    private ExerciseStudentGradeService exerciseStudentGradeService;

    @Mock
    private ExerciseStudentDocumentService exerciseStudentDocumentService;

    @InjectMocks
    private ExerciseStudentGradeUseCaseImpl exerciseStudentGradeUseCase;

    @Test
    void getGradesByClassId_shouldDelegateToService() {
        List<ExerciseStudentGrade> expected = List.of(ExerciseStudentGrade.builder().id(1).build());
        when(exerciseStudentGradeService.getGradesByClassId(1)).thenReturn(expected);

        List<ExerciseStudentGrade> result = exerciseStudentGradeUseCase.getGradesByClassId(1);

        assertEquals(expected, result);
        verify(exerciseStudentGradeService).getGradesByClassId(1);
    }

    @Test
    void getGradesByClassIdAndStudentId_shouldDelegateToService() {
        List<ExerciseStudentGrade> expected = List.of(ExerciseStudentGrade.builder().id(1).build());
        when(exerciseStudentGradeService.getGradesByClassIdAndStudentId(1, 2)).thenReturn(expected);

        List<ExerciseStudentGrade> result = exerciseStudentGradeUseCase.getGradesByClassIdAndStudentId(1, 2);

        assertEquals(expected, result);
        verify(exerciseStudentGradeService).getGradesByClassIdAndStudentId(1, 2);
    }

    @Test
    void createGrade_shouldDelegateToService() {
        ExerciseStudentGrade input = ExerciseStudentGrade.builder().studentId(1).grade(8.0).build();
        ExerciseStudentGrade saved = ExerciseStudentGrade.builder().id(1).studentId(1).exerciseId(1).grade(8.0).build();
        when(exerciseStudentGradeService.createGrade(1, input)).thenReturn(saved);

        ExerciseStudentGrade result = exerciseStudentGradeUseCase.createGrade(1, input);

        assertEquals(saved, result);
        verify(exerciseStudentGradeService).createGrade(1, input);
    }

    @Test
    void updateGrade_shouldDelegateToService() {
        ExerciseStudentGrade input = ExerciseStudentGrade.builder().grade(9.0).build();
        ExerciseStudentGrade updated = ExerciseStudentGrade.builder().id(1).grade(9.0).build();
        when(exerciseStudentGradeService.updateGrade(1, input)).thenReturn(updated);

        ExerciseStudentGrade result = exerciseStudentGradeUseCase.updateGrade(1, input);

        assertEquals(updated, result);
        verify(exerciseStudentGradeService).updateGrade(1, input);
    }

    @Test
    void deleteGrade_shouldDeleteDocumentsBeforeSoftDeleteGrade() {
        exerciseStudentGradeUseCase.deleteGrade(1);

        final var order = inOrder(exerciseStudentDocumentService, exerciseStudentGradeService);
        order.verify(exerciseStudentDocumentService).deleteDocumentsByGradeId(1);
        order.verify(exerciseStudentGradeService).deleteGrade(1);
    }
}
