package org.web.codefm.application.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.service.teachernotebook.StudentService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentUseCaseImplTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentUseCaseImpl studentUseCase;


    @Test
    void createStudent_shouldCallServiceAndReturnStudent() {
        Student studentToCreate = Student.builder()
                .name("Juan")
                .surnames("García López")
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .build();

        when(studentService.createStudent(studentToCreate)).thenReturn(studentToCreate);

        Student result = studentUseCase.createStudent(studentToCreate);

        assertNotNull(result);
        assertEquals("Juan", result.getName());
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

}

