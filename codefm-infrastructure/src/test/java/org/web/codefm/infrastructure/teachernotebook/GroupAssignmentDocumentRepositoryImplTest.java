package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentDocumentEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.GroupAssignmentDocumentJPARepository;
import org.web.codefm.infrastructure.mapper.GroupAssignmentDocumentMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentDocumentRepositoryImplTest {

    private GroupAssignmentDocumentRepositoryImpl groupAssignmentDocumentRepository;

    @Mock
    private GroupAssignmentDocumentJPARepository groupAssignmentDocumentJPARepository;

    @Mock
    private GroupAssignmentDocumentMapper groupAssignmentDocumentMapper;

    @BeforeEach
    void beforeEach() {
        this.groupAssignmentDocumentRepository = new GroupAssignmentDocumentRepositoryImpl(this.groupAssignmentDocumentJPARepository,
                this.groupAssignmentDocumentMapper);
    }

    @Nested
    class FindByAssignmentId {

        @Test
        void when_documents_exist_expect_mapped_documents_returned() {
            final List<GroupAssignmentDocumentEntity> entities = List.of(
                    new GroupAssignmentDocumentEntity(1, 100, null, "doc1.pdf", "Desc 1", false),
                    new GroupAssignmentDocumentEntity(2, 100, 50, "doc2.pdf", "Desc 2", true));
            final List<GroupAssignmentDocument> expected = List.of(
                    GroupAssignmentDocument.builder().id(1).groupAssignmentId(100).document("doc1.pdf").groupDocument(false).build(),
                    GroupAssignmentDocument.builder().id(2).groupAssignmentId(100).groupId(50).document("doc2.pdf").groupDocument(true).build());

            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository.findByGroupAssignmentId(100)).thenReturn(entities);
            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentMapper.toModelList(entities)).thenReturn(expected);

            final List<GroupAssignmentDocument> result = GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.findByAssignmentId(100);

            assertThat(result).hasSize(2);
            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository).findByGroupAssignmentId(100);
        }
    }

    @Nested
    class FindByAssignmentIdAndGroupId {

        @Test
        void when_documents_exist_expect_mapped_documents_returned() {
            final List<GroupAssignmentDocumentEntity> entities = List.of(
                    new GroupAssignmentDocumentEntity(2, 100, 50, "doc2.pdf", "Desc", true));
            final List<GroupAssignmentDocument> expected = List.of(
                    GroupAssignmentDocument.builder().id(2).groupAssignmentId(100).groupId(50).document("doc2.pdf").groupDocument(true).build());

            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository.findByGroupAssignmentIdAndGroupId(100, 50)).thenReturn(entities);
            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentMapper.toModelList(entities)).thenReturn(expected);

            final List<GroupAssignmentDocument> result = GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.findByAssignmentIdAndGroupId(100, 50);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getGroupId()).isEqualTo(50);
        }
    }

    @Nested
    class FindById {

        @Test
        void when_document_is_found_expect_mapped_document_returned() {
            final GroupAssignmentDocumentEntity entity = new GroupAssignmentDocumentEntity(1, 100, null, "doc.pdf", "Desc", false);
            final GroupAssignmentDocument expected = GroupAssignmentDocument.builder()
                    .id(1).groupAssignmentId(100).document("doc.pdf").description("Desc").groupDocument(false).build();

            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository.findById(1)).thenReturn(Optional.of(entity));
            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentMapper.toModel(entity)).thenReturn(expected);

            final Optional<GroupAssignmentDocument> result = GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1);
        }

        @Test
        void when_document_is_not_found_expect_empty_optional_returned() {
            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository.findById(999)).thenReturn(Optional.empty());

            final Optional<GroupAssignmentDocument> result = GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.findById(999);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_data_expect_entity_saved() {
            final GroupAssignmentDocument input = GroupAssignmentDocument.builder()
                    .groupAssignmentId(100).document("doc.pdf").description("Desc").groupDocument(false).build();
            final GroupAssignmentDocumentEntity entity = new GroupAssignmentDocumentEntity(null, 100, null, "doc.pdf", "Desc", false);
            final GroupAssignmentDocumentEntity savedEntity = new GroupAssignmentDocumentEntity(1, 100, null, "doc.pdf", "Desc", false);
            final GroupAssignmentDocument expected = GroupAssignmentDocument.builder()
                    .id(1).groupAssignmentId(100).document("doc.pdf").description("Desc").groupDocument(false).build();

            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentMapper.toEntity(input)).thenReturn(entity);
            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository.save(entity)).thenReturn(savedEntity);
            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentMapper.toModel(savedEntity)).thenReturn(expected);

            final GroupAssignmentDocument result = GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.save(input);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository).save(entity);
        }
    }

    @Nested
    class DeleteById {

        @Test
        void when_called_expect_jpa_delegated() {
            GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.deleteById(1);

            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository).deleteById(1);
        }
    }

    @Nested
    class DeleteByGroupAssignmentId {

        @Test
        void when_called_expect_jpa_delegated() {
            GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.deleteByGroupAssignmentId(100);

            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository).deleteByGroupAssignmentId(100);
        }
    }

    @Nested
    class DeleteByGroupAssignmentIds {

        @Test
        void when_ids_exist_expect_jpa_delegated() {
            final List<Integer> ids = List.of(100, 200);

            GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.deleteByGroupAssignmentIds(ids);

            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository).deleteByGroupAssignmentIds(ids);
        }

        @Test
        void when_list_is_null_expect_no_jpa_call() {
            GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.deleteByGroupAssignmentIds(null);

            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository, never())
                    .deleteByGroupAssignmentIds(any());
        }

        @Test
        void when_list_is_empty_expect_no_jpa_call() {
            GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.deleteByGroupAssignmentIds(List.of());

            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository, never())
                    .deleteByGroupAssignmentIds(any());
        }
    }

    @Nested
    class DeleteByGroupId {

        @Test
        void when_called_expect_jpa_delegated() {
            GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.deleteByGroupId(50);

            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository).deleteByGroupId(50);
        }
    }

    @Nested
    class DeleteByGroupIds {

        @Test
        void when_ids_exist_expect_jpa_delegated() {
            final List<Integer> ids = List.of(50, 60);

            GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.deleteByGroupIds(ids);

            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository).deleteByGroupIds(ids);
        }

        @Test
        void when_list_is_null_expect_no_jpa_call() {
            GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.deleteByGroupIds(null);

            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository, never()).deleteByGroupIds(any());
        }

        @Test
        void when_list_is_empty_expect_no_jpa_call() {
            GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.deleteByGroupIds(List.of());

            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository, never()).deleteByGroupIds(any());
        }
    }

    @Nested
    class FindByGroupAssignmentIds {

        @Test
        void when_assignment_ids_exist_expect_mapped_documents_returned() {
            final List<Integer> assignmentIds = List.of(100, 200);
            final List<GroupAssignmentDocumentEntity> entities = List.of(
                    new GroupAssignmentDocumentEntity(1, 100, null, "doc1.pdf", "Desc", false));
            final List<GroupAssignmentDocument> expected = List.of(
                    GroupAssignmentDocument.builder().id(1).groupAssignmentId(100).document("doc1.pdf").groupDocument(false).build());

            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository.findByGroupAssignmentIdIn(assignmentIds)).thenReturn(entities);
            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentMapper.toModelList(entities)).thenReturn(expected);

            final List<GroupAssignmentDocument> result = GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.findByGroupAssignmentIds(assignmentIds);

            assertThat(result).hasSize(1);
        }

        @Test
        void when_list_is_null_expect_empty_list_returned() {
            final List<GroupAssignmentDocument> result = GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.findByGroupAssignmentIds(null);

            assertThat(result).isEmpty();
            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository, never())
                    .findByGroupAssignmentIdIn(any());
        }

        @Test
        void when_list_is_empty_expect_empty_list_returned() {
            final List<GroupAssignmentDocument> result = GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.findByGroupAssignmentIds(List.of());

            assertThat(result).isEmpty();
            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository, never())
                    .findByGroupAssignmentIdIn(any());
        }
    }

    @Nested
    class FindByGroupIds {

        @Test
        void when_group_ids_exist_expect_mapped_documents_returned() {
            final List<Integer> groupIds = List.of(50, 60);
            final List<GroupAssignmentDocumentEntity> entities = List.of(
                    new GroupAssignmentDocumentEntity(2, 100, 50, "doc2.pdf", "Desc", true));
            final List<GroupAssignmentDocument> expected = List.of(
                    GroupAssignmentDocument.builder().id(2).groupAssignmentId(100).groupId(50).document("doc2.pdf").groupDocument(true).build());

            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository.findByGroupIdIn(groupIds)).thenReturn(entities);
            when(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentMapper.toModelList(entities)).thenReturn(expected);

            final List<GroupAssignmentDocument> result = GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.findByGroupIds(groupIds);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getGroupId()).isEqualTo(50);
        }

        @Test
        void when_list_is_null_expect_empty_list_returned() {
            final List<GroupAssignmentDocument> result = GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.findByGroupIds(null);

            assertThat(result).isEmpty();
            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository, never()).findByGroupIdIn(any());
        }

        @Test
        void when_list_is_empty_expect_empty_list_returned() {
            final List<GroupAssignmentDocument> result = GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentRepository.findByGroupIds(List.of());

            assertThat(result).isEmpty();
            verify(GroupAssignmentDocumentRepositoryImplTest.this.groupAssignmentDocumentJPARepository, never()).findByGroupIdIn(any());
        }
    }
}

