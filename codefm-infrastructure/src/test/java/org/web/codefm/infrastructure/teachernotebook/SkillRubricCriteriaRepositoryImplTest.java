package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillRubricCriteriaRepositoryImplTest {

    @Mock
    private SkillRubricCriteriaJPARepository skillRubricCriteriaJPARepository;
    @Mock
    private SkillRubricCriteriaMapper skillRubricCriteriaMapper;
    @Mock
    private ClassRubricJPARepository classRubricJPARepository;
    @Mock
    private CacheEvictionService cacheEvictionService;

    @InjectMocks
    private SkillRubricCriteriaRepositoryImpl skillRubricCriteriaRepository;

    @Test
    void findActiveByRubricId_shouldReturnActiveCriteria() {
        final SkillRubricCriteriaEntity entity = new SkillRubricCriteriaEntity(1, "Desc", null, 100, 0, 4, null);
        final SkillRubricCriteria model = SkillRubricCriteria.builder().id(1).description("Desc").rubricId(100).gradeStart(0).gradeEnd(4).build();

        when(skillRubricCriteriaJPARepository.findActiveByRubricId(100)).thenReturn(List.of(entity));
        when(skillRubricCriteriaMapper.toModelList(List.of(entity))).thenReturn(List.of(model));

        final List<SkillRubricCriteria> result = skillRubricCriteriaRepository.findActiveByRubricId(100);

        assertEquals(1, result.size());
    }

    @Test
    void findActiveById_shouldReturnCriterionWhenActive() {
        final SkillRubricCriteriaEntity entity = new SkillRubricCriteriaEntity(1, "Desc", null, 100, 0, 4, null);
        final SkillRubricCriteria model = SkillRubricCriteria.builder().id(1).description("Desc").rubricId(100).gradeStart(0).gradeEnd(4).build();

        when(skillRubricCriteriaJPARepository.findActiveById(1)).thenReturn(Optional.of(entity));
        when(skillRubricCriteriaMapper.toModel(entity)).thenReturn(model);

        final Optional<SkillRubricCriteria> result = skillRubricCriteriaRepository.findActiveById(1);

        assertTrue(result.isPresent());
    }

    @Test
    void findActiveById_shouldReturnEmptyWhenNotFound() {
        when(skillRubricCriteriaJPARepository.findActiveById(1)).thenReturn(Optional.empty());

        final Optional<SkillRubricCriteria> result = skillRubricCriteriaRepository.findActiveById(1);

        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldMapAndSaveAndEvictCache() {
        final SkillRubricCriteria criteria = SkillRubricCriteria.builder().description("A").rubricId(100).gradeStart(0).gradeEnd(4).build();
        final SkillRubricCriteriaEntity entity = new SkillRubricCriteriaEntity();
        final SkillRubricCriteriaEntity saved = new SkillRubricCriteriaEntity(1, "A", null, 100, 0, 4, null);

        when(skillRubricCriteriaMapper.toEntity(criteria)).thenReturn(entity);
        when(skillRubricCriteriaJPARepository.save(entity)).thenReturn(saved);
        when(skillRubricCriteriaMapper.toModel(saved)).thenReturn(
                SkillRubricCriteria.builder().id(1).description("A").rubricId(100).gradeStart(0).gradeEnd(4).build());
        when(classRubricJPARepository.findDistinctClassIdsByRubricId(100)).thenReturn(List.of(10));

        final SkillRubricCriteria result = skillRubricCriteriaRepository.save(criteria);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
    }

    @Test
    void softDeleteById_shouldEvictCacheAndDelegate() {
        final SkillRubricCriteriaEntity entity = new SkillRubricCriteriaEntity(1, "Desc", null, 100, 0, 4, null);
        when(skillRubricCriteriaJPARepository.findActiveById(1)).thenReturn(Optional.of(entity));
        when(classRubricJPARepository.findDistinctClassIdsByRubricId(100)).thenReturn(List.of(10));

        skillRubricCriteriaRepository.softDeleteById(1);

        verify(cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
        verify(skillRubricCriteriaJPARepository).softDeleteById(1);
    }

    @Test
    void softDeleteByRubricId_shouldEvictCacheAndDelegate() {
        when(classRubricJPARepository.findDistinctClassIdsByRubricId(100)).thenReturn(List.of(10, 20));

        skillRubricCriteriaRepository.softDeleteByRubricId(100);

        verify(cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
        verify(cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 20);
        verify(skillRubricCriteriaJPARepository).softDeleteByRubricId(100);
    }

    @Test
    void softDeleteByRubricIds_shouldEvictCacheAndDelegateWhenNotEmpty() {
        when(classRubricJPARepository.findDistinctClassIdsByRubricIds(Arrays.asList(1, 2))).thenReturn(List.of(10));

        skillRubricCriteriaRepository.softDeleteByRubricIds(Arrays.asList(1, 2));

        verify(cacheEvictionService).evict(CacheName.CLASS_RUBRICS_BY_CLASS, 10);
        verify(skillRubricCriteriaJPARepository).softDeleteByRubricIds(Arrays.asList(1, 2));
    }

    @Test
    void softDeleteByRubricIds_shouldNotDelegateWhenEmpty() {
        skillRubricCriteriaRepository.softDeleteByRubricIds(List.of());
        verify(skillRubricCriteriaJPARepository, never()).softDeleteByRubricIds(any());
    }
}
