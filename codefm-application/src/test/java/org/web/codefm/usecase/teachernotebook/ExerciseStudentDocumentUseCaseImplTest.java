package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentDocumentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseStudentDocumentUseCaseImplTest {

    @Mock
    private ExerciseStudentDocumentService exerciseStudentDocumentService;

    @InjectMocks
    private ExerciseStudentDocumentUseCaseImpl exerciseStudentDocumentUseCase;

    private static final Integer GRADE_ID = 10;
    private static final Integer DOCUMENT_ID = 200;

    @Test
    void uploadDocument_shouldDelegateToService() {
        final MultipartFile file = mock(MultipartFile.class);
        final ExerciseStudentDocument expected = ExerciseStudentDocument.builder().id(1).gradeId(GRADE_ID).build();

        when(exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc")).thenReturn(expected);

        final ExerciseStudentDocument result = exerciseStudentDocumentUseCase.uploadDocument(GRADE_ID, file, "desc");

        assertEquals(expected, result);
        verify(exerciseStudentDocumentService).uploadDocument(GRADE_ID, file, "desc");
    }

    @Test
    void downloadDocument_shouldDelegateToService() {
        final byte[] expected = "content".getBytes();

        when(exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID)).thenReturn(expected);

        final byte[] result = exerciseStudentDocumentUseCase.downloadDocument(GRADE_ID, DOCUMENT_ID);

        assertEquals(expected, result);
        verify(exerciseStudentDocumentService).downloadDocument(GRADE_ID, DOCUMENT_ID);
    }

    @Test
    void getDocumentFilename_shouldDelegateToService() {
        when(exerciseStudentDocumentService.getDocumentFilename(GRADE_ID, DOCUMENT_ID)).thenReturn("file.pdf");

        final String result = exerciseStudentDocumentUseCase.getDocumentFilename(GRADE_ID, DOCUMENT_ID);

        assertEquals("file.pdf", result);
        verify(exerciseStudentDocumentService).getDocumentFilename(GRADE_ID, DOCUMENT_ID);
    }

    @Test
    void updateDescription_shouldDelegateToService() {
        final ExerciseStudentDocument expected = ExerciseStudentDocument.builder().id(DOCUMENT_ID).description("new").build();

        when(exerciseStudentDocumentService.updateDescription(GRADE_ID, DOCUMENT_ID, "new")).thenReturn(expected);

        final ExerciseStudentDocument result = exerciseStudentDocumentUseCase.updateDescription(GRADE_ID, DOCUMENT_ID, "new");

        assertEquals(expected, result);
        verify(exerciseStudentDocumentService).updateDescription(GRADE_ID, DOCUMENT_ID, "new");
    }

    @Test
    void deleteDocument_shouldDelegateToService() {
        doNothing().when(exerciseStudentDocumentService).deleteDocument(GRADE_ID, DOCUMENT_ID);

        exerciseStudentDocumentUseCase.deleteDocument(GRADE_ID, DOCUMENT_ID);

        verify(exerciseStudentDocumentService).deleteDocument(GRADE_ID, DOCUMENT_ID);
    }
}

