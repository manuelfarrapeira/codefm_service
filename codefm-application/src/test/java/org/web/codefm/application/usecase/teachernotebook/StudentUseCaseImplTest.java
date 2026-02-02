package org.web.codefm.application.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.service.teachernotebook.StudentService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentUseCaseImplTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentUseCaseImpl studentUseCase;

    private static final Integer TEACHER_ID = 1;


    @Test
    void createStudent_shouldCallServiceAndReturnStudent() {
        Student studentToCreate = Student.builder()
                .name("Juan")
                .surnames("García López")
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .build();

        Student createdStudent = Student.builder()
                .name("Juan")
                .surnames("García López")
                .teacherId(TEACHER_ID)
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .build();

        when(studentService.createStudent(studentToCreate)).thenReturn(createdStudent);

        Student result = studentUseCase.createStudent(studentToCreate);

        assertNotNull(result);
        assertEquals("Juan", result.getName());
        assertEquals(TEACHER_ID, result.getTeacherId());
        verify(studentService, times(1)).createStudent(studentToCreate);
    }

    @Test
    void updateStudent_shouldCallServiceAndReturnUpdatedStudent() {
        Integer studentId = 1;
        Student studentToUpdate = Student.builder()
                .name("Juan Carlos")
                .surnames("García López")
                .build();

        when(studentService.updateStudent(studentId, studentToUpdate)).thenReturn(studentToUpdate);

        Student result = studentUseCase.updateStudent(studentId, studentToUpdate);

        assertNotNull(result);
        assertEquals("Juan Carlos", result.getName());
        verify(studentService, times(1)).updateStudent(studentId, studentToUpdate);
    }

    @Test
    void softDeleteStudent_shouldCallService() {
        Integer studentId = 1;

        doNothing().when(studentService).softDeleteStudent(studentId);

        studentUseCase.softDeleteStudent(studentId);

        verify(studentService, times(1)).softDeleteStudent(studentId);
    }

    @Test
    void uploadStudentPhoto_shouldCallServiceAndReturnPhotoPath() {
        Integer studentId = 1;
        MultipartFile mockFile = mock(MultipartFile.class);
        String expectedPhotoPath = "data/student-photos/1.jpg";

        when(studentService.saveStudentPhoto(studentId, mockFile)).thenReturn(expectedPhotoPath);

        String result = studentUseCase.uploadStudentPhoto(studentId, mockFile);

        assertNotNull(result);
        assertEquals(expectedPhotoPath, result);
        verify(studentService, times(1)).saveStudentPhoto(studentId, mockFile);
    }

    @Test
    void searchStudents_shouldCallServiceAndReturnStudents_whenSearchingById() {
        Integer studentId = 1;
        Student student1 = Student.builder()
                .id(1)
                .teacherId(TEACHER_ID)
                .name("Juan")
                .surnames("García López")
                .build();
        List<Student> expectedStudents = Arrays.asList(student1);

        when(studentService.searchStudents(studentId, null, null)).thenReturn(expectedStudents);

        List<Student> result = studentUseCase.searchStudents(studentId, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
        verify(studentService, times(1)).searchStudents(studentId, null, null);
    }

    @Test
    void searchStudents_shouldCallServiceAndReturnStudents_whenSearchingByName() {
        String name = "Juan";
        Student student1 = Student.builder()
                .id(1)
                .teacherId(TEACHER_ID)
                .name("Juan")
                .surnames("García López")
                .build();
        Student student2 = Student.builder()
                .id(2)
                .teacherId(TEACHER_ID)
                .name("Juan Carlos")
                .surnames("Pérez Martín")
                .build();
        List<Student> expectedStudents = Arrays.asList(student1, student2);

        when(studentService.searchStudents(null, name, null)).thenReturn(expectedStudents);

        List<Student> result = studentUseCase.searchStudents(null, name, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(studentService, times(1)).searchStudents(null, name, null);
    }

    @Test
    void searchStudents_shouldCallServiceAndReturnStudents_whenSearchingBySurnames() {
        String surnames = "García";
        Student student1 = Student.builder()
                .id(1)
                .teacherId(TEACHER_ID)
                .name("Juan")
                .surnames("García López")
                .build();
        List<Student> expectedStudents = Arrays.asList(student1);

        when(studentService.searchStudents(null, null, surnames)).thenReturn(expectedStudents);

        List<Student> result = studentUseCase.searchStudents(null, null, surnames);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("García López", result.get(0).getSurnames());
        verify(studentService, times(1)).searchStudents(null, null, surnames);
    }

    @Test
    void searchStudents_shouldCallServiceAndReturnStudents_whenSearchingByMultipleFilters() {
        String name = "Juan";
        String surnames = "García";
        Student student1 = Student.builder()
                .id(1)
                .teacherId(TEACHER_ID)
                .name("Juan")
                .surnames("García López")
                .build();
        List<Student> expectedStudents = Arrays.asList(student1);

        when(studentService.searchStudents(null, name, surnames)).thenReturn(expectedStudents);

        List<Student> result = studentUseCase.searchStudents(null, name, surnames);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
        assertEquals("García López", result.get(0).getSurnames());
        verify(studentService, times(1)).searchStudents(null, name, surnames);
    }

}

