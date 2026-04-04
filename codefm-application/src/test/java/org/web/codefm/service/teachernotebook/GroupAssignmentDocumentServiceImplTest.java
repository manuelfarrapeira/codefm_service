package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentDocumentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentDocumentUploadException;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.GroupAssignmentDocumentRepository;
import org.web.codefm.domain.repository.teachernotebook.GroupAssignmentRepository;
import org.web.codefm.domain.repository.teachernotebook.SavedStudentGroupRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentDocumentServiceImplTest {

    private static final Integer ASSIGNMENT_ID = 100;
    private static final Integer DOCUMENT_ID = 200;
    private static final Integer GROUP_ID = 50;
    private static final Integer TEACHER_ID = 10;
    private static final Integer CLASS_ID = 1;

    @Mock
    private GroupAssignmentDocumentRepository groupAssignmentDocumentRepository;

    @Mock
    private GroupAssignmentRepository groupAssignmentRepository;

    @Mock
    private SavedStudentGroupRepository savedStudentGroupRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private GroupAssignmentDocumentServiceImpl groupAssignmentDocumentService;

    @TempDir
    Path tempDir;

    @Test
    void uploadDocument_shouldSaveDocument_whenValid() throws IOException {
        setupSessionMocks();
        setupDocumentsDirectory();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));

        final MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("test content".getBytes());
        when(file.getOriginalFilename()).thenReturn("document.pdf");

        final GroupAssignmentDocument saved = GroupAssignmentDocument.builder()
                .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document("saved.pdf").groupDocument(false).build();
        when(this.groupAssignmentDocumentRepository.save(any(GroupAssignmentDocument.class))).thenReturn(saved);

        final GroupAssignmentDocument result = this.groupAssignmentDocumentService.uploadDocument(
                ASSIGNMENT_ID, null, file, "Description", false);

        assertEquals(saved, result);
        verify(this.groupAssignmentDocumentRepository).save(any(GroupAssignmentDocument.class));
    }

    @Test
    void uploadDocument_shouldSaveGroupDocument_whenGroupDocumentIsTrue() throws IOException {
        setupSessionMocks();
        setupDocumentsDirectory();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
        when(this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                .thenReturn(Optional.of(SavedStudentGroup.builder().id(GROUP_ID).classId(CLASS_ID).build()));

        final MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("test content".getBytes());
        when(file.getOriginalFilename()).thenReturn("document.pdf");

        final GroupAssignmentDocument saved = GroupAssignmentDocument.builder()
                .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).document("saved.pdf").groupDocument(true).build();
        when(this.groupAssignmentDocumentRepository.save(any(GroupAssignmentDocument.class))).thenReturn(saved);

        final GroupAssignmentDocument result = this.groupAssignmentDocumentService.uploadDocument(
                ASSIGNMENT_ID, GROUP_ID, file, "Description", true);

        assertEquals(saved, result);
        verify(this.savedStudentGroupRepository).findByIdAndTeacherId(GROUP_ID, TEACHER_ID);
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenFileIsEmpty() {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_EMPTY), any(), any(Locale.class)))
                .thenReturn("Document file is required.");

        final MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(GroupAssignmentDocumentUploadException.class,
                () -> this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, null, false));
    }

    @Test
    void uploadDocument_shouldThrowValidationException_whenGroupNotFound() {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
        when(this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Group not found.");

        final MultipartFile file = mock(MultipartFile.class);

        final GroupAssignmentValidationException ex = assertThrows(GroupAssignmentValidationException.class,
                () -> this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, GROUP_ID, file, null, true));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("groupId", ex.getErrors().get(0).getParam());
    }

    @Test
    void uploadDocument_shouldThrowValidationException_whenGroupNotInClass() {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
        when(this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                .thenReturn(Optional.of(SavedStudentGroup.builder().id(GROUP_ID).classId(999).build()));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_IN_CLASS), any(), any(Locale.class)))
                .thenReturn("Group not in class.");

        final MultipartFile file = mock(MultipartFile.class);

        final GroupAssignmentValidationException ex = assertThrows(GroupAssignmentValidationException.class,
                () -> this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, GROUP_ID, file, null, true));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("groupId", ex.getErrors().get(0).getParam());
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenFileSizeExceeded() throws IOException {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_SIZE_EXCEEDED), any(), any(Locale.class)))
                .thenReturn("File size exceeds maximum.");

        final MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        final byte[] largeContent = new byte[3 * 1024 * 1024];
        when(file.getBytes()).thenReturn(largeContent);
        when(file.getOriginalFilename()).thenReturn("large.pdf");

        assertThrows(GroupAssignmentDocumentUploadException.class,
                () -> this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, null, false));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenInvalidExtension() throws IOException {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_INVALID_EXTENSION), any(), any(Locale.class)))
                .thenReturn("Extension not allowed.");

        final MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("test".getBytes());
        when(file.getOriginalFilename()).thenReturn("document.exe");

        assertThrows(GroupAssignmentDocumentUploadException.class,
                () -> this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, null, false));
    }

    @Test
    void uploadDocument_shouldThrowNotFoundException_whenAssignmentNotOwned() {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Not found.");

        final MultipartFile file = mock(MultipartFile.class);

        assertThrows(GroupAssignmentNotFoundException.class,
                () -> this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, null, false));
    }

    @Test
    void downloadDocument_shouldReturnBytes_whenDocumentExists() throws IOException {
        setupSessionMocks();
        setupDocumentsDirectory();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));

        final String filename = "test_doc.pdf";
        Files.write(tempDir.resolve(filename), "file content".getBytes());
        final GroupAssignmentDocument doc = GroupAssignmentDocument.builder()
                .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document(filename).build();
        when(this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));

        final byte[] result = this.groupAssignmentDocumentService.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID);

        assertNotNull(result);
        assertEquals("file content", new String(result));
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenDocumentNotFound() {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));
        when(this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Not found.");

        assertThrows(GroupAssignmentDocumentNotFoundException.class,
                () -> this.groupAssignmentDocumentService.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID));
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenDocumentBelongsToDifferentAssignment() {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));
        final GroupAssignmentDocument doc = GroupAssignmentDocument.builder()
                .id(DOCUMENT_ID).groupAssignmentId(999).document("file.pdf").build();
        when(this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Not found.");

        assertThrows(GroupAssignmentDocumentNotFoundException.class,
                () -> this.groupAssignmentDocumentService.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID));
    }

    @Test
    void getDocumentFilename_shouldReturnFilename_whenDocumentExists() {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));
        final GroupAssignmentDocument doc = GroupAssignmentDocument.builder()
                .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document("file.pdf").build();
        when(this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));

        final String result = this.groupAssignmentDocumentService.getDocumentFilename(ASSIGNMENT_ID, DOCUMENT_ID);

        assertEquals("file.pdf", result);
    }

    @Test
    void deleteDocument_shouldDeleteRecordAndFile_whenDocumentExists() throws IOException {
        setupSessionMocks();
        setupDocumentsDirectory();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));

        final String filename = "to_delete.pdf";
        Files.write(tempDir.resolve(filename), "content".getBytes());
        final GroupAssignmentDocument doc = GroupAssignmentDocument.builder()
                .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document(filename).build();
        when(this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));

        this.groupAssignmentDocumentService.deleteDocument(ASSIGNMENT_ID, DOCUMENT_ID);

        verify(this.groupAssignmentDocumentRepository).deleteById(DOCUMENT_ID);
        assertFalse(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void deleteDocumentsByGroupAssignmentId_shouldDeleteAllDocumentsAndFiles() throws IOException {
        setupDocumentsDirectory();
        final String file1 = "doc1.pdf";
        final String file2 = "doc2.pdf";
        Files.write(tempDir.resolve(file1), "content1".getBytes());
        Files.write(tempDir.resolve(file2), "content2".getBytes());

        final List<GroupAssignmentDocument> docs = List.of(
                GroupAssignmentDocument.builder().id(1).document(file1).build(),
                GroupAssignmentDocument.builder().id(2).document(file2).build());
        when(this.groupAssignmentDocumentRepository.findByAssignmentId(ASSIGNMENT_ID)).thenReturn(docs);

        this.groupAssignmentDocumentService.deleteDocumentsByGroupAssignmentId(ASSIGNMENT_ID);

        verify(this.groupAssignmentDocumentRepository).deleteByGroupAssignmentId(ASSIGNMENT_ID);
        assertFalse(Files.exists(tempDir.resolve(file1)));
        assertFalse(Files.exists(tempDir.resolve(file2)));
    }

    @Test
    void deleteDocumentsByGroupAssignmentIds_shouldDoNothing_whenListIsEmpty() {
        this.groupAssignmentDocumentService.deleteDocumentsByGroupAssignmentIds(List.of());

        verifyNoInteractions(this.groupAssignmentDocumentRepository);
    }

    @Test
    void deleteDocumentsByGroupAssignmentIds_shouldDoNothing_whenListIsNull() {
        this.groupAssignmentDocumentService.deleteDocumentsByGroupAssignmentIds(null);

        verifyNoInteractions(this.groupAssignmentDocumentRepository);
    }

    @Test
    void deleteDocumentsByGroupId_shouldDeleteDocumentsAndFiles() throws IOException {
        setupDocumentsDirectory();
        final String file1 = "gdoc1.pdf";
        Files.write(tempDir.resolve(file1), "content".getBytes());

        final List<GroupAssignmentDocument> docs = List.of(
                GroupAssignmentDocument.builder().id(1).document(file1).build());
        when(this.groupAssignmentDocumentRepository.findByGroupIds(List.of(GROUP_ID))).thenReturn(docs);

        this.groupAssignmentDocumentService.deleteDocumentsByGroupId(GROUP_ID);

        verify(this.groupAssignmentDocumentRepository).deleteByGroupId(GROUP_ID);
        assertFalse(Files.exists(tempDir.resolve(file1)));
    }

    @Test
    void deleteDocumentsByGroupIds_shouldDoNothing_whenListIsEmpty() {
        this.groupAssignmentDocumentService.deleteDocumentsByGroupIds(List.of());

        verifyNoInteractions(this.groupAssignmentDocumentRepository);
    }

    @Test
    void deleteDocumentsByGroupIds_shouldDoNothing_whenListIsNull() {
        this.groupAssignmentDocumentService.deleteDocumentsByGroupIds(null);

        verifyNoInteractions(this.groupAssignmentDocumentRepository);
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenIOExceptionOnRead() throws IOException {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_UPLOAD_ERROR), any(), any(Locale.class)))
                .thenReturn("Upload error.");

        final MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenThrow(new IOException("Disk error"));

        assertThrows(GroupAssignmentDocumentUploadException.class,
                () -> this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, null, false));
    }

    private void setupSessionMocks() {
        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    }

    private void setupDocumentsDirectory() {
        ReflectionTestUtils.setField(this.groupAssignmentDocumentService, "documentsDirectory", tempDir.toString());
    }
}

