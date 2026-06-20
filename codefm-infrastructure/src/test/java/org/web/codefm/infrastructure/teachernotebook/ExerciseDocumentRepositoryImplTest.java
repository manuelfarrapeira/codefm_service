package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseDocumentEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseDocumentJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseJPARepository;
import org.web.codefm.infrastructure.mapper.ExerciseDocumentMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseDocumentRepositoryImplTest {

    private ExerciseDocumentRepositoryImpl exerciseDocumentRepository;

    @Mock
    private ExerciseDocumentJPARepository exerciseDocumentJPARepository;

    @Mock
    private ExerciseDocumentMapper exerciseDocumentMapper;

    @Mock
    private ExerciseJPARepository exerciseJPARepository;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void beforeEach() {
        this.exerciseDocumentRepository = new ExerciseDocumentRepositoryImpl(
                this.exerciseDocumentJPARepository,
                this.exerciseDocumentMapper,
                this.exerciseJPARepository,
                this.cacheEvictionService
        );
    }

    @Nested
    class Save {

        @Test
        void when_valid_document_expect_document_saved_and_cache_evicted() {
            final ExerciseDocument input = ExerciseDocument.builder()
                    .exerciseId(1).document("file.pdf").description("desc").build();
            final ExerciseDocumentEntity entity = new ExerciseDocumentEntity(null, 1, "file.pdf", "desc");
            final ExerciseDocumentEntity savedEntity = new ExerciseDocumentEntity(10, 1, "file.pdf", "desc");
            final ExerciseDocument expected = ExerciseDocument.builder()
                    .id(10).exerciseId(1).document("file.pdf").description("desc").build();

            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentMapper.toEntity(input)).thenReturn(entity);
            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository.save(entity)).thenReturn(savedEntity);
            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentMapper.toModel(savedEntity)).thenReturn(expected);
            when(ExerciseDocumentRepositoryImplTest.this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(List.of(1))).thenReturn(List.of(10));

            final ExerciseDocument result = ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.save(input);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10);
            verify(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository).save(entity);
            verify(ExerciseDocumentRepositoryImplTest.this.cacheEvictionService).evict(CacheName.EXERCISES_BY_CLASS, 10);
        }
    }

    @Nested
    class Update {

        @Test
        void when_valid_document_expect_document_updated_and_cache_evicted() {
            final ExerciseDocument input = ExerciseDocument.builder()
                    .id(10).exerciseId(1).document("file.pdf").description("updated").build();
            final ExerciseDocumentEntity entity = new ExerciseDocumentEntity(10, 1, "file.pdf", "updated");
            final ExerciseDocument expected = ExerciseDocument.builder()
                    .id(10).exerciseId(1).document("file.pdf").description("updated").build();

            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentMapper.toEntity(input)).thenReturn(entity);
            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository.save(entity)).thenReturn(entity);
            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentMapper.toModel(entity)).thenReturn(expected);
            when(ExerciseDocumentRepositoryImplTest.this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(List.of(1))).thenReturn(List.of(10));

            final ExerciseDocument result = ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.update(input);

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEqualTo("updated");
            verify(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository).save(entity);
            verify(ExerciseDocumentRepositoryImplTest.this.cacheEvictionService).evict(CacheName.EXERCISES_BY_CLASS, 10);
        }
    }

    @Nested
    class FindByExerciseId {

        @Test
        void when_exercise_exists_expect_mapped_documents_returned() {
            final Integer exerciseId = 1;
            final List<ExerciseDocumentEntity> entities = List.of(
                    new ExerciseDocumentEntity(10, exerciseId, "file1.pdf", "desc1"),
                    new ExerciseDocumentEntity(11, exerciseId, "file2.pdf", "desc2")
            );
            final List<ExerciseDocument> expected = List.of(
                    ExerciseDocument.builder().id(10).exerciseId(exerciseId).document("file1.pdf").build(),
                    ExerciseDocument.builder().id(11).exerciseId(exerciseId).document("file2.pdf").build()
            );

            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository.findByExerciseId(exerciseId)).thenReturn(entities);
            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentMapper.toModelList(entities)).thenReturn(expected);

            final List<ExerciseDocument> result = ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.findByExerciseId(exerciseId);

            assertThat(result).hasSize(2);
            verify(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository).findByExerciseId(exerciseId);
        }
    }

    @Nested
    class FindById {

        @Test
        void when_document_found_expect_document_returned() {
            final ExerciseDocumentEntity entity = new ExerciseDocumentEntity(10, 1, "file.pdf", "desc");
            final ExerciseDocument expected = ExerciseDocument.builder()
                    .id(10).exerciseId(1).document("file.pdf").description("desc").build();

            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository.findById(10)).thenReturn(Optional.of(entity));
            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentMapper.toModel(entity)).thenReturn(expected);

            final Optional<ExerciseDocument> result = ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.findById(10);

            assertThat(result).isPresent();
            assertThat(result.orElseThrow().getId()).isEqualTo(10);
        }

        @Test
        void when_document_not_found_expect_empty_optional_returned() {
            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository.findById(10)).thenReturn(Optional.empty());

            final Optional<ExerciseDocument> result = ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.findById(10);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class DeleteById {

        @Test
        void when_document_found_expect_jpa_delegated_and_cache_evicted() {
            final ExerciseDocumentEntity entity = new ExerciseDocumentEntity(10, 1, "file.pdf", "desc");

            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository.findById(10)).thenReturn(Optional.of(entity));
            when(ExerciseDocumentRepositoryImplTest.this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(List.of(1))).thenReturn(List.of(20));

            ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.deleteById(10);

            verify(ExerciseDocumentRepositoryImplTest.this.cacheEvictionService).evict(CacheName.EXERCISES_BY_CLASS, 20);
            verify(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository).deleteById(10);
        }
    }

    @Nested
    class DeleteByExerciseId {

        @Test
        void when_called_expect_jpa_delegated_and_cache_evicted() {
            when(ExerciseDocumentRepositoryImplTest.this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(List.of(1))).thenReturn(List.of(20));

            ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.deleteByExerciseId(1);

            verify(ExerciseDocumentRepositoryImplTest.this.cacheEvictionService).evict(CacheName.EXERCISES_BY_CLASS, 20);
            verify(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository).deleteByExerciseId(1);
        }
    }

    @Nested
    class DeleteByExerciseIds {

        @Test
        void when_list_contains_ids_expect_jpa_delegated_and_cache_evicted() {
            final List<Integer> exerciseIds = List.of(1, 2, 3);

            when(ExerciseDocumentRepositoryImplTest.this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(exerciseIds)).thenReturn(List.of(20));

            ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.deleteByExerciseIds(exerciseIds);

            verify(ExerciseDocumentRepositoryImplTest.this.cacheEvictionService).evict(CacheName.EXERCISES_BY_CLASS, 20);
            verify(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository).deleteByExerciseIdIn(exerciseIds);
        }

        @Test
        void when_list_is_null_expect_no_jpa_interaction() {
            ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.deleteByExerciseIds(null);

            verify(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository, never()).deleteByExerciseIdIn(any());
        }

        @Test
        void when_list_is_empty_expect_no_jpa_interaction() {
            ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.deleteByExerciseIds(List.of());

            verify(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository, never()).deleteByExerciseIdIn(any());
        }
    }

    @Nested
    class FindByExerciseIds {

        @Test
        void when_list_contains_ids_expect_mapped_documents_returned() {
            final List<Integer> exerciseIds = List.of(1, 2);
            final List<ExerciseDocumentEntity> entities = List.of(
                    new ExerciseDocumentEntity(10, 1, "file1.pdf", "desc1"),
                    new ExerciseDocumentEntity(11, 2, "file2.pdf", "desc2")
            );
            final List<ExerciseDocument> expected = List.of(
                    ExerciseDocument.builder().id(10).exerciseId(1).document("file1.pdf").build(),
                    ExerciseDocument.builder().id(11).exerciseId(2).document("file2.pdf").build()
            );

            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository.findByExerciseIdIn(exerciseIds)).thenReturn(entities);
            when(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentMapper.toModelList(entities)).thenReturn(expected);

            final List<ExerciseDocument> result = ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.findByExerciseIds(exerciseIds);

            assertThat(result).hasSize(2);
        }

        @Test
        void when_list_is_null_expect_empty_list_returned() {
            final List<ExerciseDocument> result = ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.findByExerciseIds(null);

            assertThat(result).isEmpty();
            verify(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository, never()).findByExerciseIdIn(any());
        }

        @Test
        void when_list_is_empty_expect_empty_list_returned() {
            final List<ExerciseDocument> result = ExerciseDocumentRepositoryImplTest.this.exerciseDocumentRepository.findByExerciseIds(List.of());

            assertThat(result).isEmpty();
            verify(ExerciseDocumentRepositoryImplTest.this.exerciseDocumentJPARepository, never()).findByExerciseIdIn(any());
        }
    }
}
