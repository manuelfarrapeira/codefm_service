package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SkillRubricService;
import org.web.codefm.domain.usecase.teachernotebook.SkillRubricUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillRubricUseCaseImpl implements SkillRubricUseCase {

    private final SkillRubricService skillRubricService;
    private final CascadeSoftDeleteService cascadeSoftDeleteService;

    @Override
    public List<SkillRubric> getRubricsBySkillId(Integer skillId) {
        return this.skillRubricService.getRubricsBySkillId(skillId);
    }

    @Override
    public SkillRubric createRubric(Integer skillId, SkillRubric rubric) {
        return this.skillRubricService.createRubric(skillId, rubric);
    }

    @Override
    public SkillRubric updateRubric(Integer rubricId, SkillRubric rubric) {
        return this.skillRubricService.updateRubric(rubricId, rubric);
    }

    @Override
    @Transactional
    public void deleteRubric(Integer rubricId) {
        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfRubric(rubricId);
        this.skillRubricService.deleteRubric(rubricId);
    }

    @Override
    public List<SkillRubricCriteria> getCriteriaByRubricId(Integer rubricId) {
        return this.skillRubricService.getCriteriaByRubricId(rubricId);
    }

    @Override
    public SkillRubricCriteria createCriterion(Integer rubricId, SkillRubricCriteria criterion) {
        return this.skillRubricService.createCriterion(rubricId, criterion);
    }

    @Override
    public SkillRubricCriteria updateCriterion(Integer rubricId, Integer criterionId, SkillRubricCriteria criterion) {
        return this.skillRubricService.updateCriterion(rubricId, criterionId, criterion);
    }

    @Override
    public void deleteCriterion(Integer rubricId, Integer criterionId) {
        this.skillRubricService.deleteCriterion(rubricId, criterionId);
    }
}



