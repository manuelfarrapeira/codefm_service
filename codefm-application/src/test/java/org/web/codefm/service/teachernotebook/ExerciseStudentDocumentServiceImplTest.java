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

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private ExerciseStudentDocumentServiceImpl exerciseStudentDocumentService;

    @TempDir
    Path tempDir;

    private static final Integer TEACHER_ID = 1;
    private static final Integer GRADE_ID = 10;
    private static final Integer DOCUMENT_ID = 200;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exerciseStudentDocumentService, "documentsDirectory", tempDir.toString());
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");
    }

    @Test
    void uploadDocument_shouldSaveDocumentAndReturnIt_whenGradeExistsAndFileIsValid() throws IOException {
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

        assertNotNull(result);
        assertEquals(DOCUMENT_ID, result.getId());
        assertEquals(GRADE_ID, result.getGradeId());
        assertTrue(result.getDocument().startsWith(GRADE_ID + "_document_"));
        assertTrue(result.getDocument().endsWith(".pdf"));
        verify(exerciseStudentDocumentRepository).save(any(ExerciseStudentDocument.class));
    }

    @Test
    void uploadDocument_shouldThrowNotFoundException_whenGradeNotFound() {
        final MultipartFile file = mock(MultipartFile.class);

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseStudentGradeNotFoundException.class,
                () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenFileIsEmpty() throws IOException {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final MultipartFile file = mock(MultipartFile.class);

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(file.isEmpty()).thenReturn(true);
        when(file.getBytes()).thenReturn(new byte[0]);

        assertThrows(ExerciseStudentDocumentUploadException.class,
                () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenFileIsNull() {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));

        assertThrows(ExerciseStudentDocumentUploadException.class,
                () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, null, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenFileSizeExceeded() throws IOException {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final MultipartFile file = mock(MultipartFile.class);

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn(new byte[3 * 1024 * 1024]);
        when(file.getOriginalFilename()).thenReturn("large.pdf");

        assertThrows(ExerciseStudentDocumentUploadException.class,
                () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenExtensionIsInvalid() throws IOException {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final MultipartFile file = mock(MultipartFile.class);

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("content".getBytes());
        when(file.getOriginalFilename()).thenReturn("virus.exe");

        assertThrows(ExerciseStudentDocumentUploadException.class,
                () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenGetBytesThrowsIOException() throws IOException {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final MultipartFile file = mock(MultipartFile.class);

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenThrow(new IOException("Read error"));

        assertThrows(ExerciseStudentDocumentUploadException.class,
                () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc"));
    }

    @Test
    void uploadDocument_shouldThrowUploadException_whenWritingFileToDiskFails() throws IOException {
        ReflectionTestUtils.setField(exerciseStudentDocumentService, "documentsDirectory", "/non_existent_path_xyz");
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final MultipartFile file = mock(MultipartFile.class);

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("content".getBytes());
        when(file.getOriginalFilename()).thenReturn("document.pdf");

        assertThrows(ExerciseStudentDocumentUploadException.class,
                () -> exerciseStudentDocumentService.uploadDocument(GRADE_ID, file, "desc"));
    }

    @Test
    void downloadDocument_shouldReturnFileBytes_whenDocumentExists() throws IOException {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final byte[] expectedBytes = "file content".getBytes();
        final Path file = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), expectedBytes);
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document(file.getFileName().toString()).build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        final byte[] result = exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID);

        assertArrayEquals(expectedBytes, result);
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenGradeNotFound() {
        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseStudentGradeNotFoundException.class,
                () -> exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID));
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenDocumentNotFound() {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseStudentDocumentNotFoundException.class,
                () -> exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID));
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenFileDoesNotExistOnDisk() {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document("nonexistent_file.pdf").build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        assertThrows(ExerciseStudentDocumentNotFoundException.class,
                () -> exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID));
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenReadingFileThrowsIOException() throws IOException {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final Path subDir = Files.createDirectory(tempDir.resolve("fakefile.pdf"));
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document(subDir.getFileName().toString()).build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        assertThrows(ExerciseStudentDocumentNotFoundException.class,
                () -> exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID));
    }

    @Test
    void downloadDocument_shouldThrowNotFoundException_whenDocumentBelongsToDifferentGrade() {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(999).document("file.pdf").build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        assertThrows(ExerciseStudentDocumentNotFoundException.class,
                () -> exerciseStudentDocumentService.downloadDocument(GRADE_ID, DOCUMENT_ID));
    }

    @Test
    void updateDescription_shouldUpdateAndReturnDocument_whenDocumentExists() {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document("file.pdf").description("old").build();
        final ExerciseStudentDocument updated = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document("file.pdf").description("new desc").build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));
        when(exerciseStudentDocumentRepository.update(any())).thenReturn(updated);

        final ExerciseStudentDocument result = exerciseStudentDocumentService.updateDescription(GRADE_ID, DOCUMENT_ID, "new desc");

        assertEquals("new desc", result.getDescription());
        verify(exerciseStudentDocumentRepository).update(any());
    }

    @Test
    void updateDescription_shouldThrowNotFoundException_whenDocumentNotFound() {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseStudentDocumentNotFoundException.class,
                () -> exerciseStudentDocumentService.updateDescription(GRADE_ID, DOCUMENT_ID, "new"));
    }

    @Test
    void deleteDocument_shouldDeleteDocumentAndFile_whenDocumentExists() throws IOException {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        exerciseStudentDocumentService.deleteDocument(GRADE_ID, DOCUMENT_ID);

        verify(exerciseStudentDocumentRepository).deleteById(DOCUMENT_ID);
        assertFalse(Files.exists(diskFile));
    }

    @Test
    void deleteDocument_shouldThrowNotFoundException_whenDocumentNotFound() {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseStudentDocumentNotFoundException.class,
                () -> exerciseStudentDocumentService.deleteDocument(GRADE_ID, DOCUMENT_ID));
    }

    @Test
    void deleteDocumentsByGradeId_shouldDeleteAllDocumentsAndFiles() throws IOException {
        final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

        when(exerciseStudentDocumentRepository.findByGradeId(GRADE_ID)).thenReturn(List.of(document));

        exerciseStudentDocumentService.deleteDocumentsByGradeId(GRADE_ID);

        verify(exerciseStudentDocumentRepository).deleteByGradeId(GRADE_ID);
        assertFalse(Files.exists(diskFile));
    }

    @Test
    void deleteDocumentsByExerciseId_shouldDeleteAllDocumentsAndFiles() throws IOException {
        final Integer exerciseId = 30;
        final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

        when(exerciseStudentDocumentRepository.findByExerciseId(exerciseId)).thenReturn(List.of(document));

        exerciseStudentDocumentService.deleteDocumentsByExerciseId(exerciseId);

        verify(exerciseStudentDocumentRepository).deleteByExerciseId(exerciseId);
        assertFalse(Files.exists(diskFile));
    }

    @Test
    void deleteDocumentsByExerciseIds_shouldDeleteAllDocumentsAndFiles() throws IOException {
        final List<Integer> exerciseIds = List.of(30, 31);
        final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

        when(exerciseStudentDocumentRepository.findByExerciseIds(exerciseIds)).thenReturn(List.of(document));

        exerciseStudentDocumentService.deleteDocumentsByExerciseIds(exerciseIds);

        verify(exerciseStudentDocumentRepository).deleteByExerciseIds(exerciseIds);
        assertFalse(Files.exists(diskFile));
    }

    @Test
    void deleteDocumentsByStudentIdAndClassId_shouldDeleteAllDocumentsAndFiles() throws IOException {
        final Integer studentId = 5;
        final Integer classId = 2;
        final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

        when(exerciseStudentDocumentRepository.findByStudentIdAndClassId(studentId, classId)).thenReturn(List.of(document));

        exerciseStudentDocumentService.deleteDocumentsByStudentIdAndClassId(studentId, classId);

        verify(exerciseStudentDocumentRepository).deleteByStudentIdAndClassId(studentId, classId);
        assertFalse(Files.exists(diskFile));
    }

    @Test
    void getDocumentFilename_shouldReturnFilename_whenDocumentExists() {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document("10_doc_abc12345.pdf").build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        final String result = exerciseStudentDocumentService.getDocumentFilename(GRADE_ID, DOCUMENT_ID);

        assertEquals("10_doc_abc12345.pdf", result);
    }

    @Test
    void getDocumentFilename_shouldThrowGradeNotFoundException_whenGradeNotFound() {
        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseStudentGradeNotFoundException.class,
                () -> exerciseStudentDocumentService.getDocumentFilename(GRADE_ID, DOCUMENT_ID));
    }

    @Test
    void getDocumentFilename_shouldThrowDocumentNotFoundException_whenDocumentNotFound() {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ExerciseStudentDocumentNotFoundException.class,
                () -> exerciseStudentDocumentService.getDocumentFilename(GRADE_ID, DOCUMENT_ID));
    }

    @Test
    void getDocumentFilename_shouldThrowDocumentNotFoundException_whenDocumentBelongsToDifferentGrade() {
        final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(GRADE_ID).build();
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(999).document("file.pdf").build();

        when(exerciseStudentGradeRepository.findByIdAndTeacherId(GRADE_ID, TEACHER_ID)).thenReturn(Optional.of(grade));
        when(exerciseStudentDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        assertThrows(ExerciseStudentDocumentNotFoundException.class,
                () -> exerciseStudentDocumentService.getDocumentFilename(GRADE_ID, DOCUMENT_ID));
    }

    @Test
    void deleteDocumentsByGradeIds_shouldDeleteAllDocumentsAndFiles() throws IOException {
        final List<Integer> gradeIds = List.of(GRADE_ID, 11);
        final Path diskFile = Files.write(tempDir.resolve("10_doc_gradeids.pdf"), "content".getBytes());
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

        when(exerciseStudentDocumentRepository.findByGradeIds(gradeIds)).thenReturn(List.of(document));

        exerciseStudentDocumentService.deleteDocumentsByGradeIds(gradeIds);

        verify(exerciseStudentDocumentRepository).deleteByGradeIds(gradeIds);
        assertFalse(Files.exists(diskFile));
    }

    @Test
    void deleteDocumentsByGradeIds_shouldDoNothing_whenListIsEmpty() {
        exerciseStudentDocumentService.deleteDocumentsByGradeIds(List.of());

        verifyNoInteractions(exerciseStudentDocumentRepository);
    }

    @Test
    void deleteDocumentsByGradeIds_shouldDoNothing_whenListIsNull() {
        exerciseStudentDocumentService.deleteDocumentsByGradeIds(null);

        verifyNoInteractions(exerciseStudentDocumentRepository);
    }

    @Test
    void deleteDocumentsByExerciseIds_shouldDoNothing_whenListIsNull() {
        exerciseStudentDocumentService.deleteDocumentsByExerciseIds(null);

        verifyNoInteractions(exerciseStudentDocumentRepository);
    }

    @Test
    void deleteDocumentsByExerciseIds_shouldDoNothing_whenListIsEmpty() {
        exerciseStudentDocumentService.deleteDocumentsByExerciseIds(List.of());

        verifyNoInteractions(exerciseStudentDocumentRepository);
    }

    @Test
    void deleteDocumentsByStudentId_shouldDeleteAllDocumentsAndFiles() throws IOException {
        final Integer studentId = 5;
        final Path diskFile = Files.write(tempDir.resolve("10_doc_abc12345.pdf"), "content".getBytes());
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document(diskFile.getFileName().toString()).build();

        when(exerciseStudentDocumentRepository.findByStudentId(studentId)).thenReturn(List.of(document));

        exerciseStudentDocumentService.deleteDocumentsByStudentId(studentId);

        verify(exerciseStudentDocumentRepository).deleteByStudentId(studentId);
        assertFalse(Files.exists(diskFile));
    }

    @Test
    void deleteDocumentsByGradeId_shouldThrowUploadException_whenDeleteFileFromDiskFails() throws IOException {
        final Path nonEmptyDir = Files.createDirectory(tempDir.resolve("undeletable_dir"));
        Files.write(nonEmptyDir.resolve("child.txt"), "block".getBytes());
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder()
                .id(DOCUMENT_ID).gradeId(GRADE_ID).document(nonEmptyDir.getFileName().toString()).build();

        when(exerciseStudentDocumentRepository.findByGradeId(GRADE_ID)).thenReturn(List.of(document));

        assertThrows(ExerciseStudentDocumentUploadException.class,
                () -> exerciseStudentDocumentService.deleteDocumentsByGradeId(GRADE_ID));
    }

    @Test
    void extractBaseName_shouldReturnEmpty_whenFilenameIsNull() {
        final String result = ReflectionTestUtils.invokeMethod(exerciseStudentDocumentService, "extractBaseName", (String) null);

        assertEquals("", result);
    }

    @Test
    void extractBaseName_shouldSanitizeAndReturn_whenFilenameHasNoDot() {
        final String result = ReflectionTestUtils.invokeMethod(exerciseStudentDocumentService, "extractBaseName", "my file (1)");

        assertEquals("my_file__1_", result);
    }

    @Test
    void extractBaseName_shouldReturnSanitizedName_whenFilenameHasNoDotAndIsAlphanumeric() {
        final String result = ReflectionTestUtils.invokeMethod(exerciseStudentDocumentService, "extractBaseName", "document");

        assertEquals("document", result);
    }

    @Test
    void extractExtension_shouldReturnEmpty_whenFilenameIsNull() {
        final String result = ReflectionTestUtils.invokeMethod(exerciseStudentDocumentService, "extractExtension", (String) null);

        assertEquals("", result);
    }

    @Test
    void extractExtension_shouldReturnEmpty_whenFilenameHasNoDot() {
        final String result = ReflectionTestUtils.invokeMethod(exerciseStudentDocumentService, "extractExtension", "document");

        assertEquals("", result);
    }
}

