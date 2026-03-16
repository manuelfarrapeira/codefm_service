package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SkillService;
import org.web.codefm.domain.usecase.teachernotebook.SkillUseCase;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillUseCaseImpl implements SkillUseCase {

    private final SkillService skillService;
    private final CascadeSoftDeleteService cascadeSoftDeleteService;

    @Override
    public List<Skill> getSkillsByTeacher() {
        return this.skillService.getSkillsByTeacher();
    }

    @Override
    public Skill createSkill(Skill skill) {
        return this.skillService.createSkill(skill);
    }

    @Override
    public Skill updateSkill(Integer skillId, Skill skill) {
        return this.skillService.updateSkill(skillId, skill);
    }

    @Override
    @Transactional
    public void softDeleteSkill(Integer skillId) {
        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSkill(skillId);
        this.skillService.softDeleteSkill(skillId);
    }
}

