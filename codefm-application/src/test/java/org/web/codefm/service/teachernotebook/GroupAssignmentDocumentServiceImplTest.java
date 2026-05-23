package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
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

import static org.assertj.core.api.Assertions.*;
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

    @TempDir
    Path tempDir;

    private GroupAssignmentDocumentServiceImpl groupAssignmentDocumentService;

    @BeforeEach
    void beforeEach() {
        this.groupAssignmentDocumentService = new GroupAssignmentDocumentServiceImpl(
                this.groupAssignmentDocumentRepository,
                this.groupAssignmentRepository,
                this.savedStudentGroupRepository,
                this.messageSource,
                this.sessionUser
        );
        lenient().when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    }

    @Nested
    class UploadDocument {

        @Test
        void when_input_is_valid_expect_save_document() throws IOException {
            GroupAssignmentDocumentServiceImplTest.this.setupDocumentsDirectory();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));

            final MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn("test content".getBytes());
            when(file.getOriginalFilename()).thenReturn("document.pdf");

            final GroupAssignmentDocument saved = GroupAssignmentDocument.builder()
                    .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document("saved.pdf").groupDocument(false).build();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.save(any(GroupAssignmentDocument.class))).thenReturn(saved);

            final GroupAssignmentDocument result = GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.uploadDocument(
                    ASSIGNMENT_ID, null, file, "Description", false);

            assertThat(result).isEqualTo(saved);
            verify(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository).save(any(GroupAssignmentDocument.class));
        }

        @Test
        void when_group_document_is_true_expect_save_group_document() throws IOException {
            GroupAssignmentDocumentServiceImplTest.this.setupDocumentsDirectory();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                    .thenReturn(Optional.of(SavedStudentGroup.builder().id(GROUP_ID).classId(CLASS_ID).build()));

            final MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn("test content".getBytes());
            when(file.getOriginalFilename()).thenReturn("document.pdf");

            final GroupAssignmentDocument saved = GroupAssignmentDocument.builder()
                    .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).document("saved.pdf").groupDocument(true).build();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.save(any(GroupAssignmentDocument.class))).thenReturn(saved);

            final GroupAssignmentDocument result = GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.uploadDocument(
                    ASSIGNMENT_ID, GROUP_ID, file, "Description", true);

            assertThat(result).isEqualTo(saved);
            verify(GroupAssignmentDocumentServiceImplTest.this.savedStudentGroupRepository).findByIdAndTeacherId(GROUP_ID, TEACHER_ID);
        }

        @Test
        void when_file_is_empty_expect_throw_upload_exception() {
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_EMPTY), any(), any(Locale.class)))
                    .thenReturn("Document file is required.");

            final MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);
            final ThrowingCallable action = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, null, false);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentDocumentUploadException.class);
        }

        @Test
        void when_group_is_not_found_expect_throw_validation_exception() {
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Group not found.");

            final MultipartFile file = mock(MultipartFile.class);
            final ThrowingCallable call = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, GROUP_ID, file, null, true);
            final Throwable throwable = catchThrowable(call);

            assertThat(throwable).isInstanceOf(GroupAssignmentValidationException.class);
            final GroupAssignmentValidationException ex = (GroupAssignmentValidationException) throwable;
            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("groupId");
        }

        @Test
        void when_group_is_not_in_class_expect_throw_validation_exception() {
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                    .thenReturn(Optional.of(SavedStudentGroup.builder().id(GROUP_ID).classId(999).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_IN_CLASS), any(), any(Locale.class)))
                    .thenReturn("Group not in class.");

            final MultipartFile file = mock(MultipartFile.class);
            final ThrowingCallable call = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, GROUP_ID, file, null, true);
            final Throwable throwable = catchThrowable(call);

            assertThat(throwable).isInstanceOf(GroupAssignmentValidationException.class);
            final GroupAssignmentValidationException ex = (GroupAssignmentValidationException) throwable;
            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("groupId");
        }

        @Test
        void when_file_size_is_exceeded_expect_throw_upload_exception() throws IOException {
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_SIZE_EXCEEDED), any(), any(Locale.class)))
                    .thenReturn("File size exceeds maximum.");

            final MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            final byte[] largeContent = new byte[3 * 1024 * 1024];
            when(file.getBytes()).thenReturn(largeContent);
            when(file.getOriginalFilename()).thenReturn("large.pdf");
            final ThrowingCallable action = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, null, false);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentDocumentUploadException.class);
        }

        @Test
        void when_extension_is_invalid_expect_throw_upload_exception() throws IOException {
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_INVALID_EXTENSION), any(), any(Locale.class)))
                    .thenReturn("Extension not allowed.");

            final MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn("test".getBytes());
            when(file.getOriginalFilename()).thenReturn("document.exe");
            final ThrowingCallable action = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, null, false);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentDocumentUploadException.class);
        }

        @Test
        void when_assignment_is_not_owned_expect_throw_not_found_exception() {
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Not found.");

            final MultipartFile file = mock(MultipartFile.class);
            final ThrowingCallable action = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, null, false);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentNotFoundException.class);
        }

        @Test
        void when_io_exception_occurs_on_read_expect_throw_upload_exception() throws IOException {
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_UPLOAD_ERROR), any(), any(Locale.class)))
                    .thenReturn("Upload error.");

            final MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenThrow(new IOException("Disk error"));
            final ThrowingCallable action = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, null, false);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentDocumentUploadException.class);
        }

        @Test
        void when_io_exception_occurs_on_disk_write_expect_throw_upload_exception() throws IOException {
            ReflectionTestUtils.setField(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService, "documentsDirectory", "/nonexistent/invalid/path");
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_UPLOAD_ERROR), any(), any(Locale.class)))
                    .thenReturn("Upload error.");

            final MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn("test content".getBytes());
            when(file.getOriginalFilename()).thenReturn("document.pdf");
            final ThrowingCallable action = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.uploadDocument(ASSIGNMENT_ID, null, file, null, false);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentDocumentUploadException.class);
        }
    }

    @Nested
    class DownloadDocument {

        @Test
        void when_document_exists_expect_return_bytes() throws IOException {
            GroupAssignmentDocumentServiceImplTest.this.setupDocumentsDirectory();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));

            final String filename = "test_doc.pdf";
            Files.write(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(filename), "file content".getBytes());
            final GroupAssignmentDocument doc = GroupAssignmentDocument.builder()
                    .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document(filename).build();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));

            final byte[] result = GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID);

            assertThat(result).isNotNull();
            assertThat(new String(result)).isEqualTo("file content");
        }

        @Test
        void when_document_is_not_found_expect_throw_not_found_exception() {
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Not found.");

            final ThrowingCallable action = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentDocumentNotFoundException.class);
        }

        @Test
        void when_document_belongs_to_different_assignment_expect_throw_not_found_exception() {
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));
            final GroupAssignmentDocument doc = GroupAssignmentDocument.builder()
                    .id(DOCUMENT_ID).groupAssignmentId(999).document("file.pdf").build();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Not found.");

            final ThrowingCallable action = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentDocumentNotFoundException.class);
        }

        @Test
        void when_file_does_not_exist_on_disk_expect_throw_not_found_exception() {
            GroupAssignmentDocumentServiceImplTest.this.setupDocumentsDirectory();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Not found.");

            final GroupAssignmentDocument doc = GroupAssignmentDocument.builder()
                    .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document("missing_file.pdf").build();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));

            final ThrowingCallable action = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentDocumentNotFoundException.class);
        }

        @Test
        void when_io_exception_occurs_on_read_expect_throw_not_found_exception() throws IOException {
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));
            when(GroupAssignmentDocumentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Not found.");

            final Path subDir = GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve("restricted");
            Files.createDirectory(subDir);
            ReflectionTestUtils.setField(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService, "documentsDirectory", GroupAssignmentDocumentServiceImplTest.this.tempDir.toString());

            final GroupAssignmentDocument doc = GroupAssignmentDocument.builder()
                    .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document("restricted").build();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));

            final ThrowingCallable action = () -> GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.downloadDocument(ASSIGNMENT_ID, DOCUMENT_ID);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentDocumentNotFoundException.class);
        }
    }

    @Nested
    class GetDocumentFilename {

        @Test
        void when_document_exists_expect_return_filename() {
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));
            final GroupAssignmentDocument doc = GroupAssignmentDocument.builder()
                    .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document("file.pdf").build();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));

            final String result = GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.getDocumentFilename(ASSIGNMENT_ID, DOCUMENT_ID);

            assertThat(result).isEqualTo("file.pdf");
        }
    }

    @Nested
    class DeleteDocument {

        @Test
        void when_document_exists_expect_delete_record_and_file() throws IOException {
            GroupAssignmentDocumentServiceImplTest.this.setupDocumentsDirectory();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(GroupAssignment.builder().id(ASSIGNMENT_ID).build()));

            final String filename = "to_delete.pdf";
            Files.write(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(filename), "content".getBytes());
            final GroupAssignmentDocument doc = GroupAssignmentDocument.builder()
                    .id(DOCUMENT_ID).groupAssignmentId(ASSIGNMENT_ID).document(filename).build();
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));

            GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.deleteDocument(ASSIGNMENT_ID, DOCUMENT_ID);

            verify(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository).deleteById(DOCUMENT_ID);
            assertThat(Files.exists(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(filename))).isFalse();
        }
    }

    @Nested
    class DeleteDocumentsByGroupAssignmentId {

        @Test
        void when_documents_exist_expect_delete_all_documents_and_files() throws IOException {
            GroupAssignmentDocumentServiceImplTest.this.setupDocumentsDirectory();
            final String file1 = "doc1.pdf";
            final String file2 = "doc2.pdf";
            Files.write(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file1), "content1".getBytes());
            Files.write(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file2), "content2".getBytes());

            final List<GroupAssignmentDocument> docs = List.of(
                    GroupAssignmentDocument.builder().id(1).document(file1).build(),
                    GroupAssignmentDocument.builder().id(2).document(file2).build());
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.findByAssignmentId(ASSIGNMENT_ID)).thenReturn(docs);

            GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.deleteDocumentsByGroupAssignmentId(ASSIGNMENT_ID);

            verify(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository).deleteByGroupAssignmentId(ASSIGNMENT_ID);
            assertThat(Files.exists(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file1))).isFalse();
            assertThat(Files.exists(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file2))).isFalse();
        }
    }

    @Nested
    class DeleteDocumentsByGroupAssignmentIds {

        @Test
        void when_list_is_empty_expect_do_nothing() {
            GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.deleteDocumentsByGroupAssignmentIds(List.of());

            verifyNoInteractions(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository);
        }

        @Test
        void when_list_is_null_expect_do_nothing() {
            GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.deleteDocumentsByGroupAssignmentIds(null);

            verifyNoInteractions(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository);
        }

        @Test
        void when_list_is_not_empty_expect_delete_all_documents_and_files() throws IOException {
            GroupAssignmentDocumentServiceImplTest.this.setupDocumentsDirectory();
            final String file1 = "batch_doc1.pdf";
            final String file2 = "batch_doc2.pdf";
            Files.write(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file1), "content1".getBytes());
            Files.write(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file2), "content2".getBytes());

            final List<Integer> assignmentIds = List.of(ASSIGNMENT_ID, 101);
            final List<GroupAssignmentDocument> docs = List.of(
                    GroupAssignmentDocument.builder().id(1).document(file1).build(),
                    GroupAssignmentDocument.builder().id(2).document(file2).build());
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.findByGroupAssignmentIds(assignmentIds)).thenReturn(docs);

            GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.deleteDocumentsByGroupAssignmentIds(assignmentIds);

            verify(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository).deleteByGroupAssignmentIds(assignmentIds);
            assertThat(Files.exists(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file1))).isFalse();
            assertThat(Files.exists(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file2))).isFalse();
        }
    }

    @Nested
    class DeleteDocumentsByGroupId {

        @Test
        void when_documents_exist_expect_delete_documents_and_files() throws IOException {
            GroupAssignmentDocumentServiceImplTest.this.setupDocumentsDirectory();
            final String file1 = "gdoc1.pdf";
            Files.write(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file1), "content".getBytes());

            final List<GroupAssignmentDocument> docs = List.of(
                    GroupAssignmentDocument.builder().id(1).document(file1).build());
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.findByGroupIds(List.of(GROUP_ID))).thenReturn(docs);

            GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.deleteDocumentsByGroupId(GROUP_ID);

            verify(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository).deleteByGroupId(GROUP_ID);
            assertThat(Files.exists(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file1))).isFalse();
        }
    }

    @Nested
    class DeleteDocumentsByGroupIds {

        @Test
        void when_list_is_empty_expect_do_nothing() {
            GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.deleteDocumentsByGroupIds(List.of());

            verifyNoInteractions(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository);
        }

        @Test
        void when_list_is_null_expect_do_nothing() {
            GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.deleteDocumentsByGroupIds(null);

            verifyNoInteractions(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository);
        }

        @Test
        void when_list_is_not_empty_expect_delete_documents_and_files() throws IOException {
            GroupAssignmentDocumentServiceImplTest.this.setupDocumentsDirectory();
            final String file1 = "grp_doc1.pdf";
            final String file2 = "grp_doc2.pdf";
            Files.write(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file1), "content1".getBytes());
            Files.write(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file2), "content2".getBytes());

            final List<Integer> groupIds = List.of(GROUP_ID, 51);
            final List<GroupAssignmentDocument> docs = List.of(
                    GroupAssignmentDocument.builder().id(1).document(file1).build(),
                    GroupAssignmentDocument.builder().id(2).document(file2).build());
            when(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository.findByGroupIds(groupIds)).thenReturn(docs);

            GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentService.deleteDocumentsByGroupIds(groupIds);

            verify(GroupAssignmentDocumentServiceImplTest.this.groupAssignmentDocumentRepository).deleteByGroupIds(groupIds);
            assertThat(Files.exists(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file1))).isFalse();
            assertThat(Files.exists(GroupAssignmentDocumentServiceImplTest.this.tempDir.resolve(file2))).isFalse();
        }
    }

    private void setupDocumentsDirectory() {
        ReflectionTestUtils.setField(this.groupAssignmentDocumentService, "documentsDirectory", this.tempDir.toString());
    }
}

