package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.StudentClassService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentClassUseCaseImplTest {

    @Mock
    private StudentClassService studentClassService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

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
    void removeStudentFromClass_shouldCallCascadeBeforeService() {
        // Given
        Integer classId = 10;
        Integer studentId = 20;
        Integer studentClassId = 1;
        StudentClass association = StudentClass.builder()
                .id(studentClassId).classId(classId).studentId(studentId).build();

        when(studentClassService.findActiveAssociation(classId, studentId)).thenReturn(association);
        doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfStudentClass(studentClassId);
        doNothing().when(studentClassService).removeStudentFromClass(classId, studentId);

        // When
        studentClassUseCase.removeStudentFromClass(classId, studentId);

        // Then
        var order = inOrder(cascadeSoftDeleteService, studentClassService);
        order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfStudentClass(studentClassId);
        order.verify(studentClassService).removeStudentFromClass(classId, studentId);
    }

    @Test
    void removeStudentFromClass_shouldCallFindActiveAssociationFirst() {
        // Given
        Integer classId = 10;
        Integer studentId = 20;
        StudentClass association = StudentClass.builder()
                .id(1).classId(classId).studentId(studentId).build();

        when(studentClassService.findActiveAssociation(classId, studentId)).thenReturn(association);

        // When
        studentClassUseCase.removeStudentFromClass(classId, studentId);

        // Then
        verify(studentClassService, times(1)).findActiveAssociation(classId, studentId);
        verify(studentClassService, times(1)).removeStudentFromClass(classId, studentId);
    }
}
