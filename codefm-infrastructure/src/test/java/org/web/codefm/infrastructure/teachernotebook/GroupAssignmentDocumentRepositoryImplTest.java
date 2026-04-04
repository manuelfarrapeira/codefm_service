package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentDocumentEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.GroupAssignmentDocumentJPARepository;
import org.web.codefm.infrastructure.mapper.GroupAssignmentDocumentMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentDocumentRepositoryImplTest {

    @Mock
    private GroupAssignmentDocumentJPARepository groupAssignmentDocumentJPARepository;

    @Mock
    private GroupAssignmentDocumentMapper groupAssignmentDocumentMapper;

    @InjectMocks
    private GroupAssignmentDocumentRepositoryImpl groupAssignmentDocumentRepository;

    @Test
    void findByAssignmentId_shouldReturnMappedDocuments() {
        final List<GroupAssignmentDocumentEntity> entities = List.of(
                new GroupAssignmentDocumentEntity(1, 100, null, "doc1.pdf", "Desc 1", false),
                new GroupAssignmentDocumentEntity(2, 100, 50, "doc2.pdf", "Desc 2", true));
        final List<GroupAssignmentDocument> expected = List.of(
                GroupAssignmentDocument.builder().id(1).groupAssignmentId(100).document("doc1.pdf").groupDocument(false).build(),
                GroupAssignmentDocument.builder().id(2).groupAssignmentId(100).groupId(50).document("doc2.pdf").groupDocument(true).build());

        when(this.groupAssignmentDocumentJPARepository.findByGroupAssignmentId(100)).thenReturn(entities);
        when(this.groupAssignmentDocumentMapper.toModelList(entities)).thenReturn(expected);

        final List<GroupAssignmentDocument> result = this.groupAssignmentDocumentRepository.findByAssignmentId(100);

        assertEquals(2, result.size());
        verify(this.groupAssignmentDocumentJPARepository).findByGroupAssignmentId(100);
    }

    @Test
    void findByAssignmentIdAndGroupId_shouldReturnMappedDocuments() {
        final List<GroupAssignmentDocumentEntity> entities = List.of(
                new GroupAssignmentDocumentEntity(2, 100, 50, "doc2.pdf", "Desc", true));
        final List<GroupAssignmentDocument> expected = List.of(
                GroupAssignmentDocument.builder().id(2).groupAssignmentId(100).groupId(50).document("doc2.pdf").groupDocument(true).build());

        when(this.groupAssignmentDocumentJPARepository.findByGroupAssignmentIdAndGroupId(100, 50)).thenReturn(entities);
        when(this.groupAssignmentDocumentMapper.toModelList(entities)).thenReturn(expected);

        final List<GroupAssignmentDocument> result = this.groupAssignmentDocumentRepository.findByAssignmentIdAndGroupId(100, 50);

        assertEquals(1, result.size());
        assertEquals(50, result.get(0).getGroupId());
    }

    @Test
    void findById_shouldReturnMappedDocument_whenFound() {
        final GroupAssignmentDocumentEntity entity = new GroupAssignmentDocumentEntity(1, 100, null, "doc.pdf", "Desc", false);
        final GroupAssignmentDocument expected = GroupAssignmentDocument.builder()
                .id(1).groupAssignmentId(100).document("doc.pdf").description("Desc").groupDocument(false).build();

        when(this.groupAssignmentDocumentJPARepository.findById(1)).thenReturn(Optional.of(entity));
        when(this.groupAssignmentDocumentMapper.toModel(entity)).thenReturn(expected);

        final Optional<GroupAssignmentDocument> result = this.groupAssignmentDocumentRepository.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(this.groupAssignmentDocumentJPARepository.findById(999)).thenReturn(Optional.empty());

        final Optional<GroupAssignmentDocument> result = this.groupAssignmentDocumentRepository.findById(999);

        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldMapAndSaveEntity() {
        final GroupAssignmentDocument input = GroupAssignmentDocument.builder()
                .groupAssignmentId(100).document("doc.pdf").description("Desc").groupDocument(false).build();
        final GroupAssignmentDocumentEntity entity = new GroupAssignmentDocumentEntity(null, 100, null, "doc.pdf", "Desc", false);
        final GroupAssignmentDocumentEntity savedEntity = new GroupAssignmentDocumentEntity(1, 100, null, "doc.pdf", "Desc", false);
        final GroupAssignmentDocument expected = GroupAssignmentDocument.builder()
                .id(1).groupAssignmentId(100).document("doc.pdf").description("Desc").groupDocument(false).build();

        when(this.groupAssignmentDocumentMapper.toEntity(input)).thenReturn(entity);
        when(this.groupAssignmentDocumentJPARepository.save(entity)).thenReturn(savedEntity);
        when(this.groupAssignmentDocumentMapper.toModel(savedEntity)).thenReturn(expected);

        final GroupAssignmentDocument result = this.groupAssignmentDocumentRepository.save(input);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(this.groupAssignmentDocumentJPARepository).save(entity);
    }

    @Test
    void deleteById_shouldDelegateToJPARepository() {
        this.groupAssignmentDocumentRepository.deleteById(1);

        verify(this.groupAssignmentDocumentJPARepository).deleteById(1);
    }

    @Test
    void deleteByGroupAssignmentId_shouldDelegateToJPARepository() {
        this.groupAssignmentDocumentRepository.deleteByGroupAssignmentId(100);

        verify(this.groupAssignmentDocumentJPARepository).deleteByGroupAssignmentId(100);
    }

    @Test
    void deleteByGroupAssignmentIds_shouldDelegateToJPARepository() {
        final List<Integer> ids = List.of(100, 200);

        this.groupAssignmentDocumentRepository.deleteByGroupAssignmentIds(ids);

        verify(this.groupAssignmentDocumentJPARepository).deleteByGroupAssignmentIds(ids);
    }

    @Test
    void deleteByGroupAssignmentIds_shouldDoNothing_whenListIsNull() {
        this.groupAssignmentDocumentRepository.deleteByGroupAssignmentIds(null);

        verify(this.groupAssignmentDocumentJPARepository, never()).deleteByGroupAssignmentIds(any());
    }

    @Test
    void deleteByGroupAssignmentIds_shouldDoNothing_whenListIsEmpty() {
        this.groupAssignmentDocumentRepository.deleteByGroupAssignmentIds(List.of());

        verify(this.groupAssignmentDocumentJPARepository, never()).deleteByGroupAssignmentIds(any());
    }

    @Test
    void deleteByGroupId_shouldDelegateToJPARepository() {
        this.groupAssignmentDocumentRepository.deleteByGroupId(50);

        verify(this.groupAssignmentDocumentJPARepository).deleteByGroupId(50);
    }

    @Test
    void deleteByGroupIds_shouldDelegateToJPARepository() {
        final List<Integer> ids = List.of(50, 60);

        this.groupAssignmentDocumentRepository.deleteByGroupIds(ids);

        verify(this.groupAssignmentDocumentJPARepository).deleteByGroupIds(ids);
    }

    @Test
    void deleteByGroupIds_shouldDoNothing_whenListIsNull() {
        this.groupAssignmentDocumentRepository.deleteByGroupIds(null);

        verify(this.groupAssignmentDocumentJPARepository, never()).deleteByGroupIds(any());
    }

    @Test
    void deleteByGroupIds_shouldDoNothing_whenListIsEmpty() {
        this.groupAssignmentDocumentRepository.deleteByGroupIds(List.of());

        verify(this.groupAssignmentDocumentJPARepository, never()).deleteByGroupIds(any());
    }

    @Test
    void findByGroupAssignmentIds_shouldReturnMappedDocuments() {
        final List<Integer> assignmentIds = List.of(100, 200);
        final List<GroupAssignmentDocumentEntity> entities = List.of(
                new GroupAssignmentDocumentEntity(1, 100, null, "doc1.pdf", "Desc", false));
        final List<GroupAssignmentDocument> expected = List.of(
                GroupAssignmentDocument.builder().id(1).groupAssignmentId(100).document("doc1.pdf").groupDocument(false).build());

        when(this.groupAssignmentDocumentJPARepository.findByGroupAssignmentIdIn(assignmentIds)).thenReturn(entities);
        when(this.groupAssignmentDocumentMapper.toModelList(entities)).thenReturn(expected);

        final List<GroupAssignmentDocument> result = this.groupAssignmentDocumentRepository.findByGroupAssignmentIds(assignmentIds);

        assertEquals(1, result.size());
    }

    @Test
    void findByGroupAssignmentIds_shouldReturnEmptyList_whenListIsNull() {
        final List<GroupAssignmentDocument> result = this.groupAssignmentDocumentRepository.findByGroupAssignmentIds(null);

        assertTrue(result.isEmpty());
        verify(this.groupAssignmentDocumentJPARepository, never()).findByGroupAssignmentIdIn(any());
    }

    @Test
    void findByGroupAssignmentIds_shouldReturnEmptyList_whenListIsEmpty() {
        final List<GroupAssignmentDocument> result = this.groupAssignmentDocumentRepository.findByGroupAssignmentIds(List.of());

        assertTrue(result.isEmpty());
        verify(this.groupAssignmentDocumentJPARepository, never()).findByGroupAssignmentIdIn(any());
    }

    @Test
    void findByGroupIds_shouldReturnMappedDocuments() {
        final List<Integer> groupIds = List.of(50, 60);
        final List<GroupAssignmentDocumentEntity> entities = List.of(
                new GroupAssignmentDocumentEntity(2, 100, 50, "doc2.pdf", "Desc", true));
        final List<GroupAssignmentDocument> expected = List.of(
                GroupAssignmentDocument.builder().id(2).groupAssignmentId(100).groupId(50).document("doc2.pdf").groupDocument(true).build());

        when(this.groupAssignmentDocumentJPARepository.findByGroupIdIn(groupIds)).thenReturn(entities);
        when(this.groupAssignmentDocumentMapper.toModelList(entities)).thenReturn(expected);

        final List<GroupAssignmentDocument> result = this.groupAssignmentDocumentRepository.findByGroupIds(groupIds);

        assertEquals(1, result.size());
        assertEquals(50, result.get(0).getGroupId());
    }

    @Test
    void findByGroupIds_shouldReturnEmptyList_whenListIsNull() {
        final List<GroupAssignmentDocument> result = this.groupAssignmentDocumentRepository.findByGroupIds(null);

        assertTrue(result.isEmpty());
        verify(this.groupAssignmentDocumentJPARepository, never()).findByGroupIdIn(any());
    }

    @Test
    void findByGroupIds_shouldReturnEmptyList_whenListIsEmpty() {
        final List<GroupAssignmentDocument> result = this.groupAssignmentDocumentRepository.findByGroupIds(List.of());

        assertTrue(result.isEmpty());
        verify(this.groupAssignmentDocumentJPARepository, never()).findByGroupIdIn(any());
    }
}

