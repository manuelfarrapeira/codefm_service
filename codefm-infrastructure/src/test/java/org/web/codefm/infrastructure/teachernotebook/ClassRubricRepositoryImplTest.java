package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassRubricEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricCriteriaEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassRubricJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricCriteriaJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricJPARepository;
import org.web.codefm.infrastructure.mapper.ClassRubricMapper;
import org.web.codefm.infrastructure.mapper.SkillRubricCriteriaMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassRubricRepositoryImplTest {

    private ClassRubricRepositoryImpl classRubricRepository;

    @Mock
    private ClassRubricJPARepository classRubricJPARepository;

    @Mock
    private SkillRubricJPARepository skillRubricJPARepository;

    @Mock
    private SkillRubricCriteriaJPARepository skillRubricCriteriaJPARepository;

    @Mock
    private ClassRubricMapper classRubricMapper;

    @Mock
    private SkillRubricCriteriaMapper skillRubricCriteriaMapper;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void beforeEach() {
        this.classRubricRepository = new ClassRubricRepositoryImpl(
                this.classRubricJPARepository,
                this.skillRubricJPARepository,
                this.skillRubricCriteriaJPARepository,
                this.classRubricMapper,
                this.skillRubricCriteriaMapper,
                this.cacheEvictionService);
    }

    @Nested
    class FindByClassId {

        @Test
        void when_class_rubrics_exist_expect_enriched_class_rubrics_returned() {
            final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, null);
            final ClassRubric model = ClassRubric.builder().id(1).classId(10).rubricId(50).build();
            final SkillRubricEntity rubricEntity = new SkillRubricEntity(50, "Rubric Title", 5, null);
            final SkillRubricCriteriaEntity criteriaEntity = new SkillRubricCriteriaEntity(100, "Lo hace bien", null, 50, 7, 10, null);
            final SkillRubricCriteria criteriaModel = SkillRubricCriteria.builder().id(100).description("Lo hace bien").rubricId(50).gradeStart(7).gradeEnd(10).build();

            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findByClassIdAndDeletionDateIsNull(10)).thenReturn(List.of(entity));
            when(ClassRubricRepositoryImplTest.this.classRubricMapper.toModelList(List.of(entity))).thenReturn(List.of(model));
            when(ClassRubricRepositoryImplTest.this.skillRubricJPARepository.findByIdAndDeletionDateIsNull(50)).thenReturn(Optional.of(rubricEntity));
            when(ClassRubricRepositoryImplTest.this.skillRubricCriteriaJPARepository.findActiveByRubricId(50)).thenReturn(List.of(criteriaEntity));
            when(ClassRubricRepositoryImplTest.this.skillRubricCriteriaMapper.toModelList(List.of(criteriaEntity))).thenReturn(List.of(criteriaModel));

            final List<ClassRubric> result = ClassRubricRepositoryImplTest.this.classRubricRepository.findByClassId(10);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRubricTitle()).isEqualTo("Rubric Title");
            assertThat(result.get(0).getSkillId()).isEqualTo(5);
            assertThat(result.get(0).getCriteria()).hasSize(1);
            assertThat(result.get(0).getCriteria().get(0).getDescription()).isEqualTo("Lo hace bien");
        }

        @Test
        void when_no_class_rubrics_exist_expect_empty_list_returned() {
            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findByClassIdAndDeletionDateIsNull(10)).thenReturn(List.of());
            when(ClassRubricRepositoryImplTest.this.classRubricMapper.toModelList(List.of())).thenReturn(List.of());

            final List<ClassRubric> result = ClassRubricRepositoryImplTest.this.classRubricRepository.findByClassId(10);

            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        void when_rubric_entity_is_missing_expect_class_rubric_without_enrichment_returned() {
            final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, null);
            final ClassRubric model = ClassRubric.builder().id(1).classId(10).rubricId(50).build();

            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findByClassIdAndDeletionDateIsNull(10)).thenReturn(List.of(entity));
            when(ClassRubricRepositoryImplTest.this.classRubricMapper.toModelList(List.of(entity))).thenReturn(List.of(model));
            when(ClassRubricRepositoryImplTest.this.skillRubricJPARepository.findByIdAndDeletionDateIsNull(50)).thenReturn(Optional.empty());
            when(ClassRubricRepositoryImplTest.this.skillRubricCriteriaJPARepository.findActiveByRubricId(50)).thenReturn(List.of());
            when(ClassRubricRepositoryImplTest.this.skillRubricCriteriaMapper.toModelList(List.of())).thenReturn(List.of());

            final List<ClassRubric> result = ClassRubricRepositoryImplTest.this.classRubricRepository.findByClassId(10);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRubricTitle()).isNull();
            assertThat(result.get(0).getSkillId()).isNull();
            assertThat(result.get(0).getCriteria()).isNotNull().isEmpty();
        }

        @Test
        void when_multiple_class_rubrics_exist_expect_all_rubrics_enriched() {
            final ClassRubricEntity entity1 = new ClassRubricEntity(1, 10, 50, null);
            final ClassRubricEntity entity2 = new ClassRubricEntity(2, 10, 60, null);
            final ClassRubric model1 = ClassRubric.builder().id(1).classId(10).rubricId(50).build();
            final ClassRubric model2 = ClassRubric.builder().id(2).classId(10).rubricId(60).build();

            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findByClassIdAndDeletionDateIsNull(10)).thenReturn(Arrays.asList(entity1, entity2));
            when(ClassRubricRepositoryImplTest.this.classRubricMapper.toModelList(Arrays.asList(entity1, entity2))).thenReturn(Arrays.asList(model1, model2));
            when(ClassRubricRepositoryImplTest.this.skillRubricJPARepository.findByIdAndDeletionDateIsNull(50)).thenReturn(Optional.of(new SkillRubricEntity(50, "Rubric A", 5, null)));
            when(ClassRubricRepositoryImplTest.this.skillRubricJPARepository.findByIdAndDeletionDateIsNull(60)).thenReturn(Optional.of(new SkillRubricEntity(60, "Rubric B", 6, null)));
            when(ClassRubricRepositoryImplTest.this.skillRubricCriteriaJPARepository.findActiveByRubricId(50)).thenReturn(List.of());
            when(ClassRubricRepositoryImplTest.this.skillRubricCriteriaJPARepository.findActiveByRubricId(60)).thenReturn(List.of());
            when(ClassRubricRepositoryImplTest.this.skillRubricCriteriaMapper.toModelList(List.of())).thenReturn(List.of());

            final List<ClassRubric> result = ClassRubricRepositoryImplTest.this.classRubricRepository.findByClassId(10);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getRubricTitle()).isEqualTo("Rubric A");
            assertThat(result.get(0).getSkillId()).isEqualTo(5);
            assertThat(result.get(1).getRubricTitle()).isEqualTo("Rubric B");
            assertThat(result.get(1).getSkillId()).isEqualTo(6);
        }
    }

    @Nested
    class FindByIdAndTeacherId {

        @Test
        void when_class_rubric_exists_expect_class_rubric_returned() {
            final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, null);
            final ClassRubric model = ClassRubric.builder().id(1).classId(10).rubricId(50).build();

            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, 5)).thenReturn(Optional.of(entity));
            when(ClassRubricRepositoryImplTest.this.classRubricMapper.toModel(entity)).thenReturn(model);

            final Optional<ClassRubric> result = ClassRubricRepositoryImplTest.this.classRubricRepository.findByIdAndTeacherId(1, 5);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1);
        }

        @Test
        void when_class_rubric_is_not_found_expect_empty_optional() {
            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(999, 5)).thenReturn(Optional.empty());

            final Optional<ClassRubric> result = ClassRubricRepositoryImplTest.this.classRubricRepository.findByIdAndTeacherId(999, 5);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_class_rubric_expect_class_rubric_persisted_and_cache_evicted() {
            final ClassRubric classRubric = ClassRubric.builder().classId(10).rubricId(50).build();
            final ClassRubricEntity entity = new ClassRubricEntity(null, 10, 50, null);
            final ClassRubricEntity saved = new ClassRubricEntity(1, 10, 50, null);
            final ClassRubric savedModel = ClassRubric.builder().id(1).classId(10).rubricId(50).build();

            when(ClassRubricRepositoryImplTest.this.classRubricMapper.toEntity(classRubric)).thenReturn(entity);
            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.save(entity)).thenReturn(saved);
            when(ClassRubricRepositoryImplTest.this.classRubricMapper.toModel(saved)).thenReturn(savedModel);

            final ClassRubric result = ClassRubricRepositoryImplTest.this.classRubricRepository.save(classRubric);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            verify(ClassRubricRepositoryImplTest.this.classRubricJPARepository).save(entity);
            verify(ClassRubricRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
        }
    }

    @Nested
    class SoftDeleteById {

        @Test
        void when_class_rubric_exists_expect_cache_evicted_and_repository_called() {
            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByIds(List.of(1))).thenReturn(List.of(10));

            ClassRubricRepositoryImplTest.this.classRubricRepository.softDeleteById(1);

            verify(ClassRubricRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
            verify(ClassRubricRepositoryImplTest.this.classRubricJPARepository).softDeleteById(1);
        }
    }

    @Nested
    class SoftDeleteByClassId {

        @Test
        void when_class_id_is_provided_expect_cache_evicted_and_repository_called() {
            ClassRubricRepositoryImplTest.this.classRubricRepository.softDeleteByClassId(10);

            verify(ClassRubricRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
            verify(ClassRubricRepositoryImplTest.this.classRubricJPARepository).softDeleteByClassId(10);
        }
    }

    @Nested
    class SoftDeleteByRubricId {

        @Test
        void when_rubric_has_related_classes_expect_all_caches_evicted_and_repository_called() {
            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByRubricId(50)).thenReturn(List.of(10, 20));

            ClassRubricRepositoryImplTest.this.classRubricRepository.softDeleteByRubricId(50);

            verify(ClassRubricRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
            verify(ClassRubricRepositoryImplTest.this.cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 20);
            verify(ClassRubricRepositoryImplTest.this.classRubricJPARepository).softDeleteByRubricId(50);
        }
    }

    @Nested
    class FindActiveIdsByClassId {

        @Test
        void when_active_ids_exist_expect_ids_returned() {
            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findActiveIdsByClassId(10)).thenReturn(Arrays.asList(1, 2, 3));

            final List<Integer> result = ClassRubricRepositoryImplTest.this.classRubricRepository.findActiveIdsByClassId(10);

            assertThat(result).hasSize(3).isEqualTo(Arrays.asList(1, 2, 3));
        }

        @Test
        void when_no_active_ids_exist_expect_empty_list() {
            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findActiveIdsByClassId(10)).thenReturn(List.of());

            final List<Integer> result = ClassRubricRepositoryImplTest.this.classRubricRepository.findActiveIdsByClassId(10);

            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    class FindActiveIdsByRubricId {

        @Test
        void when_active_ids_exist_expect_ids_returned() {
            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findActiveIdsByRubricId(50)).thenReturn(Arrays.asList(10, 20));

            final List<Integer> result = ClassRubricRepositoryImplTest.this.classRubricRepository.findActiveIdsByRubricId(50);

            assertThat(result).hasSize(2).isEqualTo(Arrays.asList(10, 20));
        }

        @Test
        void when_no_active_ids_exist_expect_empty_list() {
            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.findActiveIdsByRubricId(50)).thenReturn(List.of());

            final List<Integer> result = ClassRubricRepositoryImplTest.this.classRubricRepository.findActiveIdsByRubricId(50);

            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    class ExistsByClassIdAndRubricIdAndDeletionDateIsNull {

        @Test
        void when_relationship_exists_expect_true() {
            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(10, 50)).thenReturn(true);

            final boolean result = ClassRubricRepositoryImplTest.this.classRubricRepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(10, 50);

            assertThat(result).isTrue();
        }

        @Test
        void when_relationship_does_not_exist_expect_false() {
            when(ClassRubricRepositoryImplTest.this.classRubricJPARepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(10, 50)).thenReturn(false);

            final boolean result = ClassRubricRepositoryImplTest.this.classRubricRepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(10, 50);

            assertThat(result).isFalse();
        }
    }
}
