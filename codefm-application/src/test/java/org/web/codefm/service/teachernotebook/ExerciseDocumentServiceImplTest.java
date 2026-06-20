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
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.domain.exception.teachernotebook.ExerciseDocumentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseDocumentUploadException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseNotFoundException;
import org.web.codefm.domain.repository.teachernotebook.ExerciseDocumentRepository;
import org.web.codefm.domain.repository.teachernotebook.ExerciseRepository;
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
class ExerciseDocumentServiceImplTest {

    @Mock
    private ExerciseDocumentRepository exerciseDocumentRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    private ExerciseDocumentServiceImpl exerciseDocumentService;

    @TempDir
    Path tempDir;

    private static final Integer TEACHER_ID = 1;
    private static final Integer EXERCISE_ID = 100;
    private static final Integer DOCUMENT_ID = 200;

    @BeforeEach
    void beforeEach() {
        exerciseDocumentService = new ExerciseDocumentServiceImpl(
                exerciseDocumentRepository, exerciseRepository, messageSource, sessionUser);
        ReflectionTestUtils.setField(exerciseDocumentService, "documentsDirectory", tempDir.toString());

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");
    }

    @Nested
    class UploadDocument {

        @Test
        void when_valid_expect_save_document_and_file() throws IOException {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn("file content".getBytes());
            when(file.getOriginalFilename()).thenReturn("document.pdf");
            when(exerciseDocumentRepository.save(any(ExerciseDocument.class))).thenAnswer(invocation -> {
                ExerciseDocument doc = invocation.getArgument(0);
                return ExerciseDocument.builder().id(DOCUMENT_ID).exerciseId(doc.getExerciseId())
                        .document(doc.getDocument()).description(doc.getDescription()).build();
            });

            final ExerciseDocument result = exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "Test description");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(DOCUMENT_ID);
            assertThat(result.getExerciseId()).isEqualTo(EXERCISE_ID);
            assertThat(result.getDocument()).startsWith(EXERCISE_ID + "_document_");
            assertThat(result.getDocument()).endsWith(".pdf");
            verify(exerciseDocumentRepository).save(any(ExerciseDocument.class));
        }

