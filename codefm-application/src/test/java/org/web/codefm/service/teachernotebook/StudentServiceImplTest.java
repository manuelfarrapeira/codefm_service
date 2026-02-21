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
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.exception.teachernotebook.*;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentServiceImplTest {

    @TempDir
    Path tempDir;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentClassRepository studentClassRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(studentService, "photosDirectory", tempDir.toString());

        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), "1");
        when(sessionUser.getParameters()).thenReturn(parameters);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("Student name is required.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_NAME_MIN_LENGTH), eq(null), any(Locale.class)))
                .thenReturn("Student name must be at least 3 characters.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_SURNAMES_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("Student surnames are required.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_SURNAMES_MIN_LENGTH), eq(null), any(Locale.class)))
                .thenReturn("Student surnames must be at least 3 characters.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_NOT_FOUND), eq(null), any(Locale.class)))
                .thenReturn("Student not found.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_PHOTO_SIZE_EXCEEDED), eq(null), any(Locale.class)))
                .thenReturn("Photo size cannot exceed 500KB.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_PHOTO_INVALID_EXTENSION), eq(null), any(Locale.class)))
                .thenReturn("Photo must be in JPG, JPEG or PNG format.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_PHOTO_UPLOAD_ERROR), eq(null), any(Locale.class)))
                .thenReturn("Error uploading student photo.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_PHOTO_EMPTY), eq(null), any(Locale.class)))
                .thenReturn("Photo file is required.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_PHOTO_NOT_FOUND), eq(null), any(Locale.class)))
                .thenReturn("Student photo not found.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_PHOTO_DELETE_ERROR), eq(null), any(Locale.class)))
                .thenReturn("Error deleting student photo.");
    }

    @Test
    void createStudent_shouldSaveStudent_whenDataIsValid() {
        Student studentToCreate = Student.builder()
                .name("Juan")
                .surnames("García López")
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .additionalInfo("Good student")
                .build();

        Student expectedStudent = Student.builder()
                .name("Juan")
                .surnames("García López")
                .teacherId(1)
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .additionalInfo("Good student")
                .build();

        when(studentRepository.save(any(Student.class))).thenReturn(expectedStudent);

        Student createdStudent = studentService.createStudent(studentToCreate);

        assertNotNull(createdStudent);
        assertEquals("Juan", createdStudent.getName());
        assertEquals("García López", createdStudent.getSurnames());
        assertEquals(1, createdStudent.getTeacherId());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void getStudentPhoto_shouldReturnPhotoBytes_whenPhotoExists() throws Exception {
        Integer studentId = 1;
        Integer teacherId = 1;
        String photoFileName = "1.jpg";
        byte[] expectedPhotoBytes = new byte[]{1, 2, 3, 4, 5};

        Student student = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .photo(photoFileName)
                .build();

        java.nio.file.Files.write(tempDir.resolve(photoFileName), expectedPhotoBytes);

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(student));

        byte[] result = studentService.getStudentPhoto(studentId);

        assertNotNull(result);
        assertArrayEquals(expectedPhotoBytes, result);
        verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
    }

    @Test
    void getStudentPhoto_shouldThrowNotFoundException_whenStudentNotFound() {
        Integer studentId = 99;
        Integer teacherId = 1;

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () -> studentService.getStudentPhoto(studentId));
        verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
    }

    @Test
    void getStudentPhoto_shouldThrowNotFoundException_whenPhotoIsEmpty() {
        Integer studentId = 1;
        Integer teacherId = 1;

        Student student = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .build();

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(student));

        assertThrows(StudentPhotoNotFoundException.class, () -> studentService.getStudentPhoto(studentId));
        verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
    }

    @Test
    void deleteStudentPhoto_shouldDeletePhotoAndUpdateStudent_whenPhotoExists() throws Exception {
        Integer studentId = 1;
        Integer teacherId = 1;
        String photoFileName = "1.jpg";
        byte[] photoBytes = new byte[]{1, 2, 3, 4, 5};

        Student student = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .photo(photoFileName)
                .build();

        java.nio.file.Files.write(tempDir.resolve(photoFileName), photoBytes);

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(student));
        when(studentRepository.update(any(Student.class))).thenReturn(student);

        studentService.deleteStudentPhoto(studentId);

        assertFalse(java.nio.file.Files.exists(tempDir.resolve(photoFileName)));
        assertNull(student.getPhoto());
        verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
        verify(studentRepository, times(1)).update(any(Student.class));
    }

    @Test
    void deleteStudentPhoto_shouldThrowNotFoundException_whenStudentNotFound() {
        Integer studentId = 99;
        Integer teacherId = 1;

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () -> studentService.deleteStudentPhoto(studentId));
        verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
    }

    @Test
    void deleteStudentPhoto_shouldThrowNotFoundException_whenPhotoIsEmpty() {
        Integer studentId = 1;
        Integer teacherId = 1;

        Student student = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .build();

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(student));

        assertThrows(StudentPhotoNotFoundException.class, () -> studentService.deleteStudentPhoto(studentId));
        verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
    }

    @Test
    void createStudent_shouldThrowValidationException_whenNameIsNull() {
        Student studentWithoutName = Student.builder()
                .name(null)
                .surnames("García López")
                .build();

        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            studentService.createStudent(studentWithoutName);
        });

        assertNotNull(exception);
        assertTrue(exception.getErrors().stream()
                .anyMatch(e -> e.getParam().equals("name")));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_shouldThrowValidationException_whenNameIsEmpty() {
        Student studentWithEmptyName = Student.builder()
                .name("")
                .surnames("García López")
                .build();

        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            studentService.createStudent(studentWithEmptyName);
        });

        assertNotNull(exception);
        assertTrue(exception.getErrors().stream()
                .anyMatch(e -> e.getParam().equals("name")));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_shouldThrowValidationException_whenNameIsTooShort() {
        Student studentWithShortName = Student.builder()
                .name("Ab")
                .surnames("García López")
                .build();

        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            studentService.createStudent(studentWithShortName);
        });

        assertNotNull(exception);
        assertTrue(exception.getErrors().stream()
                .anyMatch(e -> e.getParam().equals("name")));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_shouldThrowValidationException_whenSurnamesIsNull() {
        Student studentWithoutSurnames = Student.builder()
                .name("Juan")
                .surnames(null)
                .build();

        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            studentService.createStudent(studentWithoutSurnames);
        });

        assertNotNull(exception);
        assertTrue(exception.getErrors().stream()
                .anyMatch(e -> e.getParam().equals("surnames")));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_shouldThrowValidationException_whenSurnamesAreTooShort() {
        Student studentWithShortSurnames = Student.builder()
                .name("Juan")
                .surnames("Ab")
                .build();

        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            studentService.createStudent(studentWithShortSurnames);
        });

        assertNotNull(exception);
        assertTrue(exception.getErrors().stream()
                .anyMatch(e -> e.getParam().equals("surnames")));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void updateStudent_shouldUpdateStudent_whenDataIsValid() {
        Integer studentId = 1;
        Integer teacherId = 1;
        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García")
                .build();

        Student updatedData = Student.builder()
                .name("Juan Carlos")
                .surnames("García López")
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .build();

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(existingStudent));
        when(studentRepository.update(any(Student.class))).thenReturn(existingStudent);

        Student result = studentService.updateStudent(studentId, updatedData);

        assertNotNull(result);
        assertEquals("Juan Carlos", result.getName());
        assertEquals("García López", result.getSurnames());
        verify(studentRepository, times(1)).update(any(Student.class));
    }

    @Test
    void updateStudent_shouldThrowNotFoundException_whenStudentDoesNotExist() {
        Integer studentId = 999;
        Integer teacherId = 1;
        Student updatedData = Student.builder()
                .name("Juan")
                .surnames("García López")
                .build();

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.empty());

        StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> {
            studentService.updateStudent(studentId, updatedData);
        });

        assertNotNull(exception);
        verify(studentRepository, never()).update(any());
    }

    @Test
    void updateStudent_shouldThrowValidationException_whenNameIsInvalid() {
        Student invalidData = Student.builder()
                .name("A")
                .surnames("García López")
                .build();

        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            studentService.updateStudent(1, invalidData);
        });

        assertNotNull(exception);
        verify(studentRepository, never()).update(any());
    }

    @Test
    void softDeleteStudent_shouldDeleteStudent_whenStudentExists() {
        Integer studentId = 1;
        Integer teacherId = 1;
        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García")
                .build();

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(existingStudent));

        studentService.softDeleteStudent(studentId);

        verify(studentClassRepository, times(1)).softDeleteByStudentId(studentId);
        verify(studentRepository, times(1)).softDelete(studentId, teacherId);
    }

    @Test
    void softDeleteStudent_shouldThrowNotFoundException_whenStudentDoesNotExist() {
        Integer studentId = 999;
        Integer teacherId = 1;

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.empty());

        StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> {
            studentService.softDeleteStudent(studentId);
        });

        assertNotNull(exception);
        verify(studentRepository, never()).softDelete(any(), any());
    }

    @Test
    void saveStudentPhoto_shouldSavePhoto_whenStudentExists() {
        Integer studentId = 1;
        Integer teacherId = 1;
        byte[] photoBytes = "test photo content".getBytes();
        MultipartFile file = mock(MultipartFile.class);

        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García")
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        try {
            when(file.getBytes()).thenReturn(photoBytes);
        } catch (Exception e) {
            fail("Mock setup failed");
        }

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(existingStudent));
        when(studentRepository.update(any(Student.class))).thenReturn(existingStudent);

        String result = studentService.saveStudentPhoto(studentId, file);

        assertNotNull(result);
        assertEquals("1.jpg", result);
        verify(studentRepository, times(1)).update(any(Student.class));
    }

    @Test
    void saveStudentPhoto_shouldThrowException_whenFileSizeExceeds500KB() {
        Integer studentId = 1;
        Integer teacherId = 1;
        byte[] photoBytes = new byte[600 * 1024];
        MultipartFile file = mock(MultipartFile.class);

        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García")
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        try {
            when(file.getBytes()).thenReturn(photoBytes);
        } catch (Exception e) {
            fail("Mock setup failed");
        }

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(existingStudent));

        assertThrows(org.web.codefm.domain.exception.teachernotebook.StudentPhotoUploadException.class, () -> {
            studentService.saveStudentPhoto(studentId, file);
        });

        verify(studentRepository, never()).update(any());
    }

    @Test
    void saveStudentPhoto_shouldThrowException_whenExtensionIsInvalid() {
        Integer studentId = 1;
        Integer teacherId = 1;
        byte[] photoBytes = "test photo content".getBytes();
        MultipartFile file = mock(MultipartFile.class);

        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García")
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("photo.pdf");
        try {
            when(file.getBytes()).thenReturn(photoBytes);
        } catch (Exception e) {
            fail("Mock setup failed");
        }

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(existingStudent));

        assertThrows(org.web.codefm.domain.exception.teachernotebook.StudentPhotoUploadException.class, () -> {
            studentService.saveStudentPhoto(studentId, file);
        });

        verify(studentRepository, never()).update(any());
    }

    @Test
    void saveStudentPhoto_shouldThrowNotFoundException_whenStudentDoesNotExist() {
        Integer studentId = 999;
        Integer teacherId = 1;
        byte[] photoBytes = "test photo content".getBytes();
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        try {
            when(file.getBytes()).thenReturn(photoBytes);
        } catch (Exception e) {
            fail("Mock setup failed");
        }

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.empty());

        StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> {
            studentService.saveStudentPhoto(studentId, file);
        });

        assertNotNull(exception);
        verify(studentRepository, never()).update(any());
    }

    @Test
    void searchStudents_shouldReturnStudents_whenIdProvided() {
        Integer teacherId = 1;
        Integer id = 1;
        Student student = Student.builder()
                .id(id)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .build();

        when(studentRepository.searchStudents(teacherId, id, null, null))
                .thenReturn(Arrays.asList(student));

        List<Student> result = studentService.searchStudents(id, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
        verify(studentRepository, times(1)).searchStudents(teacherId, id, null, null);
    }

    @Test
    void searchStudents_shouldThrowException_whenNoFiltersProvided() {
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_SEARCH_NO_FILTERS), eq(null), any(Locale.class)))
                .thenReturn("At least one filter is required");

        StudentSearchValidationException exception = assertThrows(
                StudentSearchValidationException.class,
                () -> studentService.searchStudents(null, null, null)
        );

        assertNotNull(exception);
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.STUDENT_SEARCH_NO_FILTERS), eq(null), any(Locale.class));
        verify(studentRepository, never()).searchStudents(any(), any(), any(), any());
    }


    @Test
    void getAllStudents_shouldReturnAllStudents() {
        Integer teacherId = 1;
        Student student1 = Student.builder()
                .id(1)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .build();

        Student student2 = Student.builder()
                .id(2)
                .teacherId(teacherId)
                .name("María")
                .surnames("Pérez Martínez")
                .build();

        List<Student> expectedStudents = Arrays.asList(student1, student2);
        Map<Integer, List<Integer>> studentClassMap = new HashMap<>();
        studentClassMap.put(1, Arrays.asList(1, 2));
        studentClassMap.put(2, Arrays.asList(3));

        when(studentRepository.findAllByTeacherId(teacherId)).thenReturn(expectedStudents);
        when(studentClassRepository.findClassIdsByTeacherId(teacherId)).thenReturn(studentClassMap);

        List<Student> result = studentService.getAllStudents();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Juan", result.get(0).getName());
        assertEquals("María", result.get(1).getName());
        assertEquals(2, result.get(0).getClassIds().size());
        assertEquals(Arrays.asList(1, 2), result.get(0).getClassIds());
        assertEquals(1, result.get(1).getClassIds().size());
        assertEquals(Arrays.asList(3), result.get(1).getClassIds());
        verify(studentRepository, times(1)).findAllByTeacherId(teacherId);
        verify(studentClassRepository, times(1)).findClassIdsByTeacherId(teacherId);
        verify(studentClassRepository, never()).findClassIdsByStudentId(any());
    }

    @Test
    void getAllStudents_shouldReturnEmptyListWhenNoStudents() {
        Integer teacherId = 1;
        List<Student> emptyList = List.of();
        Map<Integer, List<Integer>> emptyMap = new HashMap<>();

        when(studentRepository.findAllByTeacherId(teacherId)).thenReturn(emptyList);
        when(studentClassRepository.findClassIdsByTeacherId(teacherId)).thenReturn(emptyMap);

        List<Student> result = studentService.getAllStudents();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentRepository, times(1)).findAllByTeacherId(teacherId);
        verify(studentClassRepository, times(1)).findClassIdsByTeacherId(teacherId);
        verify(studentClassRepository, never()).findClassIdsByStudentId(any());
    }

    @Test
    void saveStudentPhoto_shouldThrowUploadException_whenGetBytesThrowsIOException() throws Exception {
        Integer studentId = 1;
        Integer teacherId = 1;
        MultipartFile file = mock(MultipartFile.class);

        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García")
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getBytes()).thenThrow(new IOException("Error reading file"));

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(existingStudent));

        StudentPhotoUploadException exception = assertThrows(StudentPhotoUploadException.class, () ->
                studentService.saveStudentPhoto(studentId, file));

        assertNotNull(exception);
        assertEquals("[Code: 1000, CodeDescription: GENERIC_ERROR, ErrorDescription: Error uploading student photo.]", exception.getMessage());
        verify(studentRepository, never()).update(any());
    }

    @Test
    void saveStudentPhoto_shouldThrowUploadException_whenFileWriteThrowsIOException() throws Exception {
        Integer studentId = 1;
        Integer teacherId = 1;
        byte[] photoBytes = "test photo content".getBytes();
        MultipartFile file = mock(MultipartFile.class);

        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García")
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getBytes()).thenReturn(photoBytes);

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(existingStudent));

        ReflectionTestUtils.setField(studentService, "photosDirectory", "/nonexistent/readonly/path");

        StudentPhotoUploadException exception = assertThrows(StudentPhotoUploadException.class, () ->
                studentService.saveStudentPhoto(studentId, file));

        assertNotNull(exception);
        assertEquals("[Code: 1000, CodeDescription: GENERIC_ERROR, ErrorDescription: Error uploading student photo.]", exception.getMessage());
        verify(studentRepository, never()).update(any());

        ReflectionTestUtils.setField(studentService, "photosDirectory", tempDir.toString());
    }

    @Test
    void saveStudentPhoto_shouldThrowUploadException_whenFileIsEmpty() throws Exception {
        Integer studentId = 1;
        Integer teacherId = 1;
        MultipartFile file = mock(MultipartFile.class);

        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García")
                .build();

        when(file.isEmpty()).thenReturn(true);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getBytes()).thenReturn(new byte[0]);

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(existingStudent));

        StudentPhotoUploadException exception = assertThrows(StudentPhotoUploadException.class, () ->
                studentService.saveStudentPhoto(studentId, file));

        assertNotNull(exception);
        assertEquals("[Code: 1000, CodeDescription: GENERIC_ERROR, ErrorDescription: Photo file is required.]", exception.getMessage());
        verify(studentRepository, never()).update(any());
    }

    @Test
    void getStudentPhoto_shouldThrowNotFoundException_whenFileReadThrowsIOException() throws Exception {
        Integer studentId = 1;
        Integer teacherId = 1;
        String photoFileName = "nonexistent.jpg";

        Student student = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .photo(photoFileName)
                .build();

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(student));

        StudentPhotoNotFoundException exception = assertThrows(StudentPhotoNotFoundException.class, () ->
                studentService.getStudentPhoto(studentId));

        assertNotNull(exception);
        assertEquals("[Code: 1003, CodeDescription: RESOURCE_NOT_FOUND, ErrorDescription: Student photo not found.]", exception.getMessage());
    }

    @Test
    void deleteStudentPhoto_shouldThrowDeleteException_whenFileDeleteThrowsIOException() throws Exception {
        Integer studentId = 1;
        Integer teacherId = 1;
        String photoFileName = "1.jpg";

        Student student = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .photo(photoFileName)
                .build();

        Path photoPath = tempDir.resolve(photoFileName);
        java.nio.file.Files.write(photoPath, new byte[]{1, 2, 3});
        photoPath.toFile().setReadOnly();

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(student));

        try {
            studentService.deleteStudentPhoto(studentId);
            photoPath.toFile().setWritable(true);
        } catch (StudentPhotoDeleteException e) {
            photoPath.toFile().setWritable(true);
            assertEquals("[Code: 1000, CodeDescription: GENERIC_ERROR, ErrorDescription: Error deleting student photo.]", e.getMessage());
            return;
        }

        photoPath.toFile().setWritable(true);
    }

    @Test
    void getStudentPhoto_shouldThrowNotFoundException_whenPhotoFieldIsNull() {
        Integer studentId = 1;
        Integer teacherId = 1;

        Student student = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .photo(null)
                .build();

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(student));

        StudentPhotoNotFoundException exception = assertThrows(StudentPhotoNotFoundException.class, () ->
                studentService.getStudentPhoto(studentId));

        assertNotNull(exception);
        assertEquals("[Code: 1003, CodeDescription: RESOURCE_NOT_FOUND, ErrorDescription: Student photo not found.]", exception.getMessage());
    }

    @Test
    void deleteStudentPhoto_shouldThrowNotFoundException_whenPhotoFieldIsNull() {
        Integer studentId = 1;
        Integer teacherId = 1;

        Student student = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .photo(null)
                .build();

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(student));

        StudentPhotoNotFoundException exception = assertThrows(StudentPhotoNotFoundException.class, () ->
                studentService.deleteStudentPhoto(studentId));

        assertNotNull(exception);
        assertEquals("[Code: 1003, CodeDescription: RESOURCE_NOT_FOUND, ErrorDescription: Student photo not found.]", exception.getMessage());
    }

    @Test
    void saveStudentPhoto_shouldThrowUploadException_whenFileIsNull() throws Exception {
        Integer studentId = 1;
        Integer teacherId = 1;

        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García")
                .build();

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(existingStudent));

        StudentPhotoUploadException exception = assertThrows(StudentPhotoUploadException.class, () ->
                studentService.saveStudentPhoto(studentId, null));

        assertNotNull(exception);
        assertEquals("[Code: 1000, CodeDescription: GENERIC_ERROR, ErrorDescription: Photo file is required.]", exception.getMessage());
        verify(studentRepository, never()).update(any());
    }
}
