package org.web.codefm.application.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.service.teachernotebook.StudentClassService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentClassUseCaseImplTest {

    @Mock
    private StudentClassService studentClassService;

    @InjectMocks
    private StudentClassUseCaseImpl studentClassUseCase;

    @Test
    void addStudentToClass_shouldCallService_withCorrectParameters() {
        // Given
        Integer classId = 10;
        Integer studentId = 20;

        // When
        studentClassUseCase.addStudentToClass(classId, studentId);

        // Then
        verify(studentClassService, times(1)).addStudentToClass(classId, studentId);
    }

    @Test
    void removeStudentFromClass_shouldCallService_withCorrectParameters() {
        // Given
        Integer classId = 10;
        Integer studentId = 20;

        // When
        studentClassUseCase.removeStudentFromClass(classId, studentId);

        // Then
        verify(studentClassService, times(1)).removeStudentFromClass(classId, studentId);
    }
}

