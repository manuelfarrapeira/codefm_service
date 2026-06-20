package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassRubricJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricJPARepository;
import org.web.codefm.infrastructure.mapper.SkillRubricMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillRubricRepositoryImplTest {

    private SkillRubricRepositoryImpl skillRubricRepository;

    @Mock
    private SkillRubricJPARepository skillRubricJPARepository;

    @Mock
    private SkillRubricMapper skillRubricMapper;

    @Mock
    private ClassRubricJPARepository classRubricJPARepository;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void beforeEach() {
        this.skillRubricRepository = new SkillRubricRepositoryImpl(
                this.skillRubricJPARepository,
                this.skillRubricMapper,
                this.classRubricJPARepository,
                this.cacheEvictionService);
    }

    @Nested
    class FindBySkillId {

        @Test
        void when_rubrics_exist_expect_rubrics_returned() {
            final Integer skillId = 1;
            final SkillRubricEntity entity = new SkillRubricEntity(1, "Rubric", skillId, null);
            final SkillRubric model = SkillRubric.builder().id(1).title("Rubric").skillId(skillId).build();

            when(SkillRubricRepositoryImplTest.this.skillRubricJPARepository.findBySkillId(skillId)).thenReturn(List.of(entity));
            when(SkillRubricRepositoryImplTest.this.skillRubricMapper.toModelList(List.of(entity))).thenReturn(List.of(model));

            final List<SkillRubric> result = SkillRubricRepositoryImplTest.this.skillRubricRepository.findBySkillId(skillId);

            assertThat(result).isNotNull().hasSize(1);
        }
    }

    @Nested
    class FindById {

        @Test
        void when_rubric_is_found_and_active_expect_rubric_returned() {
            final SkillRubricEntity entity = new SkillRubricEntity(1, "Rubric", 10, null);
            final SkillRubric model = SkillRubric.builder().id(1).title("Rubric").skillId(10).build();

            when(SkillRubricRepositoryImplTest.this.skillRubricJPARepository.findByIdAndDeletionDateIsNull(1)).thenReturn(Optional.of(entity));
            when(SkillRubricRepositoryImplTest.this.skillRubricMapper.toModel(entity)).thenReturn(model);

            final Optional<SkillRubric> result = SkillRubricRepositoryImplTest.this.skillRubricRepository.findById(1);

            assertThat(result).isPresent();
        }

        @Test
        void when_rubric_is_not_found_expect_empty_optional() {
            when(SkillRubricRepositoryImplTest.this.skillRubricJPARepository.findByIdAndDeletionDateIsNull(1)).thenReturn(Optional.empty());

            final Optional<SkillRubric> result = SkillRubricRepositoryImplTest.this.skillRubricRepository.findById(1);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_rubric_expect_rubric_saved_and_cache_evicted() {
            final SkillRubric rubric = SkillRubric.builder().title("New").skillId(10).build();
            final SkillRubricEntity entity = new SkillRubricEntity();
            final SkillRubricEntity saved = new SkillRubricEntity(1, "New", 10, null);
            final SkillRubric savedModel = SkillRubric.builder().id(1).title("New").skillId(10).build();

            when(SkillRubricRepositoryImplTest.this.skillRubricMapper.toEntity(rubric)).thenReturn(entity);
            when(SkillRubricRepositoryImplTest.this.skillRubricJPARepository.save(entity)).thenReturn(saved);
            when(SkillRubricRepositoryImplTest.this.skillRubricMapper.toModel(saved)).thenReturn(savedModel);
            when(SkillRubricRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByRubricId(1)).thenReturn(List.of(10));

            final SkillRubric result = SkillRubricRepositoryImplTest.this.skillRubricRepository.save(rubric);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            verify(SkillRubricRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
        }
    }

    @Nested
    class SoftDeleteById {

        @Test
        void when_rubric_exists_expect_cache_evicted_and_repository_called() {
            when(SkillRubricRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByRubricId(1)).thenReturn(List.of(10));

            SkillRubricRepositoryImplTest.this.skillRubricRepository.softDeleteById(1);

            verify(SkillRubricRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
            verify(SkillRubricRepositoryImplTest.this.skillRubricJPARepository).softDeleteById(1);
        }
    }

    @Nested
    class SoftDeleteBySkillId {

        @Test
        void when_skill_has_rubrics_expect_cache_evicted_and_repository_called() {
            when(SkillRubricRepositoryImplTest.this.skillRubricJPARepository.findActiveIdsBySkillId(10)).thenReturn(Arrays.asList(1, 2));
            when(SkillRubricRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByRubricIds(Arrays.asList(1, 2))).thenReturn(List.of(100, 200));

            SkillRubricRepositoryImplTest.this.skillRubricRepository.softDeleteBySkillId(10);

            verify(SkillRubricRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 100);
            verify(SkillRubricRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 200);
            verify(SkillRubricRepositoryImplTest.this.skillRubricJPARepository).softDeleteBySkillId(10);
        }
    }

    @Nested
    class FindActiveIdsBySkillId {

        @Test
        void when_active_ids_exist_expect_ids_returned() {
            when(SkillRubricRepositoryImplTest.this.skillRubricJPARepository.findActiveIdsBySkillId(10)).thenReturn(Arrays.asList(1, 2));

            final List<Integer> result = SkillRubricRepositoryImplTest.this.skillRubricRepository.findActiveIdsBySkillId(10);

            assertThat(result).hasSize(2);
        }
    }
}
