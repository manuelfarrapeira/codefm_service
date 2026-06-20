package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.domain.service.teachernotebook.GroupAssignmentDocumentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentDocumentUseCaseImplTest {

    private GroupAssignmentDocumentUseCaseImpl groupAssignmentDocumentUseCase;

    @Mock
    private GroupAssignmentDocumentService groupAssignmentDocumentService;

    private static final Integer ASSIGNMENT_ID = 100;
    private static final Integer GROUP_ID = 50;
    private static final Integer DOCUMENT_ID = 200;

    @BeforeEach
    void beforeEach() {
        groupAssignmentDocumentUseCase = new GroupAssignmentDocumentUseCaseImpl(groupAssignmentDocumentService);
    }

    @Nested
    class UploadAssignmentDocument {

        @Test
        void when_uploading_assignment_document_expect_group_document_false() {
            final MultipartFile file = mock(MultipartFile.class);
            final GroupAssignmentDocument expected = GroupAssignmentDocument.builder()
                    .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document("file.pdf").groupDocument(false).build();
            when(groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, "desc", false))
                    .thenReturn(expected);

            final GroupAssignmentDocument result = groupAssignmentDocumentUseCase.uploadAssignmentDocument(ASSIGNMENT_ID, file, "desc");

            assertThat(result).isEqualTo(expected);
            verify(groupAssignmentDocumentService).uploadDocument(ASSIGNMENT_ID, null, file, "desc", false);
        }
    }

    @Nested
    class UploadGroupDocument {

        @Test
        void when_uploading_group_document_expect_group_document_true() {
            final MultipartFile file = mock(MultipartFile.class);
            final GroupAssignmentDocument expected = GroupAssignmentDocument.builder()
                    .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).document("file.pdf").groupDocument(true).build();
            when(groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, GROUP_ID, file, "desc", true))
                    .thenReturn(expected);

            final GroupAssignmentDocument result = groupAssignmentDocumentUseCase.uploadGroupDocument(ASSIGNMENT_ID, GROUP_ID, file, "desc");

            assertThat(result).isEqualTo(expected);
            verify(groupAssignmentDocumentService).uploadDocument(ASSIGNMENT_ID, GROUP_ID, file, "desc", true);
        }
    }

    @Nested
    class DownloadDocument {

        @Test
        void when_downloading_document_expect_delegated_to_service() {
            final byte[] expected = "content".getBytes();
            when(groupAssignmentDocumentService.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID)).thenReturn(expected);

            final byte[] result = groupAssignmentDocumentUseCase.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID);

            assertThat(result).isEqualTo(expected);
            verify(groupAssignmentDocumentService).downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID);
        }
    }

    @Nested
    class GetDocumentFilename {

        @Test
        void when_getting_filename_expect_delegated_to_service() {
            when(groupAssignmentDocumentService.getDocumentFilename(ASSIGNMENT_ID, DOCUMENT_ID)).thenReturn("file.pdf");

            final String result = groupAssignmentDocumentUseCase.getDocumentFilename(ASSIGNMENT_ID, DOCUMENT_ID);

            assertThat(result).isEqualTo("file.pdf");
            verify(groupAssignmentDocumentService).getDocumentFilename(ASSIGNMENT_ID, DOCUMENT_ID);
        }
    }

    @Nested
    class DeleteDocument {

        @Test
        void when_deleting_document_expect_delegated_to_service() {
            doNothing().when(groupAssignmentDocumentService).deleteDocument(ASSIGNMENT_ID, DOCUMENT_ID);

            groupAssignmentDocumentUseCase.deleteDocument(ASSIGNMENT_ID, DOCUMENT_ID);

            verify(groupAssignmentDocumentService).deleteDocument(ASSIGNMENT_ID, DOCUMENT_ID);
        }
    }
}