        @Test
        void when_exercise_not_found_expect_throw_exercise_not_found_exception() {
            final MultipartFile file = mock(MultipartFile.class);
            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseNotFoundException.class);
        }

        @Test
        void when_file_is_empty_expect_throw_upload_exception() throws IOException {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(file.isEmpty()).thenReturn(true);
            when(file.getBytes()).thenReturn(new byte[0]);

            final ThrowingCallable call = () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentUploadException.class);
        }

        @Test
        void when_file_is_null_expect_throw_upload_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));

            final ThrowingCallable call = () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, null, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentUploadException.class);
        }

        @Test
        void when_file_size_exceeds_2mb_expect_throw_upload_exception() throws IOException {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);
            final byte[] largeContent = new byte[3 * 1024 * 1024];

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn(largeContent);
            when(file.getOriginalFilename()).thenReturn("large.pdf");

            final ThrowingCallable call = () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentUploadException.class);
        }

        @Test
        void when_extension_not_allowed_expect_throw_upload_exception() throws IOException {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn("content".getBytes());
            when(file.getOriginalFilename()).thenReturn("virus.exe");

            final ThrowingCallable call = () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentUploadException.class);
        }

        @Test
        void when_get_bytes_fails_expect_throw_upload_exception() throws IOException {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenThrow(new IOException("Read error"));

            final ThrowingCallable call = () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentUploadException.class);
        }

        @Test
        void when_write_to_file_fails_expect_throw_upload_exception() throws IOException {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final MultipartFile file = mock(MultipartFile.class);

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenReturn("content".getBytes());
            when(file.getOriginalFilename()).thenReturn("document.pdf");

            ReflectionTestUtils.setField(exerciseDocumentService, "documentsDirectory", "/nonexistent/path/that/does/not/exist");

            final ThrowingCallable call = () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentUploadException.class);

            ReflectionTestUtils.setField(exerciseDocumentService, "documentsDirectory", tempDir.toString());
        }
    }

    @Nested
    class DownloadDocument {

        @Test
        void when_document_exists_expect_return_file_bytes() throws IOException {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final String filename = "100_test_abc12345.pdf";
            final Path filePath = tempDir.resolve(filename);
            Files.write(filePath, "pdf content".getBytes());

            final ExerciseDocument document = ExerciseDocument.builder()
                    .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document(filename).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final byte[] result = exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID);

            assertThat(result).isNotNull();
            assertThat(new String(result)).isEqualTo("pdf content");
        }

        @Test
        void when_exercise_not_found_expect_throw_exercise_not_found_exception() {
            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseNotFoundException.class);
        }

        @Test
        void when_document_not_found_expect_throw_not_found_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentNotFoundException.class);
        }

        @Test
        void when_document_belongs_to_different_exercise_expect_throw_not_found_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final ExerciseDocument document = ExerciseDocument.builder()
                    .id(DOCUMENT_ID).exerciseId(999).document("file.pdf").build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final ThrowingCallable call = () -> exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentNotFoundException.class);
        }

        @Test
        void when_file_not_on_disk_expect_throw_not_found_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final ExerciseDocument document = ExerciseDocument.builder()
                    .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("nonexistent.pdf").build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final ThrowingCallable call = () -> exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentNotFoundException.class);
        }

        @Test
        void when_read_all_bytes_fails_expect_throw_not_found_exception() throws IOException {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final String dirName = "fake_file_dir";
            final Path dirPath = tempDir.resolve(dirName);
            Files.createDirectory(dirPath);

            final ExerciseDocument document = ExerciseDocument.builder()
                    .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document(dirName).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final ThrowingCallable call = () -> exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentNotFoundException.class);
        }
    }

    @Nested
    class GetDocumentFilename {

        @Test
        void when_document_exists_expect_return_filename() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final ExerciseDocument document = ExerciseDocument.builder()
                    .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("100_test_abc12345.pdf").build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final String result = exerciseDocumentService.getDocumentFilename(EXERCISE_ID, DOCUMENT_ID);

            assertThat(result).isEqualTo("100_test_abc12345.pdf");
        }

        @Test
        void when_exercise_not_found_expect_throw_exercise_not_found_exception() {
            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseDocumentService.getDocumentFilename(EXERCISE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseNotFoundException.class);
        }

        @Test
        void when_document_not_found_expect_throw_not_found_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseDocumentService.getDocumentFilename(EXERCISE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentNotFoundException.class);
        }
    }

    @Nested
    class UpdateDescription {

        @Test
        void when_valid_expect_update_and_return() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final ExerciseDocument document = ExerciseDocument.builder()
                    .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("file.pdf").description("Old").build();
            final ExerciseDocument updatedDocument = ExerciseDocument.builder()
                    .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("file.pdf").description("New description").build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));
            when(exerciseDocumentRepository.update(any(ExerciseDocument.class))).thenReturn(updatedDocument);

            final ExerciseDocument result = exerciseDocumentService.updateDescription(EXERCISE_ID, DOCUMENT_ID, "New description");

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEqualTo("New description");
            verify(exerciseDocumentRepository).update(any(ExerciseDocument.class));
        }

        @Test
        void when_exercise_not_found_expect_throw_exercise_not_found_exception() {
            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseDocumentService.updateDescription(EXERCISE_ID, DOCUMENT_ID, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseNotFoundException.class);
        }

        @Test
        void when_document_not_found_expect_throw_not_found_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseDocumentService.updateDescription(EXERCISE_ID, DOCUMENT_ID, "desc");

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentNotFoundException.class);
        }
    }

    @Nested
    class DeleteDocument {

        @Test
        void when_valid_expect_delete_record_and_file() throws IOException {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final String filename = "100_test_abc12345.pdf";
            final Path filePath = tempDir.resolve(filename);
            Files.write(filePath, "content".getBytes());

            final ExerciseDocument document = ExerciseDocument.builder()
                    .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document(filename).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            exerciseDocumentService.deleteDocument(EXERCISE_ID, DOCUMENT_ID);

            verify(exerciseDocumentRepository).deleteById(DOCUMENT_ID);
            assertThat(filePath).doesNotExist();
        }

        @Test
        void when_exercise_not_found_expect_throw_exercise_not_found_exception() {
            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseDocumentService.deleteDocument(EXERCISE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseNotFoundException.class);
        }

        @Test
        void when_document_not_found_expect_throw_not_found_exception() {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> exerciseDocumentService.deleteDocument(EXERCISE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentNotFoundException.class);
        }

        @Test
        void when_delete_file_from_disk_fails_expect_throw_exception() throws IOException {
            final Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
            final String dirName = "undeletable_dir";
            final Path dirPath = tempDir.resolve(dirName);
            Files.createDirectory(dirPath);
            Files.write(dirPath.resolve("child.txt"), "block delete".getBytes());

            final ExerciseDocument document = ExerciseDocument.builder()
                    .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document(dirName).build();

            when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
            when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

            final ThrowingCallable call = () -> exerciseDocumentService.deleteDocument(EXERCISE_ID, DOCUMENT_ID);

            assertThatThrownBy(call).isInstanceOf(ExerciseDocumentUploadException.class);
        }
    }

    @Nested
    class DeleteDocumentsByExerciseId {

        @Test
        void when_documents_exist_expect_delete_all_documents_and_files() throws IOException {
            final String filename1 = "100_doc1_abc12345.pdf";
            final String filename2 = "100_doc2_def67890.png";
            Files.write(tempDir.resolve(filename1), "content1".getBytes());
            Files.write(tempDir.resolve(filename2), "content2".getBytes());

            final List<ExerciseDocument> documents = List.of(
                    ExerciseDocument.builder().id(1).exerciseId(EXERCISE_ID).document(filename1).build(),
                    ExerciseDocument.builder().id(2).exerciseId(EXERCISE_ID).document(filename2).build()
            );

            when(exerciseDocumentRepository.findByExerciseId(EXERCISE_ID)).thenReturn(documents);

            exerciseDocumentService.deleteDocumentsByExerciseId(EXERCISE_ID);

            verify(exerciseDocumentRepository).deleteByExerciseId(EXERCISE_ID);
            assertThat(tempDir.resolve(filename1)).doesNotExist();
            assertThat(tempDir.resolve(filename2)).doesNotExist();
        }
    }

    @Nested
    class DeleteDocumentsByExerciseIds {

        @Test
        void when_documents_exist_expect_delete_all_documents_and_files() throws IOException {
            final String filename1 = "100_doc1_abc12345.pdf";
            final String filename2 = "101_doc2_def67890.png";
            Files.write(tempDir.resolve(filename1), "content1".getBytes());
            Files.write(tempDir.resolve(filename2), "content2".getBytes());

            final List<Integer> exerciseIds = List.of(100, 101);
            final List<ExerciseDocument> documents = List.of(
                    ExerciseDocument.builder().id(1).exerciseId(100).document(filename1).build(),
                    ExerciseDocument.builder().id(2).exerciseId(101).document(filename2).build()
            );

            when(exerciseDocumentRepository.findByExerciseIds(exerciseIds)).thenReturn(documents);

            exerciseDocumentService.deleteDocumentsByExerciseIds(exerciseIds);

            verify(exerciseDocumentRepository).deleteByExerciseIds(exerciseIds);
            assertThat(tempDir.resolve(filename1)).doesNotExist();
            assertThat(tempDir.resolve(filename2)).doesNotExist();
        }

        @Test
        void when_list_is_null_expect_do_nothing() {
            exerciseDocumentService.deleteDocumentsByExerciseIds(null);

            verifyNoInteractions(exerciseDocumentRepository);
        }

        @Test
        void when_list_is_empty_expect_do_nothing() {
            exerciseDocumentService.deleteDocumentsByExerciseIds(List.of());

            verifyNoInteractions(exerciseDocumentRepository);
        }
    }
}
