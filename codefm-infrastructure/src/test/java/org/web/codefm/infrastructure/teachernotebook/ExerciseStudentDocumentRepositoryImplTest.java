package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseStudentDocumentEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseStudentDocumentJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseStudentGradeJPARepository;
import org.web.codefm.infrastructure.mapper.ExerciseStudentDocumentMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseStudentDocumentRepositoryImplTest {

    private ExerciseStudentDocumentRepositoryImpl exerciseStudentDocumentRepository;

    private static final Integer GRADE_ID = 10;
    private static final Integer DOCUMENT_ID = 200;

    @Mock
    private ExerciseStudentDocumentJPARepository exerciseStudentDocumentJPARepository;

    @Mock
    private ExerciseStudentDocumentMapper exerciseStudentDocumentMapper;

    @Mock
    private ExerciseStudentGradeJPARepository exerciseStudentGradeJPARepository;

    @Mock
    private ExerciseJPARepository exerciseJPARepository;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void beforeEach() {
        this.exerciseStudentDocumentRepository = new ExerciseStudentDocumentRepositoryImpl(
                this.exerciseStudentDocumentJPARepository,
                this.exerciseStudentDocumentMapper,
                this.exerciseStudentGradeJPARepository,
                this.exerciseJPARepository,
                this.cacheEvictionService
        );
    }

    @Nested
    class Save {

        @Test
        void when_valid_document_expect_document_saved_and_cache_evicted() {
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder().gradeId(GRADE_ID).build();
            final ExerciseStudentDocumentEntity entity = new ExerciseStudentDocumentEntity();
            final ExerciseStudentDocumentEntity saved = new ExerciseStudentDocumentEntity();
            final ExerciseStudentDocument expected = ExerciseStudentDocument.builder().id(DOCUMENT_ID).gradeId(GRADE_ID).build();

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentMapper.toEntity(document)).thenReturn(entity);
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository.save(entity)).thenReturn(saved);
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentMapper.toModel(saved)).thenReturn(expected);
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentGradeJPARepository.findDistinctClassIdsByGradeIds(List.of(GRADE_ID))).thenReturn(List.of(1));

            final ExerciseStudentDocument result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.save(document);

            assertThat(result).isEqualTo(expected);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 1);
        }
    }

    @Nested
    class Update {

        @Test
        void when_valid_document_expect_document_updated_and_cache_evicted() {
            final ExerciseStudentDocument document = ExerciseStudentDocument.builder().id(DOCUMENT_ID).gradeId(GRADE_ID).build();
            final ExerciseStudentDocumentEntity entity = new ExerciseStudentDocumentEntity();
            final ExerciseStudentDocumentEntity saved = new ExerciseStudentDocumentEntity();
            final ExerciseStudentDocument expected = ExerciseStudentDocument.builder().id(DOCUMENT_ID).gradeId(GRADE_ID).description("updated").build();

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentMapper.toEntity(document)).thenReturn(entity);
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository.save(entity)).thenReturn(saved);
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentMapper.toModel(saved)).thenReturn(expected);
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentGradeJPARepository.findDistinctClassIdsByGradeIds(List.of(GRADE_ID))).thenReturn(List.of(1));

            final ExerciseStudentDocument result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.update(document);

            assertThat(result).isEqualTo(expected);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 1);
        }
    }

    @Nested
    class FindByGradeId {

        @Test
        void when_grade_exists_expect_mapped_documents_returned() {
            final List<ExerciseStudentDocumentEntity> entities = List.of(new ExerciseStudentDocumentEntity());
            final List<ExerciseStudentDocument> expected = List.of(ExerciseStudentDocument.builder().id(DOCUMENT_ID).build());

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findByGradeId(GRADE_ID)).thenReturn(entities);
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentMapper.toModelList(entities)).thenReturn(expected);

            final List<ExerciseStudentDocument> result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.findByGradeId(GRADE_ID);

            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    class FindById {

        @Test
        void when_document_exists_expect_document_returned() {
            final ExerciseStudentDocumentEntity entity = new ExerciseStudentDocumentEntity();
            final ExerciseStudentDocument expected = ExerciseStudentDocument.builder().id(DOCUMENT_ID).build();

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(entity));
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentMapper.toModel(entity)).thenReturn(expected);

            final Optional<ExerciseStudentDocument> result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.findById(DOCUMENT_ID);

            assertThat(result).contains(expected);
        }

        @Test
        void when_document_does_not_exist_expect_empty_optional_returned() {
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

            final Optional<ExerciseStudentDocument> result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.findById(DOCUMENT_ID);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class DeleteById {

        @Test
        void when_document_exists_expect_jpa_delegated_and_cache_evicted() {
            final ExerciseStudentDocumentEntity entity = new ExerciseStudentDocumentEntity(DOCUMENT_ID, GRADE_ID, "doc.pdf", null);

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(entity));
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentGradeJPARepository.findDistinctClassIdsByGradeIds(List.of(GRADE_ID))).thenReturn(List.of(1));

            ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.deleteById(DOCUMENT_ID);

            verify(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository).deleteById(DOCUMENT_ID);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 1);
        }
    }

    @Nested
    class DeleteByGradeId {

        @Test
        void when_called_expect_jpa_delegated_and_cache_evicted() {
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentGradeJPARepository.findDistinctClassIdsByGradeIds(List.of(GRADE_ID))).thenReturn(List.of(1));

            ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.deleteByGradeId(GRADE_ID);

            verify(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository).deleteByGradeId(GRADE_ID);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 1);
        }
    }

    @Nested
    class DeleteByGradeIds {

        @Test
        void when_list_is_not_empty_expect_jpa_delegated_and_cache_evicted() {
            final List<Integer> gradeIds = List.of(1, 2, 3);

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentGradeJPARepository.findDistinctClassIdsByGradeIds(gradeIds)).thenReturn(List.of(10));

            ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.deleteByGradeIds(gradeIds);

            verify(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository).deleteByGradeIdIn(gradeIds);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 10);
        }

        @Test
        void when_list_is_empty_expect_no_jpa_interaction() {
            ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.deleteByGradeIds(List.of());

            verifyNoInteractions(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository);
        }
    }

    @Nested
    class FindByGradeIds {

        @Test
        void when_list_is_not_empty_expect_mapped_documents_returned() {
            final List<Integer> gradeIds = List.of(1, 2, 3);
            final List<ExerciseStudentDocumentEntity> entities = List.of(new ExerciseStudentDocumentEntity());
            final List<ExerciseStudentDocument> expected = List.of(ExerciseStudentDocument.builder().id(DOCUMENT_ID).build());

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findByGradeIdIn(gradeIds)).thenReturn(entities);
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentMapper.toModelList(entities)).thenReturn(expected);

            final List<ExerciseStudentDocument> result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.findByGradeIds(gradeIds);

            assertThat(result).isEqualTo(expected);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository).findByGradeIdIn(gradeIds);
        }

        @Test
        void when_list_is_empty_expect_empty_list_returned() {
            final List<ExerciseStudentDocument> result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.findByGradeIds(List.of());

            assertThat(result).isEmpty();
            verifyNoInteractions(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository);
        }

        @Test
        void when_list_is_null_expect_empty_list_returned() {
            final List<ExerciseStudentDocument> result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.findByGradeIds(null);

            assertThat(result).isEmpty();
            verifyNoInteractions(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository);
        }
    }

    @Nested
    class FindByExerciseId {

        @Test
        void when_exercise_exists_expect_mapped_documents_returned() {
            final Integer exerciseId = 50;
            final List<ExerciseStudentDocumentEntity> entities = List.of(new ExerciseStudentDocumentEntity());
            final List<ExerciseStudentDocument> expected = List.of(ExerciseStudentDocument.builder().id(DOCUMENT_ID).build());

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findByExerciseId(exerciseId)).thenReturn(entities);
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentMapper.toModelList(entities)).thenReturn(expected);

            final List<ExerciseStudentDocument> result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.findByExerciseId(exerciseId);

            assertThat(result).isEqualTo(expected);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository).findByExerciseId(exerciseId);
        }
    }

    @Nested
    class FindByExerciseIds {

        @Test
        void when_list_is_not_empty_expect_mapped_documents_returned() {
            final List<Integer> exerciseIds = List.of(50, 51);
            final List<ExerciseStudentDocumentEntity> entities = List.of(new ExerciseStudentDocumentEntity());
            final List<ExerciseStudentDocument> expected = List.of(ExerciseStudentDocument.builder().id(DOCUMENT_ID).build());

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findByExerciseIdIn(exerciseIds)).thenReturn(entities);
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentMapper.toModelList(entities)).thenReturn(expected);

            final List<ExerciseStudentDocument> result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.findByExerciseIds(exerciseIds);

            assertThat(result).isEqualTo(expected);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository).findByExerciseIdIn(exerciseIds);
        }

        @Test
        void when_list_is_empty_expect_empty_list_returned() {
            final List<ExerciseStudentDocument> result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.findByExerciseIds(List.of());

            assertThat(result).isEmpty();
            verifyNoInteractions(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository);
        }

        @Test
        void when_list_is_null_expect_empty_list_returned() {
            final List<ExerciseStudentDocument> result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.findByExerciseIds(null);

            assertThat(result).isEmpty();
            verifyNoInteractions(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository);
        }
    }

    @Nested
    class DeleteByExerciseId {

        @Test
        void when_called_expect_jpa_delegated_and_cache_evicted() {
            final Integer exerciseId = 50;

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(List.of(exerciseId))).thenReturn(List.of(1));

            ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.deleteByExerciseId(exerciseId);

            verify(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository).deleteByExerciseId(exerciseId);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 1);
        }
    }

    @Nested
    class DeleteByExerciseIds {

        @Test
        void when_list_is_not_empty_expect_jpa_delegated_and_cache_evicted() {
            final List<Integer> exerciseIds = List.of(50, 51);

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(exerciseIds)).thenReturn(List.of(1));

            ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.deleteByExerciseIds(exerciseIds);

            verify(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository).deleteByExerciseIdIn(exerciseIds);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 1);
        }

        @Test
        void when_list_is_empty_expect_no_jpa_interaction() {
            ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.deleteByExerciseIds(List.of());

            verifyNoInteractions(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository);
        }

        @Test
        void when_list_is_null_expect_no_jpa_interaction() {
            ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.deleteByExerciseIds(null);

            verifyNoInteractions(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository);
        }
    }

    @Nested
    class FindByStudentId {

        @Test
        void when_student_exists_expect_mapped_documents_returned() {
            final Integer studentId = 7;
            final List<ExerciseStudentDocumentEntity> entities = List.of(new ExerciseStudentDocumentEntity());
            final List<ExerciseStudentDocument> expected = List.of(ExerciseStudentDocument.builder().id(DOCUMENT_ID).build());

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findByStudentId(studentId)).thenReturn(entities);
            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentMapper.toModelList(entities)).thenReturn(expected);

            final List<ExerciseStudentDocument> result = ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.findByStudentId(studentId);

            assertThat(result).isEqualTo(expected);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository).findByStudentId(studentId);
        }
    }

    @Nested
    class DeleteByStudentId {

        @Test
        void when_called_expect_jpa_delegated_and_cache_evicted() {
            final Integer studentId = 7;

            when(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentGradeJPARepository.findDistinctClassIdsByStudentId(studentId)).thenReturn(List.of(10));

            ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentRepository.deleteByStudentId(studentId);

            verify(ExerciseStudentDocumentRepositoryImplTest.this.exerciseStudentDocumentJPARepository).deleteByStudentId(studentId);
            verify(ExerciseStudentDocumentRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 10);
        }
    }
}
