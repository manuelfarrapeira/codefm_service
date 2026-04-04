package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.domain.service.teachernotebook.GroupAssignmentDocumentService;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentDocumentUseCaseImplTest {

    private static final Integer ASSIGNMENT_ID = 100;
    private static final Integer GROUP_ID = 50;
    private static final Integer DOCUMENT_ID = 200;

    @Mock
    private GroupAssignmentDocumentService groupAssignmentDocumentService;

    @InjectMocks
    private GroupAssignmentDocumentUseCaseImpl groupAssignmentDocumentUseCase;

    @Test
    void uploadAssignmentDocument_shouldDelegateToServiceWithGroupDocumentFalse() {
        final MultipartFile file = mock(MultipartFile.class);
        final GroupAssignmentDocument expected = GroupAssignmentDocument.builder()
                .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document("file.pdf").groupDocument(false).build();
        when(this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, "desc", false))
                .thenReturn(expected);

        final GroupAssignmentDocument result = this.groupAssignmentDocumentUseCase.uploadAssignmentDocument(ASSIGNMENT_ID, file, "desc");

        assertEquals(expected, result);
        verify(this.groupAssignmentDocumentService).uploadDocument(ASSIGNMENT_ID, null, file, "desc", false);
    }

    @Test
    void uploadGroupDocument_shouldDelegateToServiceWithGroupDocumentTrue() {
        final MultipartFile file = mock(MultipartFile.class);
        final GroupAssignmentDocument expected = GroupAssignmentDocument.builder()
                .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).document("file.pdf").groupDocument(true).build();
        when(this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, GROUP_ID, file, "desc", true))
                .thenReturn(expected);

        final GroupAssignmentDocument result = this.groupAssignmentDocumentUseCase.uploadGroupDocument(ASSIGNMENT_ID, GROUP_ID, file, "desc");

        assertEquals(expected, result);
        verify(this.groupAssignmentDocumentService).uploadDocument(ASSIGNMENT_ID, GROUP_ID, file, "desc", true);
    }

    @Test
    void downloadDocument_shouldDelegateToService() {
        final byte[] expected = "content".getBytes();
        when(this.groupAssignmentDocumentService.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID)).thenReturn(expected);

        final byte[] result = this.groupAssignmentDocumentUseCase.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID);

        assertArrayEquals(expected, result);
        verify(this.groupAssignmentDocumentService).downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID);
    }

    @Test
    void getDocumentFilename_shouldDelegateToService() {
        when(this.groupAssignmentDocumentService.getDocumentFilename(ASSIGNMENT_ID, DOCUMENT_ID))
                .thenReturn("file.pdf");

        final String result = this.groupAssignmentDocumentUseCase.getDocumentFilename(ASSIGNMENT_ID, DOCUMENT_ID);

        assertEquals("file.pdf", result);
        verify(this.groupAssignmentDocumentService).getDocumentFilename(ASSIGNMENT_ID, DOCUMENT_ID);
    }

    @Test
    void deleteDocument_shouldDelegateToService() {
        doNothing().when(this.groupAssignmentDocumentService).deleteDocument(ASSIGNMENT_ID, DOCUMENT_ID);

        this.groupAssignmentDocumentUseCase.deleteDocument(ASSIGNMENT_ID, DOCUMENT_ID);

        verify(this.groupAssignmentDocumentService).deleteDocument(ASSIGNMENT_ID, DOCUMENT_ID);
    }
}

