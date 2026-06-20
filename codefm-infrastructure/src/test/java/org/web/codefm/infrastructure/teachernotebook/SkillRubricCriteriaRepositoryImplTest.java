package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricCriteriaEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassRubricJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricCriteriaJPARepository;
import org.web.codefm.infrastructure.mapper.SkillRubricCriteriaMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillRubricCriteriaRepositoryImplTest {

    private SkillRubricCriteriaRepositoryImpl skillRubricCriteriaRepository;

    @Mock
    private SkillRubricCriteriaJPARepository skillRubricCriteriaJPARepository;

    @Mock
    private SkillRubricCriteriaMapper skillRubricCriteriaMapper;

    @Mock
    private ClassRubricJPARepository classRubricJPARepository;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void beforeEach() {
        this.skillRubricCriteriaRepository = new SkillRubricCriteriaRepositoryImpl(
                this.skillRubricCriteriaJPARepository,
                this.skillRubricCriteriaMapper,
                this.classRubricJPARepository,
                this.cacheEvictionService);
    }

    @Nested
    class FindActiveByRubricId {

        @Test
        void when_active_criteria_exist_expect_criteria_returned() {
            final SkillRubricCriteriaEntity entity = new SkillRubricCriteriaEntity(1, "Desc", null, 100, 0, 4, null);
            final SkillRubricCriteria model = SkillRubricCriteria.builder().id(1).description("Desc").rubricId(100).gradeStart(0).gradeEnd(4).build();

            when(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findActiveByRubricId(100)).thenReturn(List.of(entity));
            when(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaMapper.toModelList(List.of(entity))).thenReturn(List.of(model));

            final List<SkillRubricCriteria> result = SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaRepository.findActiveByRubricId(100);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    class FindActiveById {

        @Test
        void when_criterion_is_active_expect_criterion_returned() {
            final SkillRubricCriteriaEntity entity = new SkillRubricCriteriaEntity(1, "Desc", null, 100, 0, 4, null);
            final SkillRubricCriteria model = SkillRubricCriteria.builder().id(1).description("Desc").rubricId(100).gradeStart(0).gradeEnd(4).build();

            when(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findActiveById(1)).thenReturn(Optional.of(entity));
            when(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaMapper.toModel(entity)).thenReturn(model);

            final Optional<SkillRubricCriteria> result = SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaRepository.findActiveById(1);

            assertThat(result).isPresent();
        }

        @Test
        void when_criterion_is_not_found_expect_empty_optional() {
            when(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findActiveById(1)).thenReturn(Optional.empty());

            final Optional<SkillRubricCriteria> result = SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaRepository.findActiveById(1);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_criteria_expect_criteria_saved_and_cache_evicted() {
            final SkillRubricCriteria criteria = SkillRubricCriteria.builder().description("A").rubricId(100).gradeStart(0).gradeEnd(4).build();
            final SkillRubricCriteriaEntity entity = new SkillRubricCriteriaEntity();
            final SkillRubricCriteriaEntity saved = new SkillRubricCriteriaEntity(1, "A", null, 100, 0, 4, null);

            when(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaMapper.toEntity(criteria)).thenReturn(entity);
            when(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.save(entity)).thenReturn(saved);
            when(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaMapper.toModel(saved)).thenReturn(
                    SkillRubricCriteria.builder().id(1).description("A").rubricId(100).gradeStart(0).gradeEnd(4).build());
            when(SkillRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByRubricId(100)).thenReturn(List.of(10));

            final SkillRubricCriteria result = SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaRepository.save(criteria);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            verify(SkillRubricCriteriaRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
        }
    }

    @Nested
    class SoftDeleteById {

        @Test
        void when_criterion_exists_expect_cache_evicted_and_repository_called() {
            final SkillRubricCriteriaEntity entity = new SkillRubricCriteriaEntity(1, "Desc", null, 100, 0, 4, null);

            when(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findActiveById(1)).thenReturn(Optional.of(entity));
            when(SkillRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByRubricId(100)).thenReturn(List.of(10));

            SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaRepository.softDeleteById(1);

            verify(SkillRubricCriteriaRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
            verify(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository).softDeleteById(1);
        }
    }

    @Nested
    class SoftDeleteByRubricId {

        @Test
        void when_rubric_has_related_classes_expect_cache_evicted_and_repository_called() {
            when(SkillRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByRubricId(100)).thenReturn(List.of(10, 20));

            SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaRepository.softDeleteByRubricId(100);

            verify(SkillRubricCriteriaRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
            verify(SkillRubricCriteriaRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 20);
            verify(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository).softDeleteByRubricId(100);
        }
    }

    @Nested
    class SoftDeleteByRubricIds {

        @Test
        void when_rubric_ids_are_not_empty_expect_cache_evicted_and_repository_called() {
            when(SkillRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByRubricIds(Arrays.asList(1, 2))).thenReturn(List.of(10));

            SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaRepository.softDeleteByRubricIds(Arrays.asList(1, 2));

            verify(SkillRubricCriteriaRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
            verify(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository).softDeleteByRubricIds(Arrays.asList(1, 2));
        }

        @Test
        void when_rubric_ids_are_empty_expect_repository_not_called() {
            SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaRepository.softDeleteByRubricIds(List.of());

            verify(SkillRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository, never()).softDeleteByRubricIds(any());
        }
    }
}
