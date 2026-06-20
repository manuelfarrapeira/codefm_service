package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentGradeEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.GroupAssignmentGradeJPARepository;
import org.web.codefm.infrastructure.mapper.GroupAssignmentGradeMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentGradeRepositoryImplTest {

    private GroupAssignmentGradeRepositoryImpl groupAssignmentGradeRepository;

    @Mock
    private GroupAssignmentGradeJPARepository groupAssignmentGradeJPARepository;

    @Mock
    private GroupAssignmentGradeMapper groupAssignmentGradeMapper;

    @BeforeEach
    void beforeEach() {
        this.groupAssignmentGradeRepository = new GroupAssignmentGradeRepositoryImpl(this.groupAssignmentGradeJPARepository,
                this.groupAssignmentGradeMapper);
    }

    @Nested
    class FindByAssignmentId {

        @Test
        void when_grades_exist_expect_mapped_grades_returned() {
            final List<GroupAssignmentGradeEntity> entities = List.of(
                    new GroupAssignmentGradeEntity(1, 100, 10, 8.5, null),
                    new GroupAssignmentGradeEntity(2, 100, 20, 9.0, null));
            final List<GroupAssignmentGrade> expected = List.of(
                    GroupAssignmentGrade.builder().id(1).groupAssignmentId(100).groupId(10).grade(8.5).build(),
                    GroupAssignmentGrade.builder().id(2).groupAssignmentId(100).groupId(20).grade(9.0).build());

            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository.findByGroupAssignmentIdAndDeletionDateIsNull(100)).thenReturn(entities);
            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeMapper.toModelList(entities)).thenReturn(expected);

            final List<GroupAssignmentGrade> result = GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.findByAssignmentId(100);

            assertThat(result).hasSize(2);
            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository)
                    .findByGroupAssignmentIdAndDeletionDateIsNull(100);
        }

        @Test
        void when_no_grades_exist_expect_empty_list_returned() {
            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository.findByGroupAssignmentIdAndDeletionDateIsNull(999)).thenReturn(List.of());
            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeMapper.toModelList(List.of())).thenReturn(List.of());

            final List<GroupAssignmentGrade> result = GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.findByAssignmentId(999);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByAssignmentIdAndGroupId {

        @Test
        void when_grade_is_found_expect_mapped_grade_returned() {
            final GroupAssignmentGradeEntity entity = new GroupAssignmentGradeEntity(1, 100, 10, 8.5, null);
            final GroupAssignmentGrade expected = GroupAssignmentGrade.builder()
                    .id(1).groupAssignmentId(100).groupId(10).grade(8.5).build();

            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository.findByGroupAssignmentIdAndGroupIdAndDeletionDateIsNull(100, 10))
                    .thenReturn(Optional.of(entity));
            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeMapper.toModel(entity)).thenReturn(expected);

            final Optional<GroupAssignmentGrade> result = GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(100, 10);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1);
            assertThat(result.get().getGroupId()).isEqualTo(10);
        }

        @Test
        void when_grade_is_not_found_expect_empty_optional_returned() {
            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository.findByGroupAssignmentIdAndGroupIdAndDeletionDateIsNull(100, 999))
                    .thenReturn(Optional.empty());

            final Optional<GroupAssignmentGrade> result = GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(100, 999);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_data_expect_entity_saved() {
            final GroupAssignmentGrade input = GroupAssignmentGrade.builder()
                    .groupAssignmentId(100).groupId(10).grade(8.5).build();
            final GroupAssignmentGradeEntity entity = new GroupAssignmentGradeEntity(null, 100, 10, 8.5, null);
            final GroupAssignmentGradeEntity savedEntity = new GroupAssignmentGradeEntity(1, 100, 10, 8.5, null);
            final GroupAssignmentGrade expected = GroupAssignmentGrade.builder()
                    .id(1).groupAssignmentId(100).groupId(10).grade(8.5).build();

            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeMapper.toEntity(input)).thenReturn(entity);
            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository.save(entity)).thenReturn(savedEntity);
            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeMapper.toModel(savedEntity)).thenReturn(expected);

            final GroupAssignmentGrade result = GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.save(input);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository).save(entity);
        }
    }

    @Nested
    class Update {

        @Test
        void when_valid_data_expect_entity_updated() {
            final GroupAssignmentGrade input = GroupAssignmentGrade.builder()
                    .id(1).groupAssignmentId(100).groupId(10).grade(9.5).build();
            final GroupAssignmentGradeEntity entity = new GroupAssignmentGradeEntity(1, 100, 10, 9.5, null);
            final GroupAssignmentGradeEntity savedEntity = new GroupAssignmentGradeEntity(1, 100, 10, 9.5, null);
            final GroupAssignmentGrade expected = GroupAssignmentGrade.builder()
                    .id(1).groupAssignmentId(100).groupId(10).grade(9.5).build();

            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeMapper.toEntity(input)).thenReturn(entity);
            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository.save(entity)).thenReturn(savedEntity);
            when(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeMapper.toModel(savedEntity)).thenReturn(expected);

            final GroupAssignmentGrade result = GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.update(input);

            assertThat(result).isNotNull();
            assertThat(result.getGrade()).isEqualTo(9.5);
            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository).save(entity);
        }
    }

    @Nested
    class SoftDeleteById {

        @Test
        void when_called_expect_jpa_delegated() {
            GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.softDeleteById(1);

            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository).softDeleteById(1);
        }
    }

    @Nested
    class SoftDeleteByGroupAssignmentId {

        @Test
        void when_called_expect_jpa_delegated() {
            GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.softDeleteByGroupAssignmentId(100);

            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository).softDeleteByGroupAssignmentId(100);
        }
    }

    @Nested
    class SoftDeleteByGroupAssignmentIds {

        @Test
        void when_ids_exist_expect_jpa_delegated() {
            final List<Integer> ids = List.of(100, 200);

            GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.softDeleteByGroupAssignmentIds(ids);

            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository).softDeleteByGroupAssignmentIds(ids);
        }

        @Test
        void when_list_is_null_expect_no_jpa_call() {
            GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.softDeleteByGroupAssignmentIds(null);

            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository, never())
                    .softDeleteByGroupAssignmentIds(any());
        }

        @Test
        void when_list_is_empty_expect_no_jpa_call() {
            GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.softDeleteByGroupAssignmentIds(List.of());

            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository, never())
                    .softDeleteByGroupAssignmentIds(any());
        }
    }

    @Nested
    class SoftDeleteByGroupId {

        @Test
        void when_called_expect_jpa_delegated() {
            GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.softDeleteByGroupId(50);

            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository).softDeleteByGroupId(50);
        }
    }

    @Nested
    class SoftDeleteByGroupIds {

        @Test
        void when_ids_exist_expect_jpa_delegated() {
            final List<Integer> ids = List.of(50, 60);

            GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.softDeleteByGroupIds(ids);

            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository).softDeleteByGroupIds(ids);
        }

        @Test
        void when_list_is_null_expect_no_jpa_call() {
            GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.softDeleteByGroupIds(null);

            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository, never()).softDeleteByGroupIds(any());
        }

        @Test
        void when_list_is_empty_expect_no_jpa_call() {
            GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeRepository.softDeleteByGroupIds(List.of());

            verify(GroupAssignmentGradeRepositoryImplTest.this.groupAssignmentGradeJPARepository, never()).softDeleteByGroupIds(any());
        }
    }
}

