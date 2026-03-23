package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroupMember;
import org.web.codefm.domain.service.teachernotebook.SavedStudentGroupService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedStudentGroupUseCaseImplTest {

    @Mock
    private SavedStudentGroupService savedStudentGroupService;

    @InjectMocks
    private SavedStudentGroupUseCaseImpl savedStudentGroupUseCase;

    @Test
    void getSavedGroupsByClassId_shouldDelegateToService() {
        final Integer classId = 1;
        final List<SavedStudentGroup> expected = List.of(
                SavedStudentGroup.builder().id(1).classId(classId).name("Group A").build());

        when(this.savedStudentGroupService.getSavedGroupsByClassId(classId)).thenReturn(expected);

        final List<SavedStudentGroup> result = this.savedStudentGroupUseCase.getSavedGroupsByClassId(classId);

        assertEquals(expected, result);
        verify(this.savedStudentGroupService).getSavedGroupsByClassId(classId);
    }

    @Test
    void createSavedGroups_shouldDelegateToService() {
        final Integer classId = 1;
        final List<SavedStudentGroup> groups = List.of(
                SavedStudentGroup.builder().name("Group A")
                        .members(List.of(SavedStudentGroupMember.builder().studentId(10).build()))
                        .build());
        final List<SavedStudentGroup> expected = List.of(
                SavedStudentGroup.builder().id(1).classId(classId).name("Group A").build());

        when(this.savedStudentGroupService.createSavedGroups(classId, groups)).thenReturn(expected);

        final List<SavedStudentGroup> result = this.savedStudentGroupUseCase.createSavedGroups(classId, groups);

        assertEquals(expected, result);
        verify(this.savedStudentGroupService).createSavedGroups(classId, groups);
    }

    @Test
    void updateAllSavedGroups_shouldDelegateToService() {
        final Integer classId = 1;
        final List<SavedStudentGroup> groups = List.of(
                SavedStudentGroup.builder().name("Group A")
                        .members(List.of(SavedStudentGroupMember.builder().studentId(10).build()))
                        .build(),
                SavedStudentGroup.builder().name("Group B")
                        .members(List.of(SavedStudentGroupMember.builder().studentId(11).build()))
                        .build());
        final List<SavedStudentGroup> expected = List.of(
                SavedStudentGroup.builder().id(1).classId(classId).name("Group A").build(),
                SavedStudentGroup.builder().id(2).classId(classId).name("Group B").build());

        when(this.savedStudentGroupService.updateAllSavedGroups(classId, groups)).thenReturn(expected);

        final List<SavedStudentGroup> result = this.savedStudentGroupUseCase.updateAllSavedGroups(classId, groups);

        assertEquals(expected, result);
        verify(this.savedStudentGroupService).updateAllSavedGroups(classId, groups);
    }


    @Test
    void softDeleteSavedGroupsByClassId_shouldDelegateToService() {
        final Integer classId = 1;

        this.savedStudentGroupUseCase.softDeleteSavedGroupsByClassId(classId);

        verify(this.savedStudentGroupService).softDeleteSavedGroupsByClassId(classId);
    }
}
