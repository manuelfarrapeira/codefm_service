package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.domain.repository.teachernotebook.SkillRubricRepository;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassRubricJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricJPARepository;
import org.web.codefm.infrastructure.mapper.SkillRubricMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SkillRubricRepositoryImpl implements SkillRubricRepository {

    private final SkillRubricJPARepository skillRubricJPARepository;
    private final SkillRubricMapper skillRubricMapper;
    private final ClassRubricJPARepository classRubricJPARepository;
    private final CacheEvictionService cacheEvictionService;

    @Override
    public List<SkillRubric> findBySkillId(Integer skillId) {
        return this.skillRubricMapper.toModelList(this.skillRubricJPARepository.findBySkillId(skillId));
    }

    @Override
    public Optional<SkillRubric> findById(Integer rubricId) {
        return this.skillRubricJPARepository.findByIdAndDeletionDateIsNull(rubricId)
                .map(this.skillRubricMapper::toModel);
    }

    @Override
    public SkillRubric save(SkillRubric rubric) {
        final SkillRubricEntity entity = this.skillRubricMapper.toEntity(rubric);
        final SkillRubricEntity savedEntity = this.skillRubricJPARepository.save(entity);
        this.evictClassRubricsCache(savedEntity.getId());
        return this.skillRubricMapper.toModel(savedEntity);
    }

    @Override
    public void softDeleteById(Integer rubricId) {
        this.evictClassRubricsCache(rubricId);
        this.skillRubricJPARepository.softDeleteById(rubricId);
    }

    @Override
    public void softDeleteBySkillId(Integer skillId) {
        final List<Integer> rubricIds = this.skillRubricJPARepository.findActiveIdsBySkillId(skillId);
        if (!rubricIds.isEmpty()) {
            this.classRubricJPARepository.findDistinctClassIdsByRubricIds(rubricIds)
                    .forEach(classId -> this.cacheEvictionService.evict(CacheName.CLASS_RUBRICS_BY_CLASS, classId));
        }
        this.skillRubricJPARepository.softDeleteBySkillId(skillId);
    }

    @Override
    public List<Integer> findActiveIdsBySkillId(Integer skillId) {
        return this.skillRubricJPARepository.findActiveIdsBySkillId(skillId);
    }

    private void evictClassRubricsCache(Integer rubricId) {
        this.classRubricJPARepository.findDistinctClassIdsByRubricId(rubricId)
                .forEach(classId -> this.cacheEvictionService.evict(CacheName.CLASS_RUBRICS_BY_CLASS, classId));
    }
}
