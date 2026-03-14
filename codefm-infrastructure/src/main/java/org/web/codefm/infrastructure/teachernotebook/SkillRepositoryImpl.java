package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.domain.repository.teachernotebook.SkillRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillJPARepository;
import org.web.codefm.infrastructure.mapper.SkillMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SkillRepositoryImpl implements SkillRepository {

    private final SkillJPARepository skillJPARepository;
    private final SkillMapper skillMapper;

    @Override
    public List<Skill> findByTeacherId(Integer teacherId) {
        return this.skillMapper.toModelList(this.skillJPARepository.findByTeacherId(teacherId));
    }

    @Override
    public Skill save(Skill skill) {
        final SkillEntity skillEntity = this.skillMapper.toEntity(skill);
        final SkillEntity savedEntity = this.skillJPARepository.save(skillEntity);
        return this.skillMapper.toModel(savedEntity);
    }

    @Override
    public Optional<Skill> findById(Integer skillId) {
        return this.skillJPARepository.findByIdAndDeletionDateIsNull(skillId)
                .map(this.skillMapper::toModel);
    }

    @Override
    public Optional<Skill> findByIdAndTeacherId(Integer skillId, Integer teacherId) {
        return this.skillJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(skillId, teacherId)
                .map(this.skillMapper::toModel);
    }

    @Override
    public Skill softDeleteSkill(Integer skillId, Integer teacherId) {
        final SkillEntity skillEntity = this.skillJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(skillId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found or not owned by teacher or already deleted."));

        skillEntity.setDeletionDate(LocalDate.now());
        final SkillEntity updatedEntity = this.skillJPARepository.save(skillEntity);
        return this.skillMapper.toModel(updatedEntity);
    }
}

