package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
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

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private ExerciseDocumentServiceImpl exerciseDocumentService;

    @TempDir
    Path tempDir;

    private static final Integer TEACHER_ID = 1;
    private static final Integer EXERCISE_ID = 100;
    private static final Integer DOCUMENT_ID = 200;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exerciseDocumentService, "documentsDirectory", tempDir.toString());

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");
    }

    @Test
    void uploadDocument_shouldSaveDocumentAndFile_whenValid() throws IOException {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        MultipartFile file = mock(MultipartFile.class);

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("file content".getBytes());
        when(file.getOriginalFilename()).thenReturn("document.pdf");
        when(exerciseDocumentRepository.save(any(ExerciseDocument.class))).thenAnswer(invocation -> {
            ExerciseDocument doc = invocation.getArgument(0);
            return ExerciseDocument.builder().id(DOCUMENT_ID).exerciseId(doc.getExerciseId())
                    .document(doc.getDocument()).description(doc.getDescription()).build();
        });

        ExerciseDocument result = exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "Test description");

        assertNotNull(result);
        assertEquals(DOCUMENT_ID, result.getId());
        assertEquals(EXERCISE_ID, result.getExerciseId());
        assertTrue(result.getDocument().startsWith(EXERCISE_ID + "_document_"));
        assertTrue(result.getDocument().endsWith(".pdf"));
        verify(exerciseDocumentRepository).save(any(ExerciseDocument.class));
    }

    @Test
    void uploadDocument_shouldThrowExerciseNotFoundException_whenExerciseNotFound() {
        MultipartFile file = mock(MultipartFile.class);

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseNotFoundException.class,
                () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenFileIsEmpty() throws IOException {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        MultipartFile file = mock(MultipartFile.class);

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(file.isEmpty()).thenReturn(true);
        when(file.getBytes()).thenReturn(new byte[0]);

        assertThrows(ExerciseDocumentUploadException.class,
                () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenFileIsNull() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));

        assertThrows(ExerciseDocumentUploadException.class,
                () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, null, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenFileSizeExceeds2MB() throws IOException {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        MultipartFile file = mock(MultipartFile.class);
        byte[] largeContent = new byte[3 * 1024 * 1024];

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn(largeContent);
        when(file.getOriginalFilename()).thenReturn("large.pdf");

        assertThrows(ExerciseDocumentUploadException.class,
                () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenExtensionNotAllowed() throws IOException {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        MultipartFile file = mock(MultipartFile.class);

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("content".getBytes());
        when(file.getOriginalFilename()).thenReturn("virus.exe");

        assertThrows(ExerciseDocumentUploadException.class,
                () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenGetBytesFails() throws IOException {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        MultipartFile file = mock(MultipartFile.class);

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenThrow(new IOException("Read error"));

        assertThrows(ExerciseDocumentUploadException.class,
                () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenWriteToFileFails() throws IOException {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        MultipartFile file = mock(MultipartFile.class);

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("content".getBytes());
        when(file.getOriginalFilename()).thenReturn("document.pdf");

        ReflectionTestUtils.setField(exerciseDocumentService, "documentsDirectory", "/nonexistent/path/that/does/not/exist");

        assertThrows(ExerciseDocumentUploadException.class,
                () -> exerciseDocumentService.uploadDocument(EXERCISE_ID, file, "desc"));

        ReflectionTestUtils.setField(exerciseDocumentService, "documentsDirectory", tempDir.toString());
    }

    @Test
    void downloadDocument_shouldReturnFileBytes_whenDocumentExists() throws IOException {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        String filename = "100_test_abc12345.pdf";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "pdf content".getBytes());

        ExerciseDocument document = ExerciseDocument.builder()
                .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document(filename).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        byte[] result = exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID);

        assertNotNull(result);
        assertEquals("pdf content", new String(result));
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenExerciseNotFound() {
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseNotFoundException.class,
                () -> exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID));
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenDocumentNotFound() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseDocumentNotFoundException.class,
                () -> exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID));
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenDocumentBelongsToDifferentExercise() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        ExerciseDocument document = ExerciseDocument.builder()
                .id(DOCUMENT_ID).exerciseId(999).document("file.pdf").build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        assertThrows(ExerciseDocumentNotFoundException.class,
                () -> exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID));
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenFileNotOnDisk() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        ExerciseDocument document = ExerciseDocument.builder()
                .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("nonexistent.pdf").build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        assertThrows(ExerciseDocumentNotFoundException.class,
                () -> exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID));
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenReadAllBytesFails() throws IOException {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        String dirName = "fake_file_dir";
        Path dirPath = tempDir.resolve(dirName);
        Files.createDirectory(dirPath);

        ExerciseDocument document = ExerciseDocument.builder()
                .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document(dirName).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        assertThrows(ExerciseDocumentNotFoundException.class,
                () -> exerciseDocumentService.downloadDocument(EXERCISE_ID, DOCUMENT_ID));
    }

    @Test
    void getDocumentFilename_shouldReturnFilename_whenDocumentExists() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        ExerciseDocument document = ExerciseDocument.builder()
                .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("100_test_abc12345.pdf").build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        String result = exerciseDocumentService.getDocumentFilename(EXERCISE_ID, DOCUMENT_ID);

        assertEquals("100_test_abc12345.pdf", result);
    }

    @Test
    void getDocumentFilename_shouldThrowNotFoundException_whenExerciseNotFound() {
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseNotFoundException.class,
                () -> exerciseDocumentService.getDocumentFilename(EXERCISE_ID, DOCUMENT_ID));
    }

    @Test
    void getDocumentFilename_shouldThrowNotFoundException_whenDocumentNotFound() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseDocumentNotFoundException.class,
                () -> exerciseDocumentService.getDocumentFilename(EXERCISE_ID, DOCUMENT_ID));
    }

    @Test
    void updateDescription_shouldUpdateAndReturn_whenValid() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        ExerciseDocument document = ExerciseDocument.builder()
                .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("file.pdf").description("Old").build();
        ExerciseDocument updatedDocument = ExerciseDocument.builder()
                .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document("file.pdf").description("New description").build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));
        when(exerciseDocumentRepository.update(any(ExerciseDocument.class))).thenReturn(updatedDocument);

        ExerciseDocument result = exerciseDocumentService.updateDescription(EXERCISE_ID, DOCUMENT_ID, "New description");

        assertNotNull(result);
        assertEquals("New description", result.getDescription());
        verify(exerciseDocumentRepository).update(any(ExerciseDocument.class));
    }

    @Test
    void updateDescription_shouldThrowNotFoundException_whenExerciseNotFound() {
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseNotFoundException.class,
                () -> exerciseDocumentService.updateDescription(EXERCISE_ID, DOCUMENT_ID, "desc"));
    }

    @Test
    void updateDescription_shouldThrowNotFoundException_whenDocumentNotFound() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseDocumentNotFoundException.class,
                () -> exerciseDocumentService.updateDescription(EXERCISE_ID, DOCUMENT_ID, "desc"));
    }

    @Test
    void deleteDocument_shouldDeleteRecordAndFile_whenValid() throws IOException {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        String filename = "100_test_abc12345.pdf";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "content".getBytes());

        ExerciseDocument document = ExerciseDocument.builder()
                .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document(filename).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        exerciseDocumentService.deleteDocument(EXERCISE_ID, DOCUMENT_ID);

        verify(exerciseDocumentRepository).deleteById(DOCUMENT_ID);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void deleteDocument_shouldThrowNotFoundException_whenExerciseNotFound() {
        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseNotFoundException.class,
                () -> exerciseDocumentService.deleteDocument(EXERCISE_ID, DOCUMENT_ID));
    }

    @Test
    void deleteDocument_shouldThrowNotFoundException_whenDocumentNotFound() {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseDocumentNotFoundException.class,
                () -> exerciseDocumentService.deleteDocument(EXERCISE_ID, DOCUMENT_ID));
    }

    @Test
    void deleteDocument_shouldThrowException_whenDeleteFileFromDiskFails() throws IOException {
        Exercise exercise = Exercise.builder().id(EXERCISE_ID).build();
        String dirName = "undeletable_dir";
        Path dirPath = tempDir.resolve(dirName);
        Files.createDirectory(dirPath);
        Files.write(dirPath.resolve("child.txt"), "block delete".getBytes());

        ExerciseDocument document = ExerciseDocument.builder()
                .id(DOCUMENT_ID).exerciseId(EXERCISE_ID).document(dirName).build();

        when(exerciseRepository.findByIdAndTeacherId(EXERCISE_ID, TEACHER_ID)).thenReturn(Optional.of(exercise));
        when(exerciseDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        assertThrows(ExerciseDocumentUploadException.class,
                () -> exerciseDocumentService.deleteDocument(EXERCISE_ID, DOCUMENT_ID));
    }

    @Test
    void deleteDocumentsByExerciseId_shouldDeleteAllDocumentsAndFiles() throws IOException {
        String filename1 = "100_doc1_abc12345.pdf";
        String filename2 = "100_doc2_def67890.png";
        Files.write(tempDir.resolve(filename1), "content1".getBytes());
        Files.write(tempDir.resolve(filename2), "content2".getBytes());

        List<ExerciseDocument> documents = List.of(
                ExerciseDocument.builder().id(1).exerciseId(EXERCISE_ID).document(filename1).build(),
                ExerciseDocument.builder().id(2).exerciseId(EXERCISE_ID).document(filename2).build()
        );

        when(exerciseDocumentRepository.findByExerciseId(EXERCISE_ID)).thenReturn(documents);

        exerciseDocumentService.deleteDocumentsByExerciseId(EXERCISE_ID);

        verify(exerciseDocumentRepository).deleteByExerciseId(EXERCISE_ID);
        assertFalse(Files.exists(tempDir.resolve(filename1)));
        assertFalse(Files.exists(tempDir.resolve(filename2)));
    }

    @Test
    void deleteDocumentsByExerciseIds_shouldDeleteAllDocumentsAndFiles() throws IOException {
        String filename1 = "100_doc1_abc12345.pdf";
        String filename2 = "101_doc2_def67890.png";
        Files.write(tempDir.resolve(filename1), "content1".getBytes());
        Files.write(tempDir.resolve(filename2), "content2".getBytes());

        List<Integer> exerciseIds = List.of(100, 101);
        List<ExerciseDocument> documents = List.of(
                ExerciseDocument.builder().id(1).exerciseId(100).document(filename1).build(),
                ExerciseDocument.builder().id(2).exerciseId(101).document(filename2).build()
        );

        when(exerciseDocumentRepository.findByExerciseIds(exerciseIds)).thenReturn(documents);

        exerciseDocumentService.deleteDocumentsByExerciseIds(exerciseIds);

        verify(exerciseDocumentRepository).deleteByExerciseIds(exerciseIds);
        assertFalse(Files.exists(tempDir.resolve(filename1)));
        assertFalse(Files.exists(tempDir.resolve(filename2)));
    }

    @Test
    void deleteDocumentsByExerciseIds_shouldDoNothing_whenListIsNull() {
        exerciseDocumentService.deleteDocumentsByExerciseIds(null);

        verify(exerciseDocumentRepository, never()).deleteByExerciseIds(any());
    }

    @Test
    void deleteDocumentsByExerciseIds_shouldDoNothing_whenListIsEmpty() {
        exerciseDocumentService.deleteDocumentsByExerciseIds(List.of());

        verify(exerciseDocumentRepository, never()).deleteByExerciseIds(any());
    }
}


