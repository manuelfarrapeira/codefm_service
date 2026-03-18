package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.domain.repository.teachernotebook.ClassRubricRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassRubricEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassRubricJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricCriteriaJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricJPARepository;
import org.web.codefm.infrastructure.mapper.ClassRubricMapper;
import org.web.codefm.infrastructure.mapper.SkillRubricCriteriaMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ClassRubricRepositoryImpl implements ClassRubricRepository {

    private final ClassRubricJPARepository classRubricJPARepository;
    private final SkillRubricJPARepository skillRubricJPARepository;
    private final SkillRubricCriteriaJPARepository skillRubricCriteriaJPARepository;
    private final ClassRubricMapper classRubricMapper;
    private final SkillRubricCriteriaMapper skillRubricCriteriaMapper;

    @Override
    public List<ClassRubric> findByClassId(Integer classId) {
        final List<ClassRubricEntity> entities = this.classRubricJPARepository.findByClassIdAndDeletionDateIsNull(classId);
        final List<ClassRubric> classRubrics = this.classRubricMapper.toModelList(entities);
        for (final ClassRubric classRubric : classRubrics) {
            this.enrichWithRubricData(classRubric);
        }
        return classRubrics;
    }

    @Override
    public Optional<ClassRubric> findByIdAndTeacherId(Integer id, Integer teacherId) {
        return this.classRubricJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(id, teacherId)
                .map(this.classRubricMapper::toModel);
    }

    @Override
    public ClassRubric save(ClassRubric classRubric) {
        final ClassRubricEntity entity = this.classRubricMapper.toEntity(classRubric);
        final ClassRubricEntity saved = this.classRubricJPARepository.save(entity);
        return this.classRubricMapper.toModel(saved);
    }

    @Override
    public void softDeleteById(Integer id) {
        this.classRubricJPARepository.softDeleteById(id);
    }

    @Override
    public void softDeleteByClassId(Integer classId) {
        this.classRubricJPARepository.softDeleteByClassId(classId);
    }

    @Override
    public void softDeleteByRubricId(Integer rubricId) {
        this.classRubricJPARepository.softDeleteByRubricId(rubricId);
    }

    @Override
    public List<Integer> findActiveIdsByClassId(Integer classId) {
        return this.classRubricJPARepository.findActiveIdsByClassId(classId);
    }

    @Override
    public List<Integer> findActiveIdsByRubricId(Integer rubricId) {
        return this.classRubricJPARepository.findActiveIdsByRubricId(rubricId);
    }

    @Override
    public boolean existsByClassIdAndRubricIdAndDeletionDateIsNull(Integer classId, Integer rubricId) {
        return this.classRubricJPARepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(classId, rubricId);
    }

    private void enrichWithRubricData(ClassRubric classRubric) {
        this.skillRubricJPARepository.findByIdAndDeletionDateIsNull(classRubric.getRubricId())
                .ifPresent(rubricEntity -> {
                    classRubric.setRubricTitle(rubricEntity.getTitle());
                    classRubric.setSkillId(rubricEntity.getSkillId());
                });
        final List<SkillRubricCriteria> criteria = this.skillRubricCriteriaMapper.toModelList(
                this.skillRubricCriteriaJPARepository.findActiveByRubricId(classRubric.getRubricId()));
        classRubric.setCriteria(criteria);
    }
}

