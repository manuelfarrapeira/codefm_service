package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseDocumentUseCaseImplTest {

    @Mock
    private ExerciseDocumentService exerciseDocumentService;

    @InjectMocks
    private ExerciseDocumentUseCaseImpl exerciseDocumentUseCase;

    private static final Integer EXERCISE_ID = 100;
    private static final Integer DOCUMENT_ID = 200;

    @Test
    void uploadDocument_shouldDelegateToService() {
        MultipartFile file = mock(MultipartFile.class);
        ExerciseDocument expected = ExerciseDocument.builder()
                .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("file.pdf").build();

        when(exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc")).thenReturn(expected);

        ExerciseDocument result = exerciseDocumentUseCase.uploadDocument(EXERCISE_ID, file, "desc");

        assertNotNull(result);
        assertEquals(DOCUMENT_ID, result.getId());
        verify(exerciseDocumentService).uploadDocument(EXERCISE_ID, file, "desc");
    }

    @Test
    void downloadDocument_shouldDelegateToService() {
        byte[] expectedBytes = "content".getBytes();

        when(exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID)).thenReturn(expectedBytes);

        byte[] result = exerciseDocumentUseCase.downloadDocument(EXERCISE_ID, DOCUMENT_ID);

        assertNotNull(result);
        assertEquals(expectedBytes.length, result.length);
        verify(exerciseDocumentService).downloadDocument(EXERCISE_ID, DOCUMENT_ID);
    }

    @Test
    void getDocumentFilename_shouldDelegateToService() {
        String expectedFilename = "100_test_abc12345.pdf";

        when(exerciseDocumentService.getDocumentFilename(EXERCISE_ID, DOCUMENT_ID)).thenReturn(expectedFilename);

        String result = exerciseDocumentUseCase.getDocumentFilename(EXERCISE_ID, DOCUMENT_ID);

        assertEquals(expectedFilename, result);
        verify(exerciseDocumentService).getDocumentFilename(EXERCISE_ID, DOCUMENT_ID);
    }

    @Test
    void updateDescription_shouldDelegateToService() {
        ExerciseDocument expected = ExerciseDocument.builder()
                .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("file.pdf").description("Updated").build();

        when(exerciseDocumentService.updateDescription(EXERCISE_ID, DOCUMENT_ID, "Updated")).thenReturn(expected);

        ExerciseDocument result = exerciseDocumentUseCase.updateDescription(EXERCISE_ID, DOCUMENT_ID, "Updated");

        assertNotNull(result);
        assertEquals("Updated", result.getDescription());
        verify(exerciseDocumentService).updateDescription(EXERCISE_ID, DOCUMENT_ID, "Updated");
    }

    @Test
    void deleteDocument_shouldDelegateToService() {
        exerciseDocumentUseCase.deleteDocument(EXERCISE_ID, DOCUMENT_ID);

        verify(exerciseDocumentService).deleteDocument(EXERCISE_ID, DOCUMENT_ID);
    }
}

