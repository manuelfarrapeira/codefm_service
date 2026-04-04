package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.GroupAssignmentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentUseCaseImplTest {

    private static final Integer CLASS_ID = 1;
    private static final Integer ASSIGNMENT_ID = 100;
    private static final Integer GROUP_ID = 50;

    @Mock
    private GroupAssignmentService groupAssignmentService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    @InjectMocks
    private GroupAssignmentUseCaseImpl groupAssignmentUseCase;

    @Test
    void getAssignmentsByClassId_shouldDelegateToService() {
        final List<GroupAssignment> expected = List.of(
                GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Project 1").build());
        when(this.groupAssignmentService.getAssignmentsByClassId(CLASS_ID)).thenReturn(expected);

        final List<GroupAssignment> result = this.groupAssignmentUseCase.getAssignmentsByClassId(CLASS_ID);

        assertEquals(expected, result);
        verify(this.groupAssignmentService).getAssignmentsByClassId(CLASS_ID);
    }

    @Test
    void createAssignment_shouldDelegateToService() {
        final GroupAssignment input = GroupAssignment.builder().title("Project 1").quarter(1).build();
        final GroupAssignment created = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Project 1").quarter(1).build();
        when(this.groupAssignmentService.createAssignment(CLASS_ID, input)).thenReturn(created);

        final GroupAssignment result = this.groupAssignmentUseCase.createAssignment(CLASS_ID, input);

        assertEquals(created, result);
        verify(this.groupAssignmentService).createAssignment(CLASS_ID, input);
    }

    @Test
    void updateAssignment_shouldDelegateToService() {
        final GroupAssignment input = GroupAssignment.builder().title("Updated").quarter(2).build();
        final GroupAssignment updated = GroupAssignment.builder().id(ASSIGNMENT_ID).title("Updated").quarter(2).build();
        when(this.groupAssignmentService.updateAssignment(ASSIGNMENT_ID, input)).thenReturn(updated);

        final GroupAssignment result = this.groupAssignmentUseCase.updateAssignment(ASSIGNMENT_ID, input);

        assertEquals(updated, result);
        verify(this.groupAssignmentService).updateAssignment(ASSIGNMENT_ID, input);
    }

    @Test
    void softDeleteAssignment_shouldCallCascadeBeforeService() {
        doNothing().when(this.cascadeSoftDeleteService).cascadeDeleteChildrenOfGroupAssignment(ASSIGNMENT_ID);
        doNothing().when(this.groupAssignmentService).softDeleteAssignment(ASSIGNMENT_ID);

        this.groupAssignmentUseCase.softDeleteAssignment(ASSIGNMENT_ID);

        final var order = inOrder(this.cascadeSoftDeleteService, this.groupAssignmentService);
        order.verify(this.cascadeSoftDeleteService).cascadeDeleteChildrenOfGroupAssignment(ASSIGNMENT_ID);
        order.verify(this.groupAssignmentService).softDeleteAssignment(ASSIGNMENT_ID);
    }

    @Test
    void getGradesByAssignmentId_shouldDelegateToService() {
        final List<GroupAssignmentGrade> expected = List.of(
                GroupAssignmentGrade.builder().id(1).groupAssignmentId(ASSIGNMENT_ID).grade(8.5).build());
        when(this.groupAssignmentService.getGradesByAssignmentId(ASSIGNMENT_ID)).thenReturn(expected);

        final List<GroupAssignmentGrade> result = this.groupAssignmentUseCase.getGradesByAssignmentId(ASSIGNMENT_ID);

        assertEquals(expected, result);
        verify(this.groupAssignmentService).getGradesByAssignmentId(ASSIGNMENT_ID);
    }

    @Test
    void createOrUpdateGrade_shouldDelegateToService() {
        final GroupAssignmentGrade expected = GroupAssignmentGrade.builder().id(1).grade(9.0).build();
        when(this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 9.0)).thenReturn(expected);

        final GroupAssignmentGrade result = this.groupAssignmentUseCase.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 9.0);

        assertEquals(expected, result);
        verify(this.groupAssignmentService).createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 9.0);
    }

    @Test
    void deleteGrade_shouldDelegateToService() {
        doNothing().when(this.groupAssignmentService).deleteGrade(ASSIGNMENT_ID, GROUP_ID);

        this.groupAssignmentUseCase.deleteGrade(ASSIGNMENT_ID, GROUP_ID);

        verify(this.groupAssignmentService).deleteGrade(ASSIGNMENT_ID, GROUP_ID);
    }
}

