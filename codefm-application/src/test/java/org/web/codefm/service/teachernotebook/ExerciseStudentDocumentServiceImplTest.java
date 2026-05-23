package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.domain.exception.teachernotebook.ExerciseStudentDocumentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseStudentDocumentUploadException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseStudentGradeNotFoundException;
import org.web.codefm.domain.repository.teachernotebook.ExerciseStudentDocumentRepository;
import org.web.codefm.domain.repository.teachernotebook.ExerciseStudentGradeRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExerciseStudentDocumentServiceImplTest {

    @Mock
    private ExerciseStudentDocumentRepository exerciseStudentDocumentRepository;

    @Mock
    private ExerciseStudentGradeRepository exerciseStudentGradeRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    private ExerciseStudentDocumentServiceImpl exerciseStudentDocumentService;

    @TempDir
    Path tempDir;

    private static final Integer TEACHER_ID = 1;
    private static final Integer GRADE_ID = 10;
    private static final Integer DOCUMENT_ID = 200;

    @BeforeEach
    void beforeEach() {
        exerciseStudentDocumentService = new ExerciseStudentDocumentServiceImpl(
                exerciseStudentDocumentRepository, exerciseStudentGradeRepository, messageSource, sessionUser);
        ReflectionTestUtils.setField(exerciseStudentDocumentService, "documentsDirectory", tempDir.toString());
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");
    }

    @Nested
    class UploadDocument {

        @Test
        void when_grade_exists_and_file_is_valid_expect_save_document_and_return_it() throws IOException {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn("file content".getBytes());
            when(file.getOriginalFilename()).thenReturn("document.pdf");
            when(exerciseStudentDocumentRepository.save(any(ExerciseStudentDocument.class))).thenAnswer(inv -> {
                final ExerciseStudentDocument doc = inv.getArgument(0);
                return ExerciseStudentDocument.builder()
                        .id(DOCUMENT_ID).gradeId(doc.getGradeId())
                        .document(doc.getDocument()).description(doc.getDescription())
                        .build();
            });

            final ExerciseStudentDocument result = exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "Test description");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(DOCUMENT_ID);
            assertThat(result.getGradeId()).isEqualTo(GRADE_ID);
            assertThat(result.getDocument()).startsWith(GRADE_ID + "_document_");
            assertThat(result.getDocument()).endsWith(".pdf");
            verify(exerciseStudentDocumentRepository).save(any(ExerciseStudentDocument.class));
        }

        @Test
        void when_grade_not_found_expect_throw_not_found_exception() {
            final MultipartFile file = mock(MultipartFile.class);
            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentGradeNotFoundException.class);
        }

        @Test
        void when_file_is_empty_expect_throw_upload_exception() throws IOException {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(file.isEmpty()).thenReturn(true);
            when(file.getBytes()).thenReturn(new byte[0]);

            final ThrowingCallable call = () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentUploadException.class);
        }

        @Test
        void when_file_is_null_expect_throw_upload_exception() {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));

            final ThrowingCallable call = () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, null, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentUploadException.class);
        }

        @Test
        void when_file_size_exceeded_expect_throw_upload_exception() throws IOException {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn(new byte[3 * 1024 * 1024]);
            when(file.getOriginalFilename()).thenReturn("large.pdf");

            final ThrowingCallable call = () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentUploadException.class);
        }

        @Test
        void when_extension_is_invalid_expect_throw_upload_exception() throws IOException {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn("content".getBytes());
            when(file.getOriginalFilename()).thenReturn("virus.exe");

            final ThrowingCallable call = () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentUploadException.class);
        }

        @Test
        void when_get_bytes_throws_io_exception_expect_throw_upload_exception() throws IOException {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenThrow(new IOException("Read error"));

            final ThrowingCallable call = () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentUploadException.class);
        }

        @Test
        void when_writing_file_to_disk_fails_expect_throw_upload_exception() throws IOException {
            ReflectionTestUtils.setField(exerciseStudentDocumentService, "documentsDirectory", "/non_existent_path_xyz");
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn("content".getBytes());
            when(file.getOriginalFilename()).thenReturn("document.pdf");

            final ThrowingCallable call = () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentUploadException.class);
        }
    }

    @Nested
    class DownloadDocument {

        @Test
        void when_document_exists_expect_return_file_bytes() throws IOException {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final byte[] expectedBytes = "file content".getBytes();
            final Path file = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), expectedBytes);
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document(file.getFileName().toString()).build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final byte[] result = exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID);

            assertThat(result).isEqualTo(expectedBytes);
        }

        @Test
        void when_grade_not_found_expect_throw_not_found_exception() {
            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentGradeNotFoundException.class);
        }

        @Test
        void when_document_not_found_expect_throw_not_found_exception() {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentNotFoundException.class);
        }

        @Test
        void when_file_does_not_exist_on_disk_expect_throw_not_found_exception() {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document("nonexistent_file.pdf").build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final ThrowingCallable call = () -> exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentNotFoundException.class);
        }

        @Test
        void when_reading_file_throws_io_exception_expect_throw_not_found_exception() throws IOException {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final Path subDir = Files.createDirectory(tempDir.resolve("fakefile.pdf"));
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document(subDir.getFileName().toString()).build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final ThrowingCallable call = () -> exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentNotFoundException.class);
        }

        @Test
        void when_document_belongs_to_different_grade_expect_throw_not_found_exception() {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(999).document("file.pdf").build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final ThrowingCallable call = () -> exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentNotFoundException.class);
        }
    }

    @Nested
    class UpdateDescription {

        @Test
        void when_document_exists_expect_update_and_return_document() {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document("file.pdf").description("old").build();
            final ExerciseStudentDocument updated = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document("file.pdf").description("new desc").build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));
            when(exerciseStudentDocumentRepository.update(any())).thenReturn(updated);

            final ExerciseStudentDocument result = exerciseStudentDocumentService.updateDescription(GRADE_ID, DOCUMENT_ID, "new desc");

            assertThat(result.getDescription()).isEqualTo("new desc");
            verify(exerciseStudentDocumentRepository).update(any());
        }

        @Test
        void when_document_not_found_expect_throw_not_found_exception() {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseStudentDocumentService.updateDescription(GRADE_ID, DOCUMENT_ID, "new");

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentNotFoundException.class);
        }
    }

    @Nested
    class DeleteDocument {

        @Test
        void when_document_exists_expect_delete_document_and_file() throws IOException {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            exerciseStudentDocumentService.deleteDocument(GRADE_ID, DOCUMENT_ID);

            verify(exerciseStudentDocumentRepository).deleteById(DOCUMENT_ID);
            assertThat(diskFile).doesNotExist();
        }

        @Test
        void when_document_not_found_expect_throw_not_found_exception() {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseStudentDocumentService.deleteDocument(GRADE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentNotFoundException.class);
        }
    }

    @Nested
    class DeleteDocumentsByGradeId {

        @Test
        void when_documents_exist_expect_delete_all_documents_and_files() throws IOException {
            final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

            when(exerciseStudentDocumentRepository.findByGradeId(GRADE_ID)).thenReturn(List.of(document));

            exerciseStudentDocumentService.deleteDocumentsByGradeId(GRADE_ID);

            verify(exerciseStudentDocumentRepository).deleteByGradeId(GRADE_ID);
            assertThat(diskFile).doesNotExist();
        }

        @Test
        void when_delete_file_from_disk_fails_expect_throw_upload_exception() throws IOException {
            final Path nonEmptyDir = Files.createDirectory(tempDir.resolve("undeletable_dir"));
            Files.write(nonEmptyDir.resolve("child.txt"), "block".getBytes());
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document(nonEmptyDir.getFileName().toString()).build();

            when(exerciseStudentDocumentRepository.findByGradeId(GRADE_ID)).thenReturn(List.of(document));

            final ThrowingCallable call = () -> exerciseStudentDocumentService.deleteDocumentsByGradeId(GRADE_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentUploadException.class);
        }
    }

    @Nested
    class DeleteDocumentsByExerciseId {

        @Test
        void when_documents_exist_expect_delete_all_documents_and_files() throws IOException {
            final Integer exerciseId = 30;
            final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

            when(exerciseStudentDocumentRepository.findByExerciseId(exerciseId)).thenReturn(List.of(document));

            exerciseStudentDocumentService.deleteDocumentsByExerciseId(exerciseId);

            verify(exerciseStudentDocumentRepository).deleteByExerciseId(exerciseId);
            assertThat(diskFile).doesNotExist();
        }
    }

    @Nested
    class DeleteDocumentsByExerciseIds {

        @Test
        void when_documents_exist_expect_delete_all_documents_and_files() throws IOException {
            final List<Integer> exerciseIds = List.of(30, 31);
            final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

            when(exerciseStudentDocumentRepository.findByExerciseIds(exerciseIds)).thenReturn(List.of(document));

            exerciseStudentDocumentService.deleteDocumentsByExerciseIds(exerciseIds);

            verify(exerciseStudentDocumentRepository).deleteByExerciseIds(exerciseIds);
            assertThat(diskFile).doesNotExist();
        }

        @Test
        void when_list_is_null_expect_do_nothing() {
            exerciseStudentDocumentService.deleteDocumentsByExerciseIds(null);

            verifyNoInteractions(exerciseStudentDocumentRepository);
        }

        @Test
        void when_list_is_empty_expect_do_nothing() {
            exerciseStudentDocumentService.deleteDocumentsByExerciseIds(List.of());

            verifyNoInteractions(exerciseStudentDocumentRepository);
        }
    }

    @Nested
    class DeleteDocumentsByStudentIdAndClassId {

        @Test
        void when_documents_exist_expect_delete_all_documents_and_files() throws IOException {
            final Integer studentId = 5;
            final Integer classId = 2;
            final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

            when(exerciseStudentDocumentRepository.findByStudentIdAndClassId(studentId, classId)).thenReturn(List.of(document));

            exerciseStudentDocumentService.deleteDocumentsByStudentIdAndClassId(studentId, classId);

            verify(exerciseStudentDocumentRepository).deleteByStudentIdAndClassId(studentId, classId);
            assertThat(diskFile).doesNotExist();
        }
    }

    @Nested
    class GetDocumentFilename {

        @Test
        void when_document_exists_expect_return_filename() {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document("10_doc_abc12345.pdf").build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final String result = exerciseStudentDocumentService.getDocumentFilename(GRADE_ID, DOCUMENT_ID);

            assertThat(result).isEqualTo("10_doc_abc12345.pdf");
        }

        @Test
        void when_grade_not_found_expect_throw_grade_not_found_exception() {
            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseStudentDocumentService.getDocumentFilename(GRADE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentGradeNotFoundException.class);
        }

        @Test
        void when_document_not_found_expect_throw_document_not_found_exception() {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseStudentDocumentService.getDocumentFilename(GRADE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentNotFoundException.class);
        }

        @Test
        void when_document_belongs_to_different_grade_expect_throw_document_not_found_exception() {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(999).document("file.pdf").build();

            when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
            when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final ThrowingCallable call = () -> exerciseStudentDocumentService.getDocumentFilename(GRADE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseStudentDocumentNotFoundException.class);
        }
    }

    @Nested
    class DeleteDocumentsByGradeIds {

        @Test
        void when_documents_exist_expect_delete_all_documents_and_files() throws IOException {
            final List<Integer> gradeIds = List.of(GRADE_ID, 11);
            final Path diskFile = Files.write(tempDir.resolve("10_doc_gradeids.pdf"), "content".getBytes());
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

            when(exerciseStudentDocumentRepository.findByGradeIds(gradeIds)).thenReturn(List.of(document));

            exerciseStudentDocumentService.deleteDocumentsByGradeIds(gradeIds);

            verify(exerciseStudentDocumentRepository).deleteByGradeIds(gradeIds);
            assertThat(diskFile).doesNotExist();
        }

        @Test
        void when_list_is_empty_expect_do_nothing() {
            exerciseStudentDocumentService.deleteDocumentsByGradeIds(List.of());

            verifyNoInteractions(exerciseStudentDocumentRepository);
        }

        @Test
        void when_list_is_null_expect_do_nothing() {
            exerciseStudentDocumentService.deleteDocumentsByGradeIds(null);

            verifyNoInteractions(exerciseStudentDocumentRepository);
        }
    }

    @Nested
    class DeleteDocumentsByStudentId {

        @Test
        void when_documents_exist_expect_delete_all_documents_and_files() throws IOException {
            final Integer studentId = 5;
            final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                    .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

            when(exerciseStudentDocumentRepository.findByStudentId(studentId)).thenReturn(List.of(document));

            exerciseStudentDocumentService.deleteDocumentsByStudentId(studentId);

            verify(exerciseStudentDocumentRepository).deleteByStudentId(studentId);
            assertThat(diskFile).doesNotExist();
        }
    }
}
