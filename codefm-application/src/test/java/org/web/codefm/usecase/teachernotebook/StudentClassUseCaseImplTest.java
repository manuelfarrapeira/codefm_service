package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.StudentClassService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentClassUseCaseImplTest {

    private StudentClassUseCaseImpl studentClassUseCase;

    @Mock
    private StudentClassService studentClassService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    @BeforeEach
    void beforeEach() {
        studentClassUseCase = new StudentClassUseCaseImpl(studentClassService, cascadeSoftDeleteService);
    }

    @Nested
    class AddStudentToClass {

        @Test
        void when_adding_student_expect_delegated_to_service() {
            final Integer classId = 10;
            final Integer studentId = 20;

            studentClassUseCase.addStudentToClass(classId, studentId);

            verify(studentClassService).addStudentToClass(classId, studentId);
        }
    }

    @Nested
    class RemoveStudentFromClass {

        @Test
        void when_removing_student_expect_cascade_before_service() {
            final Integer classId = 10;
            final Integer studentId = 20;
            final Integer studentClassId = 1;
            final StudentClass association = StudentClass.builder()
                    .id(studentClassId).classId(classId).studentId(studentId).build();
            when(studentClassService.findActiveAssociation(classId, studentId)).thenReturn(association);
            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfStudentClass(studentClassId);
            doNothing().when(studentClassService).removeStudentFromClass(classId, studentId);

            studentClassUseCase.removeStudentFromClass(classId, studentId);

            final var order = inOrder(cascadeSoftDeleteService, studentClassService);
            order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfStudentClass(studentClassId);
            order.verify(studentClassService).removeStudentFromClass(classId, studentId);
        }

        @Test
        void when_removing_student_expect_find_active_association_called_first() {
            final Integer classId = 10;
            final Integer studentId = 20;
            final StudentClass association = StudentClass.builder()
                    .id(1).classId(classId).studentId(studentId).build();
            when(studentClassService.findActiveAssociation(classId, studentId)).thenReturn(association);

            studentClassUseCase.removeStudentFromClass(classId, studentId);

            verify(studentClassService).findActiveAssociation(classId, studentId);
            verify(studentClassService).removeStudentFromClass(classId, studentId);
        }
    }
}
