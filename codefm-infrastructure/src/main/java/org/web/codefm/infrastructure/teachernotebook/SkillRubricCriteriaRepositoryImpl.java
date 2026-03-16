package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.domain.repository.teachernotebook.SkillRubricCriteriaRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricCriteriaEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricCriteriaJPARepository;
import org.web.codefm.infrastructure.mapper.SkillRubricCriteriaMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SkillRubricCriteriaRepositoryImpl implements SkillRubricCriteriaRepository {

    private final SkillRubricCriteriaJPARepository skillRubricCriteriaJPARepository;
    private final SkillRubricCriteriaMapper skillRubricCriteriaMapper;

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
        return this.skillRubricCriteriaMapper.toModel(saved);
    }

    @Override
    public void softDeleteById(Integer criterionId) {
        this.skillRubricCriteriaJPARepository.softDeleteById(criterionId);
    }

    @Override
    public void softDeleteByRubricId(Integer rubricId) {
        this.skillRubricCriteriaJPARepository.softDeleteByRubricId(rubricId);
    }

    @Override
    public void softDeleteByRubricIds(List<Integer> rubricIds) {
        if (!rubricIds.isEmpty()) {
            this.skillRubricCriteriaJPARepository.softDeleteByRubricIds(rubricIds);
        }
    }
}

