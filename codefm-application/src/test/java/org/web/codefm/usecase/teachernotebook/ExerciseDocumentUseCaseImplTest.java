package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseDocumentUseCaseImplTest {

    private ExerciseDocumentUseCaseImpl exerciseDocumentUseCase;

    @Mock
    private ExerciseDocumentService exerciseDocumentService;

    private static final Integer EXERCISE_ID = 100;
    private static final Integer DOCUMENT_ID = 200;

    @BeforeEach
    void beforeEach() {
        exerciseDocumentUseCase = new ExerciseDocumentUseCaseImpl(exerciseDocumentService);
    }

    @Nested
    class UploadDocument {

        @Test
        void when_uploading_document_expect_delegated_to_service() {
            final MultipartFile file = mock(MultipartFile.class);
            final ExerciseDocument expected = ExerciseDocument.builder()
                    .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("file.pdf").build();
            when(exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc")).thenReturn(expected);

            final ExerciseDocument result = exerciseDocumentUseCase.uploadDocument(EXERCISE_ID, file, "desc");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(DOCUMENT_ID);
            verify(exerciseDocumentService).uploadDocument(EXERCISE_ID, file, "desc");
        }
    }

    @Nested
    class DownloadDocument {

        @Test
        void when_downloading_document_expect_delegated_to_service() {
            final byte[] expectedBytes = "content".getBytes();
            when(exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID)).thenReturn(expectedBytes);

            final byte[] result = exerciseDocumentUseCase.downloadDocument(EXERCISE_ID, DOCUMENT_ID);

            assertThat(result).isNotNull();
            assertThat(result.length).isEqualTo(expectedBytes.length);
            verify(exerciseDocumentService).downloadDocument(EXERCISE_ID, DOCUMENT_ID);
        }
    }

    @Nested
    class GetDocumentFilename {

        @Test
        void when_getting_filename_expect_delegated_to_service() {
            final String expectedFilename = "100_test_abc12345.pdf";
            when(exerciseDocumentService.getDocumentFilename(EXERCISE_ID, DOCUMENT_ID)).thenReturn(expectedFilename);

            final String result = exerciseDocumentUseCase.getDocumentFilename(EXERCISE_ID, DOCUMENT_ID);

            assertThat(result).isEqualTo(expectedFilename);
            verify(exerciseDocumentService).getDocumentFilename(EXERCISE_ID, DOCUMENT_ID);
        }
    }

    @Nested
    class UpdateDescription {

        @Test
        void when_updating_description_expect_delegated_to_service() {
            final ExerciseDocument expected = ExerciseDocument.builder()
                    .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("file.pdf").description("Updated").build();
            when(exerciseDocumentService.updateDescription(EXERCISE_ID, DOCUMENT_ID, "Updated")).thenReturn(expected);

            final ExerciseDocument result = exerciseDocumentUseCase.updateDescription(EXERCISE_ID, DOCUMENT_ID, "Updated");

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEqualTo("Updated");
            verify(exerciseDocumentService).updateDescription(EXERCISE_ID, DOCUMENT_ID, "Updated");
        }
    }

    @Nested
    class DeleteDocument {

        @Test
        void when_deleting_document_expect_delegated_to_service() {
            exerciseDocumentUseCase.deleteDocument(EXERCISE_ID, DOCUMENT_ID);

            verify(exerciseDocumentService).deleteDocument(EXERCISE_ID, DOCUMENT_ID);
        }
    }
}

