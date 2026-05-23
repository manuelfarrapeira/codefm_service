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
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.exception.teachernotebook.*;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ExerciseStudentGradeRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
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
    private ExerciseStudentGradeRepository exerciseStudentGradeRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    private StudentServiceImpl studentService;

    @BeforeEach
    void beforeEach() {
        studentService = new StudentServiceImpl(studentRepository, studentClassRepository, messageSource, sessionUser);
        ReflectionTestUtils.setField(studentService, "photosDirectory", tempDir.toString());

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(1);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("Student name is required.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_NAME_MIN_LENGTH), eq(null), any(Locale.class)))
                .thenReturn("Student name must be at least 3 characters.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_SURNAMES_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("Student surnames are required.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_SURNAMES_MIN_LENGTH), eq(null), any(Locale.class)))
                .thenReturn("Student surnames must be at least 3 characters.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_GENDER_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("Student gender is required.");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_GENDER_INVALID), eq(null), any(Locale.class)))
                .thenReturn("Gender must be M (Male) or F (Female).");
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_SHAPE_INVALID), any(Object[].class), any(Locale.class)))
                .thenReturn("Shape must be Square, Circle or Triangle.");
        when(messageSource.getMessage(eq(MessageKeys.SHAPE_NAME_SQUARE), eq(null), any(Locale.class)))
                .thenReturn("Square");
        when(messageSource.getMessage(eq(MessageKeys.SHAPE_NAME_CIRCLE), eq(null), any(Locale.class)))
                .thenReturn("Circle");
        when(messageSource.getMessage(eq(MessageKeys.SHAPE_NAME_TRIANGLE), eq(null), any(Locale.class)))
                .thenReturn("Triangle");
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

    @Nested
    class CreateStudent {

        @Test
        void when_data_is_valid_expect_save_student() {
            final Student studentToCreate = Student.builder()
                    .name("Juan").surnames("García López").gender("M")
                    .dateOfBirth(LocalDate.of(2010, 3, 15)).additionalInfo("Good student").build();
            final Student expectedStudent = Student.builder()
                    .name("Juan").surnames("García López").gender("M").teacherId(1)
                    .dateOfBirth(LocalDate.of(2010, 3, 15)).additionalInfo("Good student").build();
            when(studentRepository.save(any(Student.class))).thenReturn(expectedStudent);

            final Student createdStudent = studentService.createStudent(studentToCreate);

            assertThat(createdStudent).isNotNull();
            assertThat(createdStudent.getName()).isEqualTo("Juan");
            assertThat(createdStudent.getSurnames()).isEqualTo("García López");
            assertThat(createdStudent.getTeacherId()).isEqualTo(1);
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        void when_gender_is_m_expect_save_student() {
            final Student studentToCreate = Student.builder()
                    .name("Juan").surnames("García López").gender("M").build();
            final Student expectedStudent = Student.builder()
                    .name("Juan").surnames("García López").gender("M").teacherId(1).build();
            when(studentRepository.save(any(Student.class))).thenReturn(expectedStudent);

            final Student createdStudent = studentService.createStudent(studentToCreate);

            assertThat(createdStudent).isNotNull();
            assertThat(createdStudent.getGender()).isEqualTo("M");
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        void when_gender_is_f_expect_save_student() {
            final Student studentToCreate = Student.builder()
                    .name("María").surnames("López García").gender("F").build();
            final Student expectedStudent = Student.builder()
                    .name("María").surnames("López García").gender("F").teacherId(1).build();
            when(studentRepository.save(any(Student.class))).thenReturn(expectedStudent);

            final Student createdStudent = studentService.createStudent(studentToCreate);

            assertThat(createdStudent).isNotNull();
            assertThat(createdStudent.getGender()).isEqualTo("F");
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        void when_shape_is_square_expect_save_student() {
            final Student studentToCreate = Student.builder()
                    .name("Juan").surnames("García López").gender("M").shape("SQUARE").build();
            final Student expectedStudent = Student.builder()
                    .name("Juan").surnames("García López").gender("M").shape("SQUARE").teacherId(1).build();
            when(studentRepository.save(any(Student.class))).thenReturn(expectedStudent);

            final Student createdStudent = studentService.createStudent(studentToCreate);

            assertThat(createdStudent).isNotNull();
            assertThat(createdStudent.getShape()).isEqualTo("SQUARE");
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        void when_shape_is_null_expect_save_student() {
            final Student studentToCreate = Student.builder()
                    .name("Juan").surnames("García López").gender("M").shape(null).build();
            final Student expectedStudent = Student.builder()
                    .name("Juan").surnames("García López").gender("M").teacherId(1).build();
            when(studentRepository.save(any(Student.class))).thenReturn(expectedStudent);

            final Student createdStudent = studentService.createStudent(studentToCreate);

            assertThat(createdStudent).isNotNull();
            assertThat(createdStudent.getShape()).isNull();
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        void when_shape_is_lowercase_expect_normalize_to_upper_case() {
            final Student studentToCreate = Student.builder()
                    .name("Juan").surnames("García López").gender("M").shape("circle").build();
            final Student expectedStudent = Student.builder()
                    .name("Juan").surnames("García López").gender("M").shape("CIRCLE").teacherId(1).build();
            when(studentRepository.save(any(Student.class))).thenReturn(expectedStudent);

            final Student createdStudent = studentService.createStudent(studentToCreate);

            assertThat(createdStudent).isNotNull();
            assertThat(createdStudent.getShape()).isEqualTo("CIRCLE");
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        void when_name_is_null_expect_throw_validation_exception() {
            final Student studentWithoutName = Student.builder()
                    .name(null).surnames("García López").build();

            final ThrowingCallable call = () -> studentService.createStudent(studentWithoutName);
            final StudentValidationException exception = catchThrowableOfType(call, StudentValidationException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getErrors()).anyMatch(e -> e.getParam().equals("name"));
            verify(studentRepository, never()).save(any());
        }

        @Test
        void when_name_is_empty_expect_throw_validation_exception() {
            final Student studentWithEmptyName = Student.builder()
                    .name("").surnames("García López").build();

            final ThrowingCallable call = () -> studentService.createStudent(studentWithEmptyName);
            final StudentValidationException exception = catchThrowableOfType(call, StudentValidationException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getErrors()).anyMatch(e -> e.getParam().equals("name"));
            verify(studentRepository, never()).save(any());
        }

        @Test
        void when_name_is_too_short_expect_throw_validation_exception() {
            final Student studentWithShortName = Student.builder()
                    .name("Ab").surnames("García López").build();

            final ThrowingCallable call = () -> studentService.createStudent(studentWithShortName);
            final StudentValidationException exception = catchThrowableOfType(call, StudentValidationException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getErrors()).anyMatch(e -> e.getParam().equals("name"));
            verify(studentRepository, never()).save(any());
        }

        @Test
        void when_surnames_is_null_expect_throw_validation_exception() {
            final Student studentWithoutSurnames = Student.builder()
                    .name("Juan").surnames(null).build();

            final ThrowingCallable call = () -> studentService.createStudent(studentWithoutSurnames);
            final StudentValidationException exception = catchThrowableOfType(call, StudentValidationException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getErrors()).anyMatch(e -> e.getParam().equals("surnames"));
            verify(studentRepository, never()).save(any());
        }

        @Test
        void when_surnames_are_too_short_expect_throw_validation_exception() {
            final Student studentWithShortSurnames = Student.builder()
                    .name("Juan").surnames("Ab").build();

            final ThrowingCallable call = () -> studentService.createStudent(studentWithShortSurnames);
            final StudentValidationException exception = catchThrowableOfType(call, StudentValidationException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getErrors()).anyMatch(e -> e.getParam().equals("surnames"));
            verify(studentRepository, never()).save(any());
        }

        @Test
        void when_gender_is_null_expect_throw_validation_exception() {
            final Student studentWithoutGender = Student.builder()
                    .name("Juan").surnames("García López").gender(null).build();

            final ThrowingCallable call = () -> studentService.createStudent(studentWithoutGender);
            final StudentValidationException exception = catchThrowableOfType(call, StudentValidationException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getErrors()).anyMatch(e -> e.getParam().equals("gender"));
            verify(studentRepository, never()).save(any());
        }

        @Test
        void when_gender_is_empty_expect_throw_validation_exception() {
            final Student studentWithEmptyGender = Student.builder()
                    .name("Juan").surnames("García López").gender("").build();

            final ThrowingCallable call = () -> studentService.createStudent(studentWithEmptyGender);
            final StudentValidationException exception = catchThrowableOfType(call, StudentValidationException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getErrors()).anyMatch(e -> e.getParam().equals("gender"));
            verify(studentRepository, never()).save(any());
        }

        @Test
        void when_gender_is_invalid_expect_throw_validation_exception() {
            final Student studentWithInvalidGender = Student.builder()
                    .name("Juan").surnames("García López").gender("X").build();

            final ThrowingCallable call = () -> studentService.createStudent(studentWithInvalidGender);
            final StudentValidationException exception = catchThrowableOfType(call, StudentValidationException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getErrors()).anyMatch(e -> e.getParam().equals("gender"));
            verify(studentRepository, never()).save(any());
        }

        @Test
        void when_shape_is_invalid_expect_throw_validation_exception() {
            final Student studentWithInvalidShape = Student.builder()
                    .name("Juan").surnames("García López").gender("M").shape("HEXAGON").build();

            final ThrowingCallable call = () -> studentService.createStudent(studentWithInvalidShape);
            final StudentValidationException exception = catchThrowableOfType(call, StudentValidationException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getErrors()).anyMatch(e -> e.getParam().equals("shape"));
            verify(studentRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateStudent {

        @Test
        void when_data_is_valid_expect_update_student() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final Student existingStudent = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").gender("F").surnames("García").build();
            final Student updatedData = Student.builder()
                    .name("Juan Carlos").surnames("García López").gender("F").dateOfBirth(LocalDate.of(2010, 3, 15)).build();

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(existingStudent));
            when(studentRepository.update(any(Student.class))).thenReturn(existingStudent);

            final Student result = studentService.updateStudent(studentId, updatedData);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Juan Carlos");
            assertThat(result.getSurnames()).isEqualTo("García López");
            verify(studentRepository, times(1)).update(any(Student.class));
        }

        @Test
        void when_student_does_not_exist_expect_throw_not_found_exception() {
            final Integer studentId = 999;
            final Student updatedData = Student.builder()
                    .name("Juan").surnames("García López").gender("M").build();

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, 1))
                    .thenReturn(Optional.empty());

            final ThrowingCallable call = () -> studentService.updateStudent(studentId, updatedData);

            assertThatThrownBy(call).isInstanceOf(StudentNotFoundException.class);
            verify(studentRepository, never()).update(any());
        }

        @Test
        void when_name_is_invalid_expect_throw_validation_exception() {
            final Student invalidData = Student.builder()
                    .name("A").surnames("García López").gender("M").build();

            final ThrowingCallable call = () -> studentService.updateStudent(1, invalidData);

            assertThatThrownBy(call).isInstanceOf(StudentValidationException.class);
            verify(studentRepository, never()).update(any());
        }
    }

    @Nested
    class SoftDeleteStudent {

        @Test
        void when_student_exists_expect_delete_student() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final Student existingStudent = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García").build();

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(existingStudent));

            studentService.softDeleteStudent(studentId);

            verify(studentRepository, times(1)).softDelete(studentId, teacherId);
            verify(exerciseStudentGradeRepository, never()).softDeleteByStudentId(any());
            verify(studentClassRepository, never()).softDeleteByStudentId(any());
        }

        @Test
        void when_student_does_not_exist_expect_throw_not_found_exception() {
            final Integer studentId = 999;

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, 1))
                    .thenReturn(Optional.empty());

            final ThrowingCallable call = () -> studentService.softDeleteStudent(studentId);

            assertThatThrownBy(call).isInstanceOf(StudentNotFoundException.class);
            verify(studentRepository, never()).softDelete(any(), any());
        }
    }

    @Nested
    class GetAllStudents {

        @Test
        void when_students_exist_expect_return_all_students() {
            final Integer teacherId = 1;
            final Student student1 = Student.builder()
                    .id(1).teacherId(teacherId).name("Juan").surnames("García López").build();
            final Student student2 = Student.builder()
                    .id(2).teacherId(teacherId).name("María").surnames("Pérez Martínez").build();
            final List<Student> expectedStudents = Arrays.asList(student1, student2);
            final Map<Integer, List<Integer>> studentClassMap = new HashMap<>();
            studentClassMap.put(1, Arrays.asList(1, 2));
            studentClassMap.put(2, Arrays.asList(3));

            when(studentRepository.findAllByTeacherId(teacherId)).thenReturn(expectedStudents);
            when(studentClassRepository.findClassIdsByTeacherId(teacherId)).thenReturn(studentClassMap);

            final List<Student> result = studentService.getAllStudents();

            assertThat(result).isNotNull().hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Juan");
            assertThat(result.get(1).getName()).isEqualTo("María");
            assertThat(result.get(0).getClassIds()).hasSize(2).isEqualTo(Arrays.asList(1, 2));
            assertThat(result.get(1).getClassIds()).hasSize(1).isEqualTo(Arrays.asList(3));
            verify(studentRepository, times(1)).findAllByTeacherId(teacherId);
            verify(studentClassRepository, times(1)).findClassIdsByTeacherId(teacherId);
            verify(studentClassRepository, never()).findClassIdsByStudentId(any());
        }

        @Test
        void when_no_students_exist_expect_return_empty_list() {
            final Integer teacherId = 1;

            when(studentRepository.findAllByTeacherId(teacherId)).thenReturn(List.of());
            when(studentClassRepository.findClassIdsByTeacherId(teacherId)).thenReturn(new HashMap<>());

            final List<Student> result = studentService.getAllStudents();

            assertThat(result).isNotNull().isEmpty();
            verify(studentRepository, times(1)).findAllByTeacherId(teacherId);
            verify(studentClassRepository, times(1)).findClassIdsByTeacherId(teacherId);
            verify(studentClassRepository, never()).findClassIdsByStudentId(any());
        }
    }

    @Nested
    class SearchStudents {

        @Test
        void when_id_provided_expect_return_students() {
            final Integer teacherId = 1;
            final Integer id = 1;
            final Student student = Student.builder()
                    .id(id).teacherId(teacherId).name("Juan").surnames("García López").build();

            when(studentRepository.searchStudents(teacherId, id, null, null))
                    .thenReturn(Arrays.asList(student));

            final List<Student> result = studentService.searchStudents(id, null, null);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Juan");
            verify(studentRepository, times(1)).searchStudents(teacherId, id, null, null);
        }

        @Test
        void when_no_filters_provided_expect_throw_exception() {
            when(messageSource.getMessage(eq(MessageKeys.STUDENT_SEARCH_NO_FILTERS), eq(null), any(Locale.class)))
                    .thenReturn("At least one filter is required");

            final ThrowingCallable call = () -> studentService.searchStudents(null, null, null);

            assertThatThrownBy(call).isInstanceOf(StudentSearchValidationException.class);
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.STUDENT_SEARCH_NO_FILTERS), eq(null), any(Locale.class));
            verify(studentRepository, never()).searchStudents(any(), any(), any(), any());
        }
    }

    @Nested
    class SaveStudentPhoto {

        @Test
        void when_student_exists_expect_save_photo() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final byte[] photoBytes = "test photo content".getBytes();
            final MultipartFile file = mock(MultipartFile.class);
            final Student existingStudent = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García").build();

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

            final String result = studentService.saveStudentPhoto(studentId, file);

            assertThat(result).isNotNull().isEqualTo("1.jpg");
            verify(studentRepository, times(1)).update(any(Student.class));
        }

        @Test
        void when_file_size_exceeds_500kb_expect_throw_exception() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final byte[] photoBytes = new byte[600 * 1024];
            final MultipartFile file = mock(MultipartFile.class);
            final Student existingStudent = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García").build();

            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("photo.jpg");
            try {
                when(file.getBytes()).thenReturn(photoBytes);
            } catch (Exception e) {
                fail("Mock setup failed");
            }

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(existingStudent));

            final ThrowingCallable call = () -> studentService.saveStudentPhoto(studentId, file);

            assertThatThrownBy(call).isInstanceOf(StudentPhotoUploadException.class);
            verify(studentRepository, never()).update(any());
        }

        @Test
        void when_extension_is_invalid_expect_throw_exception() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final byte[] photoBytes = "test photo content".getBytes();
            final MultipartFile file = mock(MultipartFile.class);
            final Student existingStudent = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García").build();

            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("photo.pdf");
            try {
                when(file.getBytes()).thenReturn(photoBytes);
            } catch (Exception e) {
                fail("Mock setup failed");
            }

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(existingStudent));

            final ThrowingCallable call = () -> studentService.saveStudentPhoto(studentId, file);

            assertThatThrownBy(call).isInstanceOf(StudentPhotoUploadException.class);
            verify(studentRepository, never()).update(any());
        }

        @Test
        void when_student_does_not_exist_expect_throw_not_found_exception() {
            final Integer studentId = 999;
            final Integer teacherId = 1;
            final byte[] photoBytes = "test photo content".getBytes();
            final MultipartFile file = mock(MultipartFile.class);

            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("photo.jpg");
            try {
                when(file.getBytes()).thenReturn(photoBytes);
            } catch (Exception e) {
                fail("Mock setup failed");
            }

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.empty());

            final ThrowingCallable call = () -> studentService.saveStudentPhoto(studentId, file);

            assertThatThrownBy(call).isInstanceOf(StudentNotFoundException.class);
            verify(studentRepository, never()).update(any());
        }

        @Test
        void when_get_bytes_throws_io_exception_expect_throw_upload_exception() throws Exception {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final MultipartFile file = mock(MultipartFile.class);
            final Student existingStudent = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García").build();

            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("photo.jpg");
            when(file.getBytes()).thenThrow(new IOException("Error reading file"));

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(existingStudent));

            final ThrowingCallable call = () -> studentService.saveStudentPhoto(studentId, file);
            final StudentPhotoUploadException exception = catchThrowableOfType(call, StudentPhotoUploadException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("[Code: 1000, CodeDescription: GENERIC_ERROR, ErrorDescription: Error uploading student photo.]");
            verify(studentRepository, never()).update(any());
        }

        @Test
        void when_file_write_throws_io_exception_expect_throw_upload_exception() throws Exception {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final byte[] photoBytes = "test photo content".getBytes();
            final MultipartFile file = mock(MultipartFile.class);
            final Student existingStudent = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García").build();

            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("photo.jpg");
            when(file.getBytes()).thenReturn(photoBytes);

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(existingStudent));

            ReflectionTestUtils.setField(studentService, "photosDirectory", "/nonexistent/readonly/path");

            final ThrowingCallable call = () -> studentService.saveStudentPhoto(studentId, file);
            final StudentPhotoUploadException exception = catchThrowableOfType(call, StudentPhotoUploadException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("[Code: 1000, CodeDescription: GENERIC_ERROR, ErrorDescription: Error uploading student photo.]");
            verify(studentRepository, never()).update(any());

            ReflectionTestUtils.setField(studentService, "photosDirectory", tempDir.toString());
        }

        @Test
        void when_file_is_empty_expect_throw_upload_exception() throws Exception {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final MultipartFile file = mock(MultipartFile.class);
            final Student existingStudent = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García").build();

            when(file.isEmpty()).thenReturn(true);
            when(file.getOriginalFilename()).thenReturn("photo.jpg");
            when(file.getBytes()).thenReturn(new byte[0]);

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(existingStudent));

            final ThrowingCallable call = () -> studentService.saveStudentPhoto(studentId, file);
            final StudentPhotoUploadException exception = catchThrowableOfType(call, StudentPhotoUploadException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("[Code: 1000, CodeDescription: GENERIC_ERROR, ErrorDescription: Photo file is required.]");
            verify(studentRepository, never()).update(any());
        }

        @Test
        void when_file_is_null_expect_throw_upload_exception() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final Student existingStudent = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García").build();

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(existingStudent));

            final ThrowingCallable call = () -> studentService.saveStudentPhoto(studentId, null);
            final StudentPhotoUploadException exception = catchThrowableOfType(call, StudentPhotoUploadException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("[Code: 1000, CodeDescription: GENERIC_ERROR, ErrorDescription: Photo file is required.]");
            verify(studentRepository, never()).update(any());
        }
    }

    @Nested
    class GetStudentPhoto {

        @Test
        void when_photo_exists_expect_return_photo_bytes() throws Exception {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final String photoFileName = "1.jpg";
            final byte[] expectedPhotoBytes = new byte[]{1, 2, 3, 4, 5};
            final Student student = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García López").photo(photoFileName).build();

            java.nio.file.Files.write(tempDir.resolve(photoFileName), expectedPhotoBytes);
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(student));

            final byte[] result = studentService.getStudentPhoto(studentId);

            assertThat(result).isNotNull().isEqualTo(expectedPhotoBytes);
            verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
        }

        @Test
        void when_student_not_found_expect_throw_not_found_exception() {
            final Integer studentId = 99;
            final Integer teacherId = 1;

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.empty());

            final ThrowingCallable call = () -> studentService.getStudentPhoto(studentId);

            assertThatThrownBy(call).isInstanceOf(StudentNotFoundException.class);
            verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
        }

        @Test
        void when_photo_is_empty_expect_throw_not_found_exception() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final Student student = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García López").build();

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(student));

            final ThrowingCallable call = () -> studentService.getStudentPhoto(studentId);

            assertThatThrownBy(call).isInstanceOf(StudentPhotoNotFoundException.class);
            verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
        }

        @Test
        void when_file_read_throws_io_exception_expect_throw_not_found_exception() throws Exception {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final String photoFileName = "nonexistent.jpg";
            final Student student = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García López").photo(photoFileName).build();

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(student));

            final ThrowingCallable call = () -> studentService.getStudentPhoto(studentId);
            final StudentPhotoNotFoundException exception = catchThrowableOfType(call, StudentPhotoNotFoundException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("[Code: 1003, CodeDescription: RESOURCE_NOT_FOUND, ErrorDescription: Student photo not found.]");
        }

        @Test
        void when_photo_field_is_null_expect_throw_not_found_exception() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final Student student = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García López").photo(null).build();

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(student));

            final ThrowingCallable call = () -> studentService.getStudentPhoto(studentId);
            final StudentPhotoNotFoundException exception = catchThrowableOfType(call, StudentPhotoNotFoundException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("[Code: 1003, CodeDescription: RESOURCE_NOT_FOUND, ErrorDescription: Student photo not found.]");
        }
    }

    @Nested
    class DeleteStudentPhoto {

        @Test
        void when_photo_exists_expect_delete_photo_and_update_student() throws Exception {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final String photoFileName = "1.jpg";
            final byte[] photoBytes = new byte[]{1, 2, 3, 4, 5};
            final Student student = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García López").photo(photoFileName).build();

            java.nio.file.Files.write(tempDir.resolve(photoFileName), photoBytes);
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(student));
            when(studentRepository.update(any(Student.class))).thenReturn(student);

            studentService.deleteStudentPhoto(studentId);

            assertThat(tempDir.resolve(photoFileName)).doesNotExist();
            assertThat(student.getPhoto()).isNull();
            verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
            verify(studentRepository, times(1)).update(any(Student.class));
        }

        @Test
        void when_student_not_found_expect_throw_not_found_exception() {
            final Integer studentId = 99;
            final Integer teacherId = 1;

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.empty());

            final ThrowingCallable call = () -> studentService.deleteStudentPhoto(studentId);

            assertThatThrownBy(call).isInstanceOf(StudentNotFoundException.class);
            verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
        }

        @Test
        void when_photo_is_empty_expect_throw_not_found_exception() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final Student student = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García López").build();

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(student));

            final ThrowingCallable call = () -> studentService.deleteStudentPhoto(studentId);

            assertThatThrownBy(call).isInstanceOf(StudentPhotoNotFoundException.class);
            verify(studentRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
        }

        @Test
        void when_file_delete_throws_io_exception_expect_throw_delete_exception() throws Exception {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final String photoFileName = "1.jpg";
            final Student student = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García López").photo(photoFileName).build();

            final Path photoPath = tempDir.resolve(photoFileName);
            java.nio.file.Files.write(photoPath, new byte[]{1, 2, 3});
            photoPath.toFile().setReadOnly();

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(student));

            final ThrowingCallable call = () -> studentService.deleteStudentPhoto(studentId);
            final Throwable thrown = catchThrowable(call);
            photoPath.toFile().setWritable(true);
            if (thrown != null) {
                assertThat(thrown).isInstanceOf(StudentPhotoDeleteException.class)
                        .hasMessage("[Code: 1000, CodeDescription: GENERIC_ERROR, ErrorDescription: Error deleting student photo.]");
            }
        }

        @Test
        void when_photo_field_is_null_expect_throw_not_found_exception() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final Student student = Student.builder()
                    .id(studentId).teacherId(teacherId).name("Juan").surnames("García López").photo(null).build();

            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(student));

            final ThrowingCallable call = () -> studentService.deleteStudentPhoto(studentId);
            final StudentPhotoNotFoundException exception = catchThrowableOfType(call, StudentPhotoNotFoundException.class);

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("[Code: 1003, CodeDescription: RESOURCE_NOT_FOUND, ErrorDescription: Student photo not found.]");
        }
    }
}
