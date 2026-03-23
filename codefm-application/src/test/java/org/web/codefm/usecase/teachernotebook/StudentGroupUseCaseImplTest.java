package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.service.teachernotebook.StudentGroupService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentGroupUseCaseImplTest {

    @Mock
    private StudentGroupService studentGroupService;

    @InjectMocks
    private StudentGroupUseCaseImpl studentGroupUseCase;

    @Test
    void generateGroups_shouldDelegateToService() {
        final Integer classId = 1;
        final Boolean prioritizeShapeDiversity = true;
        final List<List<Integer>> expectedGroups = List.of(
                List.of(1, 2, 3, 4),
                List.of(5, 6, 7, 8)
        );

        when(this.studentGroupService.generateGroups(classId, prioritizeShapeDiversity)).thenReturn(expectedGroups);

        final List<List<Integer>> result = this.studentGroupUseCase.generateGroups(classId, prioritizeShapeDiversity);

        assertEquals(expectedGroups, result);
        verify(this.studentGroupService).generateGroups(classId, prioritizeShapeDiversity);
    }

    @Test
    void generateGroups_shouldDelegateToServiceWithFalsePriority() {
        final Integer classId = 1;
        final Boolean prioritizeShapeDiversity = false;
        final List<List<Integer>> expectedGroups = List.of(
                List.of(1, 2, 3, 4),
                List.of(5, 6, 7, 8)
        );

        when(this.studentGroupService.generateGroups(classId, prioritizeShapeDiversity)).thenReturn(expectedGroups);

        final List<List<Integer>> result = this.studentGroupUseCase.generateGroups(classId, prioritizeShapeDiversity);

        assertEquals(expectedGroups, result);
        verify(this.studentGroupService).generateGroups(classId, prioritizeShapeDiversity);
    }
}
