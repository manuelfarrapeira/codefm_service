package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroupMember;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentGroupEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentGroupMemberEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentGroupJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentGroupMemberJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentJPARepository;
import org.web.codefm.infrastructure.mapper.SavedStudentGroupMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedStudentGroupRepositoryImplTest {

    private SavedStudentGroupRepositoryImpl savedStudentGroupRepository;

    private static final Integer CLASS_ID = 1;
    private static final Integer GROUP_ID_1 = 10;
    private static final Integer GROUP_ID_2 = 20;
    private static final Integer TEACHER_ID = 100;
    private static final Integer STUDENT_ID_1 = 50;
    private static final Integer STUDENT_ID_2 = 51;

    @Mock
    private StudentGroupJPARepository studentGroupJPARepository;

    @Mock
    private StudentGroupMemberJPARepository studentGroupMemberJPARepository;

    @Mock
    private StudentJPARepository studentJPARepository;

    @Mock
    private SavedStudentGroupMapper savedStudentGroupMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        this.savedStudentGroupRepository = new SavedStudentGroupRepositoryImpl(
                this.studentGroupJPARepository,
                this.studentGroupMemberJPARepository,
                this.studentJPARepository,
                this.savedStudentGroupMapper,
                this.jdbcTemplate);
    }

    @Nested
    class FindByClassIdWithMembers {

        @Test
        void when_groups_and_members_exist_expect_groups_with_student_names_returned() {
            final StudentGroupEntity groupEntity = new StudentGroupEntity(GROUP_ID_1, CLASS_ID, "Group A", null);
            final StudentGroupMemberEntity memberEntity = new StudentGroupMemberEntity(1, GROUP_ID_1, STUDENT_ID_1);
            final StudentEntity studentEntity = new StudentEntity();
            studentEntity.setId(STUDENT_ID_1);
            studentEntity.setName("Juan");
            studentEntity.setSurnames("García");
            final SavedStudentGroup mappedGroup = SavedStudentGroup.builder().id(GROUP_ID_1).classId(CLASS_ID).name("Group A").build();

            when(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(List.of(groupEntity));
            when(SavedStudentGroupRepositoryImplTest.this.studentGroupMemberJPARepository.findByStudentGroupIdIn(List.of(GROUP_ID_1))).thenReturn(List.of(memberEntity));
            when(SavedStudentGroupRepositoryImplTest.this.studentJPARepository.findAllById(any())).thenReturn(List.of(studentEntity));
            when(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper.toModel(groupEntity)).thenReturn(mappedGroup);

            final List<SavedStudentGroup> result = SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.findByClassIdWithMembers(CLASS_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Group A");
            assertThat(result.get(0).getMembers()).hasSize(1);
            assertThat(result.get(0).getMembers().get(0).getStudentId()).isEqualTo(STUDENT_ID_1);
            assertThat(result.get(0).getMembers().get(0).getStudentName()).isEqualTo("Juan");
            assertThat(result.get(0).getMembers().get(0).getStudentSurnames()).isEqualTo("García");
        }

        @Test
        void when_no_groups_exist_expect_empty_list_returned() {
            when(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(Collections.emptyList());

            final List<SavedStudentGroup> result = SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.findByClassIdWithMembers(CLASS_ID);

            assertThat(result).isEmpty();
            verify(SavedStudentGroupRepositoryImplTest.this.studentGroupMemberJPARepository, never()).findByStudentGroupIdIn(any());
            verify(SavedStudentGroupRepositoryImplTest.this.studentJPARepository, never()).findAllById(any());
        }

        @Test
        void when_groups_exist_without_members_expect_groups_with_empty_members_returned() {
            final StudentGroupEntity groupEntity = new StudentGroupEntity(GROUP_ID_1, CLASS_ID, "Group A", null);
            final SavedStudentGroup mappedGroup = SavedStudentGroup.builder().id(GROUP_ID_1).classId(CLASS_ID).name("Group A").build();

            when(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(List.of(groupEntity));
            when(SavedStudentGroupRepositoryImplTest.this.studentGroupMemberJPARepository.findByStudentGroupIdIn(List.of(GROUP_ID_1))).thenReturn(Collections.emptyList());
            when(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper.toModel(groupEntity)).thenReturn(mappedGroup);

            final List<SavedStudentGroup> result = SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.findByClassIdWithMembers(CLASS_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMembers()).isEmpty();
            verify(SavedStudentGroupRepositoryImplTest.this.studentJPARepository, never()).findAllById(any());
        }

        @Test
        void when_multiple_groups_exist_expect_members_assigned_to_correct_group() {
            final StudentGroupEntity groupEntity1 = new StudentGroupEntity(GROUP_ID_1, CLASS_ID, "Group A", null);
            final StudentGroupEntity groupEntity2 = new StudentGroupEntity(GROUP_ID_2, CLASS_ID, "Group B", null);
            final StudentGroupMemberEntity member1 = new StudentGroupMemberEntity(1, GROUP_ID_1, STUDENT_ID_1);
            final StudentGroupMemberEntity member2 = new StudentGroupMemberEntity(2, GROUP_ID_2, STUDENT_ID_2);
            final StudentEntity student1 = new StudentEntity();
            student1.setId(STUDENT_ID_1);
            student1.setName("Juan");
            student1.setSurnames("García");
            final StudentEntity student2 = new StudentEntity();
            student2.setId(STUDENT_ID_2);
            student2.setName("Ana");
            student2.setSurnames("López");
            final SavedStudentGroup mapped1 = SavedStudentGroup.builder().id(GROUP_ID_1).classId(CLASS_ID).name("Group A").build();
            final SavedStudentGroup mapped2 = SavedStudentGroup.builder().id(GROUP_ID_2).classId(CLASS_ID).name("Group B").build();

            when(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(List.of(groupEntity1, groupEntity2));
            when(SavedStudentGroupRepositoryImplTest.this.studentGroupMemberJPARepository.findByStudentGroupIdIn(List.of(GROUP_ID_1, GROUP_ID_2))).thenReturn(List.of(member1, member2));
            when(SavedStudentGroupRepositoryImplTest.this.studentJPARepository.findAllById(any())).thenReturn(List.of(student1, student2));
            when(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper.toModel(groupEntity1)).thenReturn(mapped1);
            when(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper.toModel(groupEntity2)).thenReturn(mapped2);

            final List<SavedStudentGroup> result = SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.findByClassIdWithMembers(CLASS_ID);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getMembers()).hasSize(1);
            assertThat(result.get(0).getMembers().get(0).getStudentId()).isEqualTo(STUDENT_ID_1);
            assertThat(result.get(1).getMembers()).hasSize(1);
            assertThat(result.get(1).getMembers().get(0).getStudentId()).isEqualTo(STUDENT_ID_2);
        }
    }

    @Nested
    class FindByIdAndTeacherId {

        @Test
        void when_group_is_found_expect_group_returned() {
            final StudentGroupEntity entity = new StudentGroupEntity(GROUP_ID_1, CLASS_ID, "Group A", null);
            final SavedStudentGroup expected = SavedStudentGroup.builder().id(GROUP_ID_1).classId(CLASS_ID).name("Group A").build();

            when(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository.findByIdAndTeacherId(GROUP_ID_1, TEACHER_ID)).thenReturn(Optional.of(entity));
            when(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper.toModel(entity)).thenReturn(expected);

            final Optional<SavedStudentGroup> result = SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID_1, TEACHER_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expected);
        }

        @Test
        void when_group_is_not_found_expect_empty_optional_returned() {
            when(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository.findByIdAndTeacherId(GROUP_ID_1, TEACHER_ID)).thenReturn(Optional.empty());

            final Optional<SavedStudentGroup> result = SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID_1, TEACHER_ID);

            assertThat(result).isNotPresent();
            verify(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper, never()).toModel(any(StudentGroupEntity.class));
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_group_expect_saved_group_returned() {
            final SavedStudentGroup groupToSave = SavedStudentGroup.builder().classId(CLASS_ID).name("Group A").build();
            final StudentGroupEntity entity = new StudentGroupEntity(null, CLASS_ID, "Group A", null);
            final StudentGroupEntity savedEntity = new StudentGroupEntity(GROUP_ID_1, CLASS_ID, "Group A", null);
            final SavedStudentGroup expected = SavedStudentGroup.builder().id(GROUP_ID_1).classId(CLASS_ID).name("Group A").build();

            when(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper.toEntity(groupToSave)).thenReturn(entity);
            when(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository.save(entity)).thenReturn(savedEntity);
            when(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper.toModel(savedEntity)).thenReturn(expected);

            final SavedStudentGroup result = SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.save(groupToSave);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(GROUP_ID_1);
            assertThat(result.getName()).isEqualTo("Group A");
        }
    }

    @Nested
    class SaveAll {

        @Test
        void when_valid_groups_expect_saved_groups_returned() {
            final SavedStudentGroup group1 = SavedStudentGroup.builder().classId(CLASS_ID).name("Group A").build();
            final SavedStudentGroup group2 = SavedStudentGroup.builder().classId(CLASS_ID).name("Group B").build();
            final StudentGroupEntity entity1 = new StudentGroupEntity(null, CLASS_ID, "Group A", null);
            final StudentGroupEntity entity2 = new StudentGroupEntity(null, CLASS_ID, "Group B", null);
            final StudentGroupEntity savedEntity1 = new StudentGroupEntity(GROUP_ID_1, CLASS_ID, "Group A", null);
            final StudentGroupEntity savedEntity2 = new StudentGroupEntity(GROUP_ID_2, CLASS_ID, "Group B", null);
            final SavedStudentGroup expected1 = SavedStudentGroup.builder().id(GROUP_ID_1).classId(CLASS_ID).name("Group A").build();
            final SavedStudentGroup expected2 = SavedStudentGroup.builder().id(GROUP_ID_2).classId(CLASS_ID).name("Group B").build();

            when(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper.toEntity(group1)).thenReturn(entity1);
            when(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper.toEntity(group2)).thenReturn(entity2);
            when(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository.saveAll(List.of(entity1, entity2))).thenReturn(List.of(savedEntity1, savedEntity2));
            when(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper.toModel(savedEntity1)).thenReturn(expected1);
            when(SavedStudentGroupRepositoryImplTest.this.savedStudentGroupMapper.toModel(savedEntity2)).thenReturn(expected2);

            final List<SavedStudentGroup> result = SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.saveAll(List.of(group1, group2));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(GROUP_ID_1);
            assertThat(result.get(1).getId()).isEqualTo(GROUP_ID_2);
        }
    }

    @Nested
    class UpdateName {

        @Test
        void when_called_expect_jpa_delegated() {
            SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.updateName(GROUP_ID_1, "New Name");

            verify(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository).updateNameById(GROUP_ID_1, "New Name");
        }
    }

    @Nested
    class SaveMembers {

        @Test
        void when_members_exist_expect_batch_update_executed() {
            final List<SavedStudentGroupMember> members = List.of(
                    SavedStudentGroupMember.builder().studentGroupId(GROUP_ID_1).studentId(STUDENT_ID_1).build(),
                    SavedStudentGroupMember.builder().studentGroupId(GROUP_ID_1).studentId(STUDENT_ID_2).build());

            SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.saveMembers(members);

            verify(SavedStudentGroupRepositoryImplTest.this.jdbcTemplate)
                    .batchUpdate(eq("INSERT INTO student_group_members (student_group_id, student_id) VALUES (?, ?)"), any(List.class));
        }
    }

    @Nested
    class SoftDeleteById {

        @Test
        void when_called_expect_jpa_delegated() {
            SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.softDeleteById(GROUP_ID_1);

            verify(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository).softDeleteById(GROUP_ID_1);
        }
    }

    @Nested
    class HardDeleteMembersByGroupId {

        @Test
        void when_called_expect_member_repository_delegated() {
            SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.hardDeleteMembersByGroupId(GROUP_ID_1);

            verify(SavedStudentGroupRepositoryImplTest.this.studentGroupMemberJPARepository).deleteByStudentGroupId(GROUP_ID_1);
        }
    }

    @Nested
    class SoftDeleteByClassId {

        @Test
        void when_called_expect_jpa_delegated() {
            SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.softDeleteByClassId(CLASS_ID);

            verify(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository).softDeleteByClassId(CLASS_ID);
        }
    }

    @Nested
    class HardDeleteMembersByGroupIds {

        @Test
        void when_list_is_not_empty_expect_member_repository_delegated() {
            final List<Integer> groupIds = List.of(GROUP_ID_1, GROUP_ID_2);

            SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.hardDeleteMembersByGroupIds(groupIds);

            verify(SavedStudentGroupRepositoryImplTest.this.studentGroupMemberJPARepository).deleteByStudentGroupIdIn(groupIds);
        }

        @Test
        void when_list_is_empty_expect_no_repository_call() {
            SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.hardDeleteMembersByGroupIds(Collections.emptyList());

            verify(SavedStudentGroupRepositoryImplTest.this.studentGroupMemberJPARepository, never()).deleteByStudentGroupIdIn(any());
        }
    }

    @Nested
    class FindActiveIdsByClassId {

        @Test
        void when_called_expect_active_ids_returned() {
            final List<Integer> expectedIds = List.of(GROUP_ID_1, GROUP_ID_2);

            when(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(expectedIds);

            final List<Integer> result = SavedStudentGroupRepositoryImplTest.this.savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID);

            assertThat(result).isEqualTo(expectedIds);
            verify(SavedStudentGroupRepositoryImplTest.this.studentGroupJPARepository).findActiveIdsByClassId(CLASS_ID);
        }
    }
}

