package org.web.codefm.application.service.teachernotebook;

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
import org.web.codefm.domain.exception.teachernotebook.StudentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentSearchValidationException;
import org.web.codefm.domain.exception.teachernotebook.StudentValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.StudentRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

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
        assertEquals("1.jpg", result); // Should be studentId.extension
        verify(studentRepository, times(1)).update(any(Student.class));
    }

    @Test
    void saveStudentPhoto_shouldThrowException_whenFileSizeExceeds500KB() {
        Integer studentId = 1;
        Integer teacherId = 1;
        byte[] photoBytes = new byte[600 * 1024]; // 600KB - exceeds limit
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
    void saveStudentPhoto_shouldAcceptPngExtension() {
        Integer studentId = 2;
        Integer teacherId = 1;
        byte[] photoBytes = "test photo content".getBytes();
        MultipartFile file = mock(MultipartFile.class);

        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("María")
                .surnames("López")
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("photo.png");
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
        assertEquals("2.png", result); // Should be studentId.extension
        verify(studentRepository, times(1)).update(any(Student.class));
    }

    @Test
    void saveStudentPhoto_shouldAcceptJpegExtension() {
        Integer studentId = 3;
        Integer teacherId = 1;
        byte[] photoBytes = "test photo content".getBytes();
        MultipartFile file = mock(MultipartFile.class);

        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Pedro")
                .surnames("Martínez")
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("photo.JPEG");
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
        assertEquals("3.jpeg", result); // Should be lowercase
        verify(studentRepository, times(1)).update(any(Student.class));
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
    void saveStudentPhoto_shouldThrowException_whenPhotoIsEmpty() {
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
        try {
            when(file.getBytes()).thenReturn(new byte[0]);
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
    void saveStudentPhoto_shouldThrowException_whenPhotoIsNull() {
        Integer studentId = 1;
        Integer teacherId = 1;
        MultipartFile file = null;

        Student existingStudent = Student.builder()
                .id(studentId)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García")
                .build();

        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(existingStudent));

        assertThrows(org.web.codefm.domain.exception.teachernotebook.StudentPhotoUploadException.class, () -> {
            studentService.saveStudentPhoto(studentId, file);
        });

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
    void searchStudents_shouldReturnStudents_whenNameProvided() {
        Integer teacherId = 1;
        String name = "Juan";
        Student student = Student.builder()
                .id(1)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .build();

        when(studentRepository.searchStudents(teacherId, null, name, null))
                .thenReturn(Arrays.asList(student));

        List<Student> result = studentService.searchStudents(null, name, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(studentRepository, times(1)).searchStudents(teacherId, null, name, null);
    }

    @Test
    void searchStudents_shouldReturnStudents_whenSurnamesProvided() {
        Integer teacherId = 1;
        String surnames = "García";
        Student student = Student.builder()
                .id(1)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .build();

        when(studentRepository.searchStudents(teacherId, null, null, surnames))
                .thenReturn(Arrays.asList(student));

        List<Student> result = studentService.searchStudents(null, null, surnames);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(studentRepository, times(1)).searchStudents(teacherId, null, null, surnames);
    }

    @Test
    void searchStudents_shouldReturnStudents_whenMultipleFiltersProvided() {
        Integer teacherId = 1;
        String name = "Juan";
        String surnames = "García";
        Student student = Student.builder()
                .id(1)
                .teacherId(teacherId)
                .name("Juan")
                .surnames("García López")
                .build();

        when(studentRepository.searchStudents(teacherId, null, name, surnames))
                .thenReturn(Arrays.asList(student));

        List<Student> result = studentService.searchStudents(null, name, surnames);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(studentRepository, times(1)).searchStudents(teacherId, null, name, surnames);
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
    void searchStudents_shouldThrowException_whenOnlyEmptyStringsProvided() {
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_SEARCH_NO_FILTERS), eq(null), any(Locale.class)))
                .thenReturn("At least one filter is required");

        StudentSearchValidationException exception = assertThrows(
                StudentSearchValidationException.class,
                () -> studentService.searchStudents(null, "  ", "  ")
        );

        assertNotNull(exception);
        verify(studentRepository, never()).searchStudents(any(), any(), any(), any());
    }

    @Test
    void searchStudents_shouldReturnEmptyList_whenNoMatchesFound() {
        Integer teacherId = 1;
        String name = "NonExistent";

        when(studentRepository.searchStudents(teacherId, null, name, null))
                .thenReturn(Arrays.asList());

        List<Student> result = studentService.searchStudents(null, name, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentRepository, times(1)).searchStudents(teacherId, null, name, null);
    }
}
