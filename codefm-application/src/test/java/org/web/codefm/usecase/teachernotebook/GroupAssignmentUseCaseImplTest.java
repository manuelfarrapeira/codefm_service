package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.GroupAssignmentService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentUseCaseImplTest {

    private GroupAssignmentUseCaseImpl groupAssignmentUseCase;

    @Mock
    private GroupAssignmentService groupAssignmentService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    private static final Integer CLASS_ID = 1;
    private static final Integer ASSIGNMENT_ID = 100;
    private static final Integer GROUP_ID = 50;

    @BeforeEach
    void beforeEach() {
        groupAssignmentUseCase = new GroupAssignmentUseCaseImpl(groupAssignmentService, cascadeSoftDeleteService);
    }

    @Nested
    class GetAssignmentsByClassId {

        @Test
        void when_assignments_found_expect_delegated_to_service() {
            final List<GroupAssignment> expected = List.of(
                    GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Project 1").build());
            when(groupAssignmentService.getAssignmentsByClassId(CLASS_ID)).thenReturn(expected);

            final List<GroupAssignment> result = groupAssignmentUseCase.getAssignmentsByClassId(CLASS_ID);

            assertThat(result).isEqualTo(expected);
            verify(groupAssignmentService).getAssignmentsByClassId(CLASS_ID);
        }
    }

    @Nested
    class CreateAssignment {

        @Test
        void when_creating_assignment_expect_delegated_to_service() {
            final GroupAssignment input = GroupAssignment.builder().title("Project 1").quarter(1).build();
            final GroupAssignment created = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Project 1").quarter(1).build();
            when(groupAssignmentService.createAssignment(CLASS_ID, input)).thenReturn(created);

            final GroupAssignment result = groupAssignmentUseCase.createAssignment(CLASS_ID, input);

            assertThat(result).isEqualTo(created);
            verify(groupAssignmentService).createAssignment(CLASS_ID, input);
        }
    }

    @Nested
    class UpdateAssignment {

        @Test
        void when_updating_assignment_expect_delegated_to_service() {
            final GroupAssignment input = GroupAssignment.builder().title("Updated").quarter(2).build();
            final GroupAssignment updated = GroupAssignment.builder().id(ASSIGNMENT_ID).title("Updated").quarter(2).build();
            when(groupAssignmentService.updateAssignment(ASSIGNMENT_ID, input)).thenReturn(updated);

            final GroupAssignment result = groupAssignmentUseCase.updateAssignment(ASSIGNMENT_ID, input);

            assertThat(result).isEqualTo(updated);
            verify(groupAssignmentService).updateAssignment(ASSIGNMENT_ID, input);
        }
    }

    @Nested
    class SoftDeleteAssignment {

        @Test
        void when_deleting_assignment_expect_cascade_before_service() {
            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfGroupAssignment(ASSIGNMENT_ID);
            doNothing().when(groupAssignmentService).softDeleteAssignment(ASSIGNMENT_ID);

            groupAssignmentUseCase.softDeleteAssignment(ASSIGNMENT_ID);

            final var order = inOrder(cascadeSoftDeleteService, groupAssignmentService);
            order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfGroupAssignment(ASSIGNMENT_ID);
            order.verify(groupAssignmentService).softDeleteAssignment(ASSIGNMENT_ID);
        }
    }

    @Nested
    class GetGradesByAssignmentId {

        @Test
        void when_grades_found_expect_delegated_to_service() {
            final List<GroupAssignmentGrade> expected = List.of(
                    GroupAssignmentGrade.builder().id(1).groupAssignmentId(ASSIGNMENT_ID).grade(8.5).build());
            when(groupAssignmentService.getGradesByAssignmentId(ASSIGNMENT_ID)).thenReturn(expected);

            final List<GroupAssignmentGrade> result = groupAssignmentUseCase.getGradesByAssignmentId(ASSIGNMENT_ID);

            assertThat(result).isEqualTo(expected);
            verify(groupAssignmentService).getGradesByAssignmentId(ASSIGNMENT_ID);
        }
    }

    @Nested
    class CreateOrUpdateGrade {

        @Test
        void when_creating_or_updating_grade_expect_delegated_to_service() {
            final GroupAssignmentGrade expected = GroupAssignmentGrade.builder().id(1).grade(9.0).build();
            when(groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 9.0)).thenReturn(expected);

            final GroupAssignmentGrade result = groupAssignmentUseCase.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 9.0);

            assertThat(result).isEqualTo(expected);
            verify(groupAssignmentService).createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 9.0);
        }
    }

    @Nested
    class DeleteGrade {

        @Test
        void when_deleting_grade_expect_delegated_to_service() {
            doNothing().when(groupAssignmentService).deleteGrade(ASSIGNMENT_ID, GROUP_ID);

            groupAssignmentUseCase.deleteGrade(ASSIGNMENT_ID, GROUP_ID);

            verify(groupAssignmentService).deleteGrade(ASSIGNMENT_ID, GROUP_ID);
        }
    }
}
