package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.service.teachernotebook.GradeExportService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeExportUseCaseImplTest {

    private GradeExportUseCaseImpl gradeExportUseCase;

    @Mock
    private GradeExportService gradeExportService;

    @BeforeEach
    void beforeEach() {
        gradeExportUseCase = new GradeExportUseCaseImpl(gradeExportService);
    }

    @Nested
    class ExportGradesByClassId {

        @Test
        void when_exporting_grades_expect_delegated_to_service() {
            final byte[] expected = new byte[]{1, 2, 3};
            when(gradeExportService.exportGradesByClassId(1)).thenReturn(expected);

            final byte[] result = gradeExportUseCase.exportGradesByClassId(1);

            assertThat(result).isEqualTo(expected);
            verify(gradeExportService).exportGradesByClassId(1);
        }
    }
}

