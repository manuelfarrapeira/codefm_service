package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentDocumentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseStudentDocumentUseCaseImplTest {

    private ExerciseStudentDocumentUseCaseImpl exerciseStudentDocumentUseCase;

    @Mock
    private ExerciseStudentDocumentService exerciseStudentDocumentService;

    private static final Integer GRADE_ID = 10;
    private static final Integer DOCUMENT_ID = 200;

    @BeforeEach
    void beforeEach() {
        exerciseStudentDocumentUseCase = new ExerciseStudentDocumentUseCaseImpl(exerciseStudentDocumentService);
    }

    @Nested
    class UploadDocument {

        @Test
        void when_uploading_document_expect_delegated_to_service() {
            final MultipartFile file = mock(MultipartFile.class);
            final ExerciseStudentDocument expected = ExerciseStudentDocument.builder().id(1).gradeId(GRADE_ID).build();
            when(exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc")).thenReturn(expected);

            final ExerciseStudentDocument result = exerciseStudentDocumentUseCase.uploadDocument(GRADE_ID, file, "desc");

            assertThat(result).isEqualTo(expected);
            verify(exerciseStudentDocumentService).uploadDocument(GRADE_ID, file, "desc");
        }
    }

    @Nested
    class DownloadDocument {

        @Test
        void when_downloading_document_expect_delegated_to_service() {
            final byte[] expected = "content".getBytes();
            when(exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID)).thenReturn(expected);

            final byte[] result = exerciseStudentDocumentUseCase.downloadDocument(GRADE_ID, DOCUMENT_ID);

            assertThat(result).isEqualTo(expected);
            verify(exerciseStudentDocumentService).downloadDocument(GRADE_ID, DOCUMENT_ID);
        }
    }

    @Nested
    class GetDocumentFilename {

        @Test
        void when_getting_filename_expect_delegated_to_service() {
            when(exerciseStudentDocumentService.getDocumentFilename(GRADE_ID, DOCUMENT_ID)).thenReturn("file.pdf");

            final String result = exerciseStudentDocumentUseCase.getDocumentFilename(GRADE_ID, DOCUMENT_ID);

            assertThat(result).isEqualTo("file.pdf");
            verify(exerciseStudentDocumentService).getDocumentFilename(GRADE_ID, DOCUMENT_ID);
        }
    }

    @Nested
    class UpdateDescription {

        @Test
        void when_updating_description_expect_delegated_to_service() {
            final ExerciseStudentDocument expected = ExerciseStudentDocument.builder().id(DOCUMENT_ID).description("new").build();
            when(exerciseStudentDocumentService.updateDescription(GRADE_ID, DOCUMENT_ID, "new")).thenReturn(expected);

            final ExerciseStudentDocument result = exerciseStudentDocumentUseCase.updateDescription(GRADE_ID, DOCUMENT_ID, "new");

            assertThat(result).isEqualTo(expected);
            verify(exerciseStudentDocumentService).updateDescription(GRADE_ID, DOCUMENT_ID, "new");
        }
    }

    @Nested
    class DeleteDocument {

        @Test
        void when_deleting_document_expect_delegated_to_service() {
            doNothing().when(exerciseStudentDocumentService).deleteDocument(GRADE_ID, DOCUMENT_ID);

            exerciseStudentDocumentUseCase.deleteDocument(GRADE_ID, DOCUMENT_ID);

            verify(exerciseStudentDocumentService).deleteDocument(GRADE_ID, DOCUMENT_ID);
        }
    }
}

