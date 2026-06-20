package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroupMember;
import org.web.codefm.domain.service.teachernotebook.SavedStudentGroupService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedStudentGroupUseCaseImplTest {

    private SavedStudentGroupUseCaseImpl savedStudentGroupUseCase;

    @Mock
    private SavedStudentGroupService savedStudentGroupService;

    @BeforeEach
    void beforeEach() {
        savedStudentGroupUseCase = new SavedStudentGroupUseCaseImpl(savedStudentGroupService);
    }

    @Nested
    class GetSavedGroupsByClassId {

        @Test
        void when_groups_found_expect_delegated_to_service() {
            final Integer classId = 1;
            final List<SavedStudentGroup> expected = List.of(
                    SavedStudentGroup.builder().id(1).classId(classId).name("Group A").build());
            when(savedStudentGroupService.getSavedGroupsByClassId(classId)).thenReturn(expected);

            final List<SavedStudentGroup> result = savedStudentGroupUseCase.getSavedGroupsByClassId(classId);

            assertThat(result).isEqualTo(expected);
            verify(savedStudentGroupService).getSavedGroupsByClassId(classId);
        }
    }

    @Nested
    class CreateSavedGroups {

        @Test
        void when_creating_groups_expect_delegated_to_service() {
            final Integer classId = 1;
            final List<SavedStudentGroup> groups = List.of(
                    SavedStudentGroup.builder().name("Group A")
                            .members(List.of(SavedStudentGroupMember.builder().studentId(10).build()))
                            .build());
            final List<SavedStudentGroup> expected = List.of(
                    SavedStudentGroup.builder().id(1).classId(classId).name("Group A").build());
            when(savedStudentGroupService.createSavedGroups(classId, groups)).thenReturn(expected);

            final List<SavedStudentGroup> result = savedStudentGroupUseCase.createSavedGroups(classId, groups);

            assertThat(result).isEqualTo(expected);
            verify(savedStudentGroupService).createSavedGroups(classId, groups);
        }
    }

    @Nested
    class UpdateAllSavedGroups {

        @Test
        void when_updating_groups_expect_delegated_to_service() {
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
            when(savedStudentGroupService.updateAllSavedGroups(classId, groups)).thenReturn(expected);

            final List<SavedStudentGroup> result = savedStudentGroupUseCase.updateAllSavedGroups(classId, groups);

            assertThat(result).isEqualTo(expected);
            verify(savedStudentGroupService).updateAllSavedGroups(classId, groups);
        }
    }

    @Nested
    class SoftDeleteSavedGroupsByClassId {

        @Test
        void when_deleting_groups_expect_delegated_to_service() {
            final Integer classId = 1;

            savedStudentGroupUseCase.softDeleteSavedGroupsByClassId(classId);

            verify(savedStudentGroupService).softDeleteSavedGroupsByClassId(classId);
        }
    }
}
