package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.service.teachernotebook.GradeExportService;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeExportUseCaseImplTest {

    @Mock
    private GradeExportService gradeExportService;

    @InjectMocks
    private GradeExportUseCaseImpl gradeExportUseCase;

    @Test
    void exportGradesByClassId_shouldDelegateToService() {
        byte[] expected = new byte[]{1, 2, 3};
        when(gradeExportService.exportGradesByClassId(1)).thenReturn(expected);

        byte[] result = gradeExportUseCase.exportGradesByClassId(1);

        assertArrayEquals(expected, result);
        verify(gradeExportService).exportGradesByClassId(1);
    }
}

