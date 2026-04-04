package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.GroupAssignmentJPARepository;
import org.web.codefm.infrastructure.mapper.GroupAssignmentMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentRepositoryImplTest {

    @Mock
    private GroupAssignmentJPARepository groupAssignmentJPARepository;

    @Mock
    private GroupAssignmentMapper groupAssignmentMapper;

    @InjectMocks
    private GroupAssignmentRepositoryImpl groupAssignmentRepository;

    @Test
    void findByClassId_shouldReturnMappedAssignments() {
        final List<GroupAssignmentEntity> entities = List.of(
                new GroupAssignmentEntity(1, 10, "Title A", "Desc A", 1, null),
                new GroupAssignmentEntity(2, 10, "Title B", "Desc B", 2, null));
        final List<GroupAssignment> expected = List.of(
                GroupAssignment.builder().id(1).classId(10).title("Title A").description("Desc A").quarter(1).build(),
                GroupAssignment.builder().id(2).classId(10).title("Title B").description("Desc B").quarter(2).build());

        when(this.groupAssignmentJPARepository.findByClassIdAndDeletionDateIsNull(10)).thenReturn(entities);
        when(this.groupAssignmentMapper.toModelList(entities)).thenReturn(expected);

        final List<GroupAssignment> result = this.groupAssignmentRepository.findByClassId(10);

        assertEquals(2, result.size());
        assertEquals("Title A", result.get(0).getTitle());
        assertEquals("Title B", result.get(1).getTitle());
        verify(this.groupAssignmentJPARepository).findByClassIdAndDeletionDateIsNull(10);
    }

    @Test
    void findByClassId_shouldReturnEmptyList_whenNoAssignmentsExist() {
        when(this.groupAssignmentJPARepository.findByClassIdAndDeletionDateIsNull(999)).thenReturn(Collections.emptyList());
        when(this.groupAssignmentMapper.toModelList(Collections.emptyList())).thenReturn(Collections.emptyList());

        final List<GroupAssignment> result = this.groupAssignmentRepository.findByClassId(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnMappedAssignment_whenFound() {
        final GroupAssignmentEntity entity = new GroupAssignmentEntity(1, 10, "Title A", "Desc A", 1, null);
        final GroupAssignment expected = GroupAssignment.builder()
                .id(1).classId(10).title("Title A").description("Desc A").quarter(1).build();

        when(this.groupAssignmentJPARepository.findByIdAndTeacherId(1, 100)).thenReturn(Optional.of(entity));
        when(this.groupAssignmentMapper.toModel(entity)).thenReturn(expected);

        final Optional<GroupAssignment> result = this.groupAssignmentRepository.findByIdAndTeacherId(1, 100);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Title A", result.get().getTitle());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnEmpty_whenNotFound() {
        when(this.groupAssignmentJPARepository.findByIdAndTeacherId(1, 100)).thenReturn(Optional.empty());

        final Optional<GroupAssignment> result = this.groupAssignmentRepository.findByIdAndTeacherId(1, 100);

        assertFalse(result.isPresent());
        verify(this.groupAssignmentMapper, never()).toModel(any(GroupAssignmentEntity.class));
    }

    @Test
    void save_shouldMapAndSaveAndReturnMappedModel() {
        final GroupAssignment input = GroupAssignment.builder()
                .classId(10).title("Title A").description("Desc A").quarter(1).build();
        final GroupAssignmentEntity entity = new GroupAssignmentEntity(null, 10, "Title A", "Desc A", 1, null);
        final GroupAssignmentEntity savedEntity = new GroupAssignmentEntity(1, 10, "Title A", "Desc A", 1, null);
        final GroupAssignment expected = GroupAssignment.builder()
                .id(1).classId(10).title("Title A").description("Desc A").quarter(1).build();

        when(this.groupAssignmentMapper.toEntity(input)).thenReturn(entity);
        when(this.groupAssignmentJPARepository.save(entity)).thenReturn(savedEntity);
        when(this.groupAssignmentMapper.toModel(savedEntity)).thenReturn(expected);

        final GroupAssignment result = this.groupAssignmentRepository.save(input);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Title A", result.getTitle());
        verify(this.groupAssignmentJPARepository).save(entity);
    }

    @Test
    void softDeleteById_shouldDelegateToJPARepository() {
        this.groupAssignmentRepository.softDeleteById(1);

        verify(this.groupAssignmentJPARepository).softDeleteById(1);
    }

    @Test
    void softDeleteByClassId_shouldDelegateToJPARepository() {
        this.groupAssignmentRepository.softDeleteByClassId(10);

        verify(this.groupAssignmentJPARepository).softDeleteByClassId(10);
    }

    @Test
    void findActiveIdsByClassId_shouldReturnActiveIds() {
        final List<Integer> expectedIds = List.of(1, 2, 3);

        when(this.groupAssignmentJPARepository.findActiveIdsByClassId(10)).thenReturn(expectedIds);

        final List<Integer> result = this.groupAssignmentRepository.findActiveIdsByClassId(10);

        assertEquals(expectedIds, result);
        verify(this.groupAssignmentJPARepository).findActiveIdsByClassId(10);
    }

    @Test
    void findActiveIdsByClassId_shouldReturnEmptyList_whenNoActiveAssignmentsExist() {
        when(this.groupAssignmentJPARepository.findActiveIdsByClassId(999)).thenReturn(Collections.emptyList());

        final List<Integer> result = this.groupAssignmentRepository.findActiveIdsByClassId(999);

        assertTrue(result.isEmpty());
    }
}

