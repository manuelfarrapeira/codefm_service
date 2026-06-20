package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.domain.repository.teachernotebook.SkillRubricCriteriaRepository;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricCriteriaEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassRubricJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricCriteriaJPARepository;
import org.web.codefm.infrastructure.mapper.SkillRubricCriteriaMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SkillRubricCriteriaRepositoryImpl implements SkillRubricCriteriaRepository {

    private final SkillRubricCriteriaJPARepository skillRubricCriteriaJPARepository;
    private final SkillRubricCriteriaMapper skillRubricCriteriaMapper;
    private final ClassRubricJPARepository classRubricJPARepository;
    private final CacheEvictionService cacheEvictionService;

    @Override
    public List<SkillRubricCriteria> findActiveByRubricId(Integer rubricId) {
        return this.skillRubricCriteriaMapper.toModelList(
                this.skillRubricCriteriaJPARepository.findActiveByRubricId(rubricId));
    }

    @Override
    public Optional<SkillRubricCriteria> findActiveById(Integer criterionId) {
        return this.skillRubricCriteriaJPARepository.findActiveById(criterionId)
                .map(this.skillRubricCriteriaMapper::toModel);
    }

    @Override
    public SkillRubricCriteria save(SkillRubricCriteria criteria) {
        final SkillRubricCriteriaEntity entity = this.skillRubricCriteriaMapper.toEntity(criteria);
        final SkillRubricCriteriaEntity saved = this.skillRubricCriteriaJPARepository.save(entity);
        this.evictClassRubricsCacheByRubricId(saved.getRubricId());
        return this.skillRubricCriteriaMapper.toModel(saved);
    }

    @Override
    public void softDeleteById(Integer criterionId) {
        this.skillRubricCriteriaJPARepository.findActiveById(criterionId)
                .ifPresent(entity -> this.evictClassRubricsCacheByRubricId(entity.getRubricId()));
        this.skillRubricCriteriaJPARepository.softDeleteById(criterionId);
    }

    @Override
    public void softDeleteByRubricId(Integer rubricId) {
        this.evictClassRubricsCacheByRubricId(rubricId);
        this.skillRubricCriteriaJPARepository.softDeleteByRubricId(rubricId);
    }

    @Override
    public void softDeleteByRubricIds(List<Integer> rubricIds) {
        if (!rubricIds.isEmpty()) {
            this.classRubricJPARepository.findDistinctClassIdsByRubricIds(rubricIds)
                    .forEach(classId -> this.cacheEvictionService.evict(CacheName.CLASS_RUBRICS_BY_CLASS, classId));
            this.skillRubricCriteriaJPARepository.softDeleteByRubricIds(rubricIds);
        }
    }

    private void evictClassRubricsCacheByRubricId(Integer rubricId) {
        this.classRubricJPARepository.findDistinctClassIdsByRubricId(rubricId)
                .forEach(classId -> this.cacheEvictionService.evict(CacheName.CLASS_RUBRICS_BY_CLASS, classId));
    }
}
