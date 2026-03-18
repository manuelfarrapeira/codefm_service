package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRubricCriteriaRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassRubricCriteriaEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.*;
import org.web.codefm.infrastructure.mapper.StudentClassRubricCriteriaMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StudentClassRubricCriteriaRepositoryImpl implements StudentClassRubricCriteriaRepository {

    private final StudentClassRubricCriteriaJPARepository studentClassRubricCriteriaJPARepository;
    private final ClassRubricJPARepository classRubricJPARepository;
    private final StudentJPARepository studentJPARepository;
    private final SkillRubricCriteriaJPARepository skillRubricCriteriaJPARepository;
    private final SkillRubricJPARepository skillRubricJPARepository;
    private final StudentClassRubricCriteriaMapper studentClassRubricCriteriaMapper;

    @Override
    public List<StudentClassRubricCriteria> findByClassId(Integer classId) {
        final List<StudentClassRubricCriteriaEntity> entities =
                this.studentClassRubricCriteriaJPARepository.findByClassIdAndDeletionDateIsNull(classId);
        final List<StudentClassRubricCriteria> result = this.studentClassRubricCriteriaMapper.toModelList(entities);
        result.forEach(this::enrichWithDisplayData);
        return result;
    }

    @Override
    public List<StudentClassRubricCriteria> findByClassIdAndStudentId(Integer classId, Integer studentId) {
        final List<StudentClassRubricCriteriaEntity> entities =
                this.studentClassRubricCriteriaJPARepository
                        .findByClassIdAndStudentIdAndDeletionDateIsNull(classId, studentId);
        final List<StudentClassRubricCriteria> result = this.studentClassRubricCriteriaMapper.toModelList(entities);
        result.forEach(this::enrichWithDisplayData);
        return result;
    }

    @Override
    public Optional<StudentClassRubricCriteria> findByIdAndTeacherId(Integer id, Integer teacherId) {
        return this.studentClassRubricCriteriaJPARepository
                .findByIdAndTeacherIdAndDeletionDateIsNull(id, teacherId)
                .map(this.studentClassRubricCriteriaMapper::toModel);
    }

    @Override
    public StudentClassRubricCriteria save(StudentClassRubricCriteria criteria) {
        final StudentClassRubricCriteriaEntity entity = this.studentClassRubricCriteriaMapper.toEntity(criteria);
        final StudentClassRubricCriteriaEntity saved = this.studentClassRubricCriteriaJPARepository.save(entity);
        return this.studentClassRubricCriteriaMapper.toModel(saved);
    }

    @Override
    public void softDeleteById(Integer id) {
        this.studentClassRubricCriteriaJPARepository.softDeleteById(id);
    }

    @Override
    public void softDeleteByClassRubricId(Integer classRubricId) {
        this.studentClassRubricCriteriaJPARepository.softDeleteByClassRubricId(classRubricId);
    }

    @Override
    public void softDeleteByClassRubricIds(List<Integer> classRubricIds) {
        if (!classRubricIds.isEmpty()) {
            this.studentClassRubricCriteriaJPARepository.softDeleteByClassRubricIds(classRubricIds);
        }
    }

    @Override
    public boolean existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(Integer classRubricId, Integer studentId) {
        return this.studentClassRubricCriteriaJPARepository
                .existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(classRubricId, studentId);
    }

    private void enrichWithDisplayData(StudentClassRubricCriteria criteria) {
        this.classRubricJPARepository.findByIdAndDeletionDateIsNull(criteria.getClassRubricId())
                .ifPresent(classRubric -> {
                    criteria.setRubricId(classRubric.getRubricId());
                    this.skillRubricJPARepository.findById(classRubric.getRubricId())
                            .ifPresent(rubric -> criteria.setRubricTitle(rubric.getTitle()));
                });

        this.studentJPARepository.findById(criteria.getStudentId())
                .ifPresent(student -> {
                    criteria.setStudentName(student.getName());
                    criteria.setStudentSurnames(student.getSurnames());
                });

        this.skillRubricCriteriaJPARepository.findById(criteria.getCriterionId())
                .ifPresent(criterion -> {
                    criteria.setCriterionDescription(criterion.getDescription());
                    criteria.setGradeStart(criterion.getGradeStart());
                    criteria.setGradeEnd(criterion.getGradeEnd());
                });
    }
}


