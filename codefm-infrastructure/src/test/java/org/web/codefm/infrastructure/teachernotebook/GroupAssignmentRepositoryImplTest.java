package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.GroupAssignmentJPARepository;
import org.web.codefm.infrastructure.mapper.GroupAssignmentMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentRepositoryImplTest {

    private GroupAssignmentRepositoryImpl groupAssignmentRepository;

    @Mock
    private GroupAssignmentJPARepository groupAssignmentJPARepository;

    @Mock
    private GroupAssignmentMapper groupAssignmentMapper;

    @BeforeEach
    void beforeEach() {
        this.groupAssignmentRepository = new GroupAssignmentRepositoryImpl(this.groupAssignmentJPARepository,
                this.groupAssignmentMapper);
    }

    @Nested
    class FindByClassId {

        @Test
        void when_assignments_exist_expect_mapped_assignments_returned() {
            final List<GroupAssignmentEntity> entities = List.of(
                    new GroupAssignmentEntity(1, 10, "Title A", "Desc A", 1, null),
                    new GroupAssignmentEntity(2, 10, "Title B", "Desc B", 2, null));
            final List<GroupAssignment> expected = List.of(
                    GroupAssignment.builder().id(1).classId(10).title("Title A").description("Desc A").quarter(1).build(),
                    GroupAssignment.builder().id(2).classId(10).title("Title B").description("Desc B").quarter(2).build());

            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository.findByClassIdAndDeletionDateIsNull(10)).thenReturn(entities);
            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentMapper.toModelList(entities)).thenReturn(expected);

            final List<GroupAssignment> result = GroupAssignmentRepositoryImplTest.this.groupAssignmentRepository.findByClassId(10);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Title A");
            assertThat(result.get(1).getTitle()).isEqualTo("Title B");
            verify(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository).findByClassIdAndDeletionDateIsNull(10);
        }

        @Test
        void when_no_assignments_exist_expect_empty_list_returned() {
            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository.findByClassIdAndDeletionDateIsNull(999)).thenReturn(Collections.emptyList());
            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentMapper.toModelList(Collections.emptyList())).thenReturn(Collections.emptyList());

            final List<GroupAssignment> result = GroupAssignmentRepositoryImplTest.this.groupAssignmentRepository.findByClassId(999);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByIdAndTeacherId {

        @Test
        void when_assignment_is_found_expect_mapped_assignment_returned() {
            final GroupAssignmentEntity entity = new GroupAssignmentEntity(1, 10, "Title A", "Desc A", 1, null);
            final GroupAssignment expected = GroupAssignment.builder()
                    .id(1).classId(10).title("Title A").description("Desc A").quarter(1).build();

            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository.findByIdAndTeacherId(1, 100)).thenReturn(Optional.of(entity));
            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentMapper.toModel(entity)).thenReturn(expected);

            final Optional<GroupAssignment> result = GroupAssignmentRepositoryImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(1, 100);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1);
            assertThat(result.get().getTitle()).isEqualTo("Title A");
        }

        @Test
        void when_assignment_is_not_found_expect_empty_optional_returned() {
            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository.findByIdAndTeacherId(1, 100)).thenReturn(Optional.empty());

            final Optional<GroupAssignment> result = GroupAssignmentRepositoryImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(1, 100);

            assertThat(result).isNotPresent();
            verify(GroupAssignmentRepositoryImplTest.this.groupAssignmentMapper, never()).toModel(any(GroupAssignmentEntity.class));
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_data_expect_mapped_model_returned() {
            final GroupAssignment input = GroupAssignment.builder()
                    .classId(10).title("Title A").description("Desc A").quarter(1).build();
            final GroupAssignmentEntity entity = new GroupAssignmentEntity(null, 10, "Title A", "Desc A", 1, null);
            final GroupAssignmentEntity savedEntity = new GroupAssignmentEntity(1, 10, "Title A", "Desc A", 1, null);
            final GroupAssignment expected = GroupAssignment.builder()
                    .id(1).classId(10).title("Title A").description("Desc A").quarter(1).build();

            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentMapper.toEntity(input)).thenReturn(entity);
            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository.save(entity)).thenReturn(savedEntity);
            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentMapper.toModel(savedEntity)).thenReturn(expected);

            final GroupAssignment result = GroupAssignmentRepositoryImplTest.this.groupAssignmentRepository.save(input);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getTitle()).isEqualTo("Title A");
            verify(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository).save(entity);
        }
    }

    @Nested
    class SoftDeleteById {

        @Test
        void when_called_expect_jpa_delegated() {
            GroupAssignmentRepositoryImplTest.this.groupAssignmentRepository.softDeleteById(1);

            verify(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository).softDeleteById(1);
        }
    }

    @Nested
    class SoftDeleteByClassId {

        @Test
        void when_called_expect_jpa_delegated() {
            GroupAssignmentRepositoryImplTest.this.groupAssignmentRepository.softDeleteByClassId(10);

            verify(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository).softDeleteByClassId(10);
        }
    }

    @Nested
    class FindActiveIdsByClassId {

        @Test
        void when_active_assignments_exist_expect_active_ids_returned() {
            final List<Integer> expectedIds = List.of(1, 2, 3);

            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository.findActiveIdsByClassId(10)).thenReturn(expectedIds);

            final List<Integer> result = GroupAssignmentRepositoryImplTest.this.groupAssignmentRepository.findActiveIdsByClassId(10);

            assertThat(result).isEqualTo(expectedIds);
            verify(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository).findActiveIdsByClassId(10);
        }

        @Test
        void when_no_active_assignments_exist_expect_empty_list_returned() {
            when(GroupAssignmentRepositoryImplTest.this.groupAssignmentJPARepository.findActiveIdsByClassId(999)).thenReturn(Collections.emptyList());

            final List<Integer> result = GroupAssignmentRepositoryImplTest.this.groupAssignmentRepository.findActiveIdsByClassId(999);

            assertThat(result).isEmpty();
        }
    }
}

