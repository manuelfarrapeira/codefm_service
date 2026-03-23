package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedStudentGroupRepositoryImplTest {

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

    private SavedStudentGroupRepositoryImpl savedStudentGroupRepository;

    @BeforeEach
    void setUp() {
        this.savedStudentGroupRepository = new SavedStudentGroupRepositoryImpl(
                this.studentGroupJPARepository,
                this.studentGroupMemberJPARepository,
                this.studentJPARepository,
                this.savedStudentGroupMapper,
                this.jdbcTemplate);
    }

    @Test
    void findByClassIdWithMembers_shouldReturnGroupsWithMembersAndStudentNames() {
        final StudentGroupEntity groupEntity = new StudentGroupEntity(GROUP_ID_1, CLASS_ID, "Group A", null);
        final StudentGroupMemberEntity memberEntity = new StudentGroupMemberEntity(1, GROUP_ID_1, STUDENT_ID_1);
        final StudentEntity studentEntity = new StudentEntity();
        studentEntity.setId(STUDENT_ID_1);
        studentEntity.setName("Juan");
        studentEntity.setSurnames("García");
        final SavedStudentGroup mappedGroup = SavedStudentGroup.builder().id(GROUP_ID_1).classId(CLASS_ID).name("Group A").build();

        when(this.studentGroupJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(List.of(groupEntity));
        when(this.studentGroupMemberJPARepository.findByStudentGroupIdIn(List.of(GROUP_ID_1))).thenReturn(List.of(memberEntity));
        when(this.studentJPARepository.findAllById(any())).thenReturn(List.of(studentEntity));
        when(this.savedStudentGroupMapper.toModel(groupEntity)).thenReturn(mappedGroup);

        final List<SavedStudentGroup> result = this.savedStudentGroupRepository.findByClassIdWithMembers(CLASS_ID);

        assertEquals(1, result.size());
        assertEquals("Group A", result.get(0).getName());
        assertEquals(1, result.get(0).getMembers().size());
        assertEquals(STUDENT_ID_1, result.get(0).getMembers().get(0).getStudentId());
        assertEquals("Juan", result.get(0).getMembers().get(0).getStudentName());
        assertEquals("García", result.get(0).getMembers().get(0).getStudentSurnames());
    }

    @Test
    void findByClassIdWithMembers_shouldReturnEmptyList_whenNoGroupsExist() {
        when(this.studentGroupJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(Collections.emptyList());

        final List<SavedStudentGroup> result = this.savedStudentGroupRepository.findByClassIdWithMembers(CLASS_ID);

        assertTrue(result.isEmpty());
        verify(this.studentGroupMemberJPARepository, never()).findByStudentGroupIdIn(any());
        verify(this.studentJPARepository, never()).findAllById(any());
    }

    @Test
    void findByClassIdWithMembers_shouldReturnGroupsWithEmptyMembers_whenNoMembersExist() {
        final StudentGroupEntity groupEntity = new StudentGroupEntity(GROUP_ID_1, CLASS_ID, "Group A", null);
        final SavedStudentGroup mappedGroup = SavedStudentGroup.builder().id(GROUP_ID_1).classId(CLASS_ID).name("Group A").build();

        when(this.studentGroupJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(List.of(groupEntity));
        when(this.studentGroupMemberJPARepository.findByStudentGroupIdIn(List.of(GROUP_ID_1))).thenReturn(Collections.emptyList());
        when(this.savedStudentGroupMapper.toModel(groupEntity)).thenReturn(mappedGroup);

        final List<SavedStudentGroup> result = this.savedStudentGroupRepository.findByClassIdWithMembers(CLASS_ID);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getMembers().isEmpty());
        verify(this.studentJPARepository, never()).findAllById(any());
    }

    @Test
    void findByClassIdWithMembers_shouldReturnMultipleGroupsWithCorrectMemberAssignment() {
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

        when(this.studentGroupJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(List.of(groupEntity1, groupEntity2));
        when(this.studentGroupMemberJPARepository.findByStudentGroupIdIn(List.of(GROUP_ID_1, GROUP_ID_2))).thenReturn(List.of(member1, member2));
        when(this.studentJPARepository.findAllById(any())).thenReturn(List.of(student1, student2));
        when(this.savedStudentGroupMapper.toModel(groupEntity1)).thenReturn(mapped1);
        when(this.savedStudentGroupMapper.toModel(groupEntity2)).thenReturn(mapped2);

        final List<SavedStudentGroup> result = this.savedStudentGroupRepository.findByClassIdWithMembers(CLASS_ID);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getMembers().size());
        assertEquals(STUDENT_ID_1, result.get(0).getMembers().get(0).getStudentId());
        assertEquals(1, result.get(1).getMembers().size());
        assertEquals(STUDENT_ID_2, result.get(1).getMembers().get(0).getStudentId());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnGroup_whenFound() {
        final StudentGroupEntity entity = new StudentGroupEntity(GROUP_ID_1, CLASS_ID, "Group A", null);
        final SavedStudentGroup expected = SavedStudentGroup.builder().id(GROUP_ID_1).classId(CLASS_ID).name("Group A").build();

        when(this.studentGroupJPARepository.findByIdAndTeacherId(GROUP_ID_1, TEACHER_ID)).thenReturn(Optional.of(entity));
        when(this.savedStudentGroupMapper.toModel(entity)).thenReturn(expected);

        final Optional<SavedStudentGroup> result = this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID_1, TEACHER_ID);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnEmpty_whenNotFound() {
        when(this.studentGroupJPARepository.findByIdAndTeacherId(GROUP_ID_1, TEACHER_ID)).thenReturn(Optional.empty());

        final Optional<SavedStudentGroup> result = this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID_1, TEACHER_ID);

        assertFalse(result.isPresent());
        verify(this.savedStudentGroupMapper, never()).toModel(any(StudentGroupEntity.class));
    }

    @Test
    void save_shouldMapToEntityAndSaveAndMapBackToModel() {
        final SavedStudentGroup groupToSave = SavedStudentGroup.builder().classId(CLASS_ID).name("Group A").build();
        final StudentGroupEntity entity = new StudentGroupEntity(null, CLASS_ID, "Group A", null);
        final StudentGroupEntity savedEntity = new StudentGroupEntity(GROUP_ID_1, CLASS_ID, "Group A", null);
        final SavedStudentGroup expected = SavedStudentGroup.builder().id(GROUP_ID_1).classId(CLASS_ID).name("Group A").build();

        when(this.savedStudentGroupMapper.toEntity(groupToSave)).thenReturn(entity);
        when(this.studentGroupJPARepository.save(entity)).thenReturn(savedEntity);
        when(this.savedStudentGroupMapper.toModel(savedEntity)).thenReturn(expected);

        final SavedStudentGroup result = this.savedStudentGroupRepository.save(groupToSave);

        assertNotNull(result);
        assertEquals(GROUP_ID_1, result.getId());
        assertEquals("Group A", result.getName());
    }

    @Test
    void saveAll_shouldMapAndSaveAllAndReturnMappedModels() {
        final SavedStudentGroup group1 = SavedStudentGroup.builder().classId(CLASS_ID).name("Group A").build();
        final SavedStudentGroup group2 = SavedStudentGroup.builder().classId(CLASS_ID).name("Group B").build();
        final StudentGroupEntity entity1 = new StudentGroupEntity(null, CLASS_ID, "Group A", null);
        final StudentGroupEntity entity2 = new StudentGroupEntity(null, CLASS_ID, "Group B", null);
        final StudentGroupEntity savedEntity1 = new StudentGroupEntity(GROUP_ID_1, CLASS_ID, "Group A", null);
        final StudentGroupEntity savedEntity2 = new StudentGroupEntity(GROUP_ID_2, CLASS_ID, "Group B", null);
        final SavedStudentGroup expected1 = SavedStudentGroup.builder().id(GROUP_ID_1).classId(CLASS_ID).name("Group A").build();
        final SavedStudentGroup expected2 = SavedStudentGroup.builder().id(GROUP_ID_2).classId(CLASS_ID).name("Group B").build();

        when(this.savedStudentGroupMapper.toEntity(group1)).thenReturn(entity1);
        when(this.savedStudentGroupMapper.toEntity(group2)).thenReturn(entity2);
        when(this.studentGroupJPARepository.saveAll(List.of(entity1, entity2))).thenReturn(List.of(savedEntity1, savedEntity2));
        when(this.savedStudentGroupMapper.toModel(savedEntity1)).thenReturn(expected1);
        when(this.savedStudentGroupMapper.toModel(savedEntity2)).thenReturn(expected2);

        final List<SavedStudentGroup> result = this.savedStudentGroupRepository.saveAll(List.of(group1, group2));

        assertEquals(2, result.size());
        assertEquals(GROUP_ID_1, result.get(0).getId());
        assertEquals(GROUP_ID_2, result.get(1).getId());
    }

    @Test
    void updateName_shouldDelegateToJpaRepository() {
        this.savedStudentGroupRepository.updateName(GROUP_ID_1, "New Name");

        verify(this.studentGroupJPARepository).updateNameById(GROUP_ID_1, "New Name");
    }

    @Test
    void saveMembers_shouldExecuteBatchUpdate() {
        final List<SavedStudentGroupMember> members = List.of(
                SavedStudentGroupMember.builder().studentGroupId(GROUP_ID_1).studentId(STUDENT_ID_1).build(),
                SavedStudentGroupMember.builder().studentGroupId(GROUP_ID_1).studentId(STUDENT_ID_2).build());

        this.savedStudentGroupRepository.saveMembers(members);

        verify(this.jdbcTemplate).batchUpdate(eq("INSERT INTO student_group_members (student_group_id, student_id) VALUES (?, ?)"), any(List.class));
    }

    @Test
    void softDeleteById_shouldDelegateToJpaRepository() {
        this.savedStudentGroupRepository.softDeleteById(GROUP_ID_1);

        verify(this.studentGroupJPARepository).softDeleteById(GROUP_ID_1);
    }

    @Test
    void hardDeleteMembersByGroupId_shouldDelegateToMemberJpaRepository() {
        this.savedStudentGroupRepository.hardDeleteMembersByGroupId(GROUP_ID_1);

        verify(this.studentGroupMemberJPARepository).deleteByStudentGroupId(GROUP_ID_1);
    }

    @Test
    void softDeleteByClassId_shouldDelegateToJpaRepository() {
        this.savedStudentGroupRepository.softDeleteByClassId(CLASS_ID);

        verify(this.studentGroupJPARepository).softDeleteByClassId(CLASS_ID);
    }

    @Test
    void hardDeleteMembersByGroupIds_shouldDelegateToMemberJpaRepository_whenListNotEmpty() {
        final List<Integer> groupIds = List.of(GROUP_ID_1, GROUP_ID_2);

        this.savedStudentGroupRepository.hardDeleteMembersByGroupIds(groupIds);

        verify(this.studentGroupMemberJPARepository).deleteByStudentGroupIdIn(groupIds);
    }

    @Test
    void hardDeleteMembersByGroupIds_shouldNotCallRepository_whenListEmpty() {
        this.savedStudentGroupRepository.hardDeleteMembersByGroupIds(Collections.emptyList());

        verify(this.studentGroupMemberJPARepository, never()).deleteByStudentGroupIdIn(any());
    }

    @Test
    void findActiveIdsByClassId_shouldDelegateToJpaRepository() {
        final List<Integer> expectedIds = List.of(GROUP_ID_1, GROUP_ID_2);
        when(this.studentGroupJPARepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(expectedIds);

        final List<Integer> result = this.savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID);

        assertEquals(expectedIds, result);
        verify(this.studentGroupJPARepository).findActiveIdsByClassId(CLASS_ID);
    }
}

