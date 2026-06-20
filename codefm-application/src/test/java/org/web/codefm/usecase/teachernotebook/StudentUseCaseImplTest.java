package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.StudentService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentUseCaseImplTest {

    private StudentUseCaseImpl studentUseCase;

    @Mock
    private StudentService studentService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    private static final Integer TEACHER_ID = 1;

    @BeforeEach
    void beforeEach() {
        studentUseCase = new StudentUseCaseImpl(studentService, cascadeSoftDeleteService);
    }

    @Nested
    class CreateStudent {

        @Test
        void when_creating_student_expect_delegated_to_service() {
            final Student studentToCreate = Student.builder()
                    .name("Juan").surnames("García López").dateOfBirth(LocalDate.of(2010, 3, 15)).build();
            final Student createdStudent = Student.builder()
                    .name("Juan").surnames("García López").teacherId(TEACHER_ID).dateOfBirth(LocalDate.of(2010, 3, 15)).build();
            when(studentService.createStudent(studentToCreate)).thenReturn(createdStudent);

            final Student result = studentUseCase.createStudent(studentToCreate);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Juan");
            assertThat(result.getTeacherId()).isEqualTo(TEACHER_ID);
            verify(studentService).createStudent(studentToCreate);
        }
    }

    @Nested
    class UpdateStudent {

        @Test
        void when_updating_student_expect_delegated_to_service() {
            final Integer studentId = 1;
            final Student studentToUpdate = Student.builder().name("Juan Carlos").surnames("García López").build();
            when(studentService.updateStudent(studentId, studentToUpdate)).thenReturn(studentToUpdate);

            final Student result = studentUseCase.updateStudent(studentId, studentToUpdate);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Juan Carlos");
            verify(studentService).updateStudent(studentId, studentToUpdate);
        }
    }

    @Nested
    class SoftDeleteStudent {

        @Test
        void when_deleting_student_expect_cascade_before_service() {
            final Integer studentId = 1;
            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfStudent(studentId);
            doNothing().when(studentService).softDeleteStudent(studentId);

            studentUseCase.softDeleteStudent(studentId);

            final var order = inOrder(cascadeSoftDeleteService, studentService);
            order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfStudent(studentId);
            order.verify(studentService).softDeleteStudent(studentId);
        }
    }

    @Nested
    class UploadStudentPhoto {

        @Test
        void when_uploading_photo_expect_delegated_to_service() {
            final Integer studentId = 1;
            final MultipartFile mockFile = mock(MultipartFile.class);
            final String expectedPhotoPath = "data/student-photos/1.jpg";
            when(studentService.saveStudentPhoto(studentId, mockFile)).thenReturn(expectedPhotoPath);

            final String result = studentUseCase.uploadStudentPhoto(studentId, mockFile);

            assertThat(result).isEqualTo(expectedPhotoPath);
            verify(studentService).saveStudentPhoto(studentId, mockFile);
        }
    }

    @Nested
    class SearchStudents {

        @Test
        void when_searching_by_id_expect_matching_students_returned() {
            final Integer studentId = 1;
            final List<Student> expected = List.of(Student.builder().id(1).teacherId(TEACHER_ID).name("Juan").surnames("García López").build());
            when(studentService.searchStudents(studentId, null, null)).thenReturn(expected);

            final List<Student> result = studentUseCase.searchStudents(studentId, null, null);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Juan");
            verify(studentService).searchStudents(studentId, null, null);
        }

        @Test
        void when_searching_by_name_expect_matching_students_returned() {
            final String name = "Juan";
            final List<Student> expected = List.of(
                    Student.builder().id(1).teacherId(TEACHER_ID).name("Juan").surnames("García López").build(),
                    Student.builder().id(2).teacherId(TEACHER_ID).name("Juan Carlos").surnames("Pérez Martín").build());
            when(studentService.searchStudents(null, name, null)).thenReturn(expected);

            final List<Student> result = studentUseCase.searchStudents(null, name, null);

            assertThat(result).isNotNull().hasSize(2);
            verify(studentService).searchStudents(null, name, null);
        }

        @Test
        void when_searching_by_surnames_expect_matching_students_returned() {
            final String surnames = "García";
            final List<Student> expected = List.of(Student.builder().id(1).teacherId(TEACHER_ID).name("Juan").surnames("García López").build());
            when(studentService.searchStudents(null, null, surnames)).thenReturn(expected);

            final List<Student> result = studentUseCase.searchStudents(null, null, surnames);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getSurnames()).isEqualTo("García López");
            verify(studentService).searchStudents(null, null, surnames);
        }

        @Test
        void when_searching_by_multiple_filters_expect_matching_students_returned() {
            final String name = "Juan";
            final String surnames = "García";
            final List<Student> expected = List.of(Student.builder().id(1).teacherId(TEACHER_ID).name("Juan").surnames("García López").build());
            when(studentService.searchStudents(null, name, surnames)).thenReturn(expected);

            final List<Student> result = studentUseCase.searchStudents(null, name, surnames);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Juan");
            verify(studentService).searchStudents(null, name, surnames);
        }
    }

    @Nested
    class GetAllStudents {

        @Test
        void when_fetching_all_students_expect_delegated_to_service() {
            final List<Student> expected = List.of(
                    Student.builder().id(1).teacherId(TEACHER_ID).name("Juan").surnames("García López").build(),
                    Student.builder().id(2).teacherId(TEACHER_ID).name("María").surnames("Pérez Martínez").build());
            when(studentService.getAllStudents()).thenReturn(expected);

            final List<Student> result = studentUseCase.getAllStudents();

            assertThat(result).isNotNull().hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Juan");
            assertThat(result.get(1).getName()).isEqualTo("María");
            verify(studentService).getAllStudents();
        }
    }

    @Nested
    class DownloadStudentPhoto {

        @Test
        void when_downloading_photo_expect_delegated_to_service() {
            final Integer studentId = 1;
            final byte[] expectedPhotoBytes = new byte[]{1, 2, 3, 4, 5};
            when(studentService.getStudentPhoto(studentId)).thenReturn(expectedPhotoBytes);

            final byte[] result = studentUseCase.downloadStudentPhoto(studentId);

            assertThat(result).isNotNull();
            assertThat(result.length).isEqualTo(expectedPhotoBytes.length);
            verify(studentService).getStudentPhoto(studentId);
        }
    }

    @Nested
    class DeleteStudentPhoto {

        @Test
        void when_deleting_photo_expect_delegated_to_service() {
            final Integer studentId = 1;
            doNothing().when(studentService).deleteStudentPhoto(studentId);

            studentUseCase.deleteStudentPhoto(studentId);

            verify(studentService).deleteStudentPhoto(studentId);
        }
    }
}
