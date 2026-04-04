package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentGradeEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.GroupAssignmentGradeJPARepository;
import org.web.codefm.infrastructure.mapper.GroupAssignmentGradeMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentGradeRepositoryImplTest {

    @Mock
    private GroupAssignmentGradeJPARepository groupAssignmentGradeJPARepository;

    @Mock
    private GroupAssignmentGradeMapper groupAssignmentGradeMapper;

    @InjectMocks
    private GroupAssignmentGradeRepositoryImpl groupAssignmentGradeRepository;

    @Test
    void findByAssignmentId_shouldReturnMappedGrades() {
        final List<GroupAssignmentGradeEntity> entities = List.of(
                new GroupAssignmentGradeEntity(1, 100, 10, 8.5, null),
                new GroupAssignmentGradeEntity(2, 100, 20, 9.0, null));
        final List<GroupAssignmentGrade> expected = List.of(
                GroupAssignmentGrade.builder().id(1).groupAssignmentId(100).groupId(10).grade(8.5).build(),
                GroupAssignmentGrade.builder().id(2).groupAssignmentId(100).groupId(20).grade(9.0).build());

        when(this.groupAssignmentGradeJPARepository.findByGroupAssignmentIdAndDeletionDateIsNull(100)).thenReturn(entities);
        when(this.groupAssignmentGradeMapper.toModelList(entities)).thenReturn(expected);

        final List<GroupAssignmentGrade> result = this.groupAssignmentGradeRepository.findByAssignmentId(100);

        assertEquals(2, result.size());
        verify(this.groupAssignmentGradeJPARepository).findByGroupAssignmentIdAndDeletionDateIsNull(100);
    }

    @Test
    void findByAssignmentId_shouldReturnEmptyList_whenNoGradesExist() {
        when(this.groupAssignmentGradeJPARepository.findByGroupAssignmentIdAndDeletionDateIsNull(999)).thenReturn(List.of());
        when(this.groupAssignmentGradeMapper.toModelList(List.of())).thenReturn(List.of());

        final List<GroupAssignmentGrade> result = this.groupAssignmentGradeRepository.findByAssignmentId(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByAssignmentIdAndGroupId_shouldReturnMappedGrade_whenFound() {
        final GroupAssignmentGradeEntity entity = new GroupAssignmentGradeEntity(1, 100, 10, 8.5, null);
        final GroupAssignmentGrade expected = GroupAssignmentGrade.builder()
                .id(1).groupAssignmentId(100).groupId(10).grade(8.5).build();

        when(this.groupAssignmentGradeJPARepository.findByGroupAssignmentIdAndGroupIdAndDeletionDateIsNull(100, 10))
                .thenReturn(Optional.of(entity));
        when(this.groupAssignmentGradeMapper.toModel(entity)).thenReturn(expected);

        final Optional<GroupAssignmentGrade> result = this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(100, 10);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals(10, result.get().getGroupId());
    }

    @Test
    void findByAssignmentIdAndGroupId_shouldReturnEmpty_whenNotFound() {
        when(this.groupAssignmentGradeJPARepository.findByGroupAssignmentIdAndGroupIdAndDeletionDateIsNull(100, 999))
                .thenReturn(Optional.empty());

        final Optional<GroupAssignmentGrade> result = this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(100, 999);

        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldMapAndSaveEntity() {
        final GroupAssignmentGrade input = GroupAssignmentGrade.builder()
                .groupAssignmentId(100).groupId(10).grade(8.5).build();
        final GroupAssignmentGradeEntity entity = new GroupAssignmentGradeEntity(null, 100, 10, 8.5, null);
        final GroupAssignmentGradeEntity savedEntity = new GroupAssignmentGradeEntity(1, 100, 10, 8.5, null);
        final GroupAssignmentGrade expected = GroupAssignmentGrade.builder()
                .id(1).groupAssignmentId(100).groupId(10).grade(8.5).build();

        when(this.groupAssignmentGradeMapper.toEntity(input)).thenReturn(entity);
        when(this.groupAssignmentGradeJPARepository.save(entity)).thenReturn(savedEntity);
        when(this.groupAssignmentGradeMapper.toModel(savedEntity)).thenReturn(expected);

        final GroupAssignmentGrade result = this.groupAssignmentGradeRepository.save(input);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(this.groupAssignmentGradeJPARepository).save(entity);
    }

    @Test
    void update_shouldMapAndSaveEntity() {
        final GroupAssignmentGrade input = GroupAssignmentGrade.builder()
                .id(1).groupAssignmentId(100).groupId(10).grade(9.5).build();
        final GroupAssignmentGradeEntity entity = new GroupAssignmentGradeEntity(1, 100, 10, 9.5, null);
        final GroupAssignmentGradeEntity savedEntity = new GroupAssignmentGradeEntity(1, 100, 10, 9.5, null);
        final GroupAssignmentGrade expected = GroupAssignmentGrade.builder()
                .id(1).groupAssignmentId(100).groupId(10).grade(9.5).build();

        when(this.groupAssignmentGradeMapper.toEntity(input)).thenReturn(entity);
        when(this.groupAssignmentGradeJPARepository.save(entity)).thenReturn(savedEntity);
        when(this.groupAssignmentGradeMapper.toModel(savedEntity)).thenReturn(expected);

        final GroupAssignmentGrade result = this.groupAssignmentGradeRepository.update(input);

        assertNotNull(result);
        assertEquals(9.5, result.getGrade());
        verify(this.groupAssignmentGradeJPARepository).save(entity);
    }

    @Test
    void softDeleteById_shouldDelegateToJPARepository() {
        this.groupAssignmentGradeRepository.softDeleteById(1);

        verify(this.groupAssignmentGradeJPARepository).softDeleteById(1);
    }

    @Test
    void softDeleteByGroupAssignmentId_shouldDelegateToJPARepository() {
        this.groupAssignmentGradeRepository.softDeleteByGroupAssignmentId(100);

        verify(this.groupAssignmentGradeJPARepository).softDeleteByGroupAssignmentId(100);
    }

    @Test
    void softDeleteByGroupAssignmentIds_shouldDelegateToJPARepository() {
        final List<Integer> ids = List.of(100, 200);

        this.groupAssignmentGradeRepository.softDeleteByGroupAssignmentIds(ids);

        verify(this.groupAssignmentGradeJPARepository).softDeleteByGroupAssignmentIds(ids);
    }

    @Test
    void softDeleteByGroupAssignmentIds_shouldDoNothing_whenListIsNull() {
        this.groupAssignmentGradeRepository.softDeleteByGroupAssignmentIds(null);

        verify(this.groupAssignmentGradeJPARepository, never()).softDeleteByGroupAssignmentIds(any());
    }

    @Test
    void softDeleteByGroupAssignmentIds_shouldDoNothing_whenListIsEmpty() {
        this.groupAssignmentGradeRepository.softDeleteByGroupAssignmentIds(List.of());

        verify(this.groupAssignmentGradeJPARepository, never()).softDeleteByGroupAssignmentIds(any());
    }

    @Test
    void softDeleteByGroupId_shouldDelegateToJPARepository() {
        this.groupAssignmentGradeRepository.softDeleteByGroupId(50);

        verify(this.groupAssignmentGradeJPARepository).softDeleteByGroupId(50);
    }

    @Test
    void softDeleteByGroupIds_shouldDelegateToJPARepository() {
        final List<Integer> ids = List.of(50, 60);

        this.groupAssignmentGradeRepository.softDeleteByGroupIds(ids);

        verify(this.groupAssignmentGradeJPARepository).softDeleteByGroupIds(ids);
    }

    @Test
    void softDeleteByGroupIds_shouldDoNothing_whenListIsNull() {
        this.groupAssignmentGradeRepository.softDeleteByGroupIds(null);

        verify(this.groupAssignmentGradeJPARepository, never()).softDeleteByGroupIds(any());
    }

    @Test
    void softDeleteByGroupIds_shouldDoNothing_whenListIsEmpty() {
        this.groupAssignmentGradeRepository.softDeleteByGroupIds(List.of());

        verify(this.groupAssignmentGradeJPARepository, never()).softDeleteByGroupIds(any());
    }
}

