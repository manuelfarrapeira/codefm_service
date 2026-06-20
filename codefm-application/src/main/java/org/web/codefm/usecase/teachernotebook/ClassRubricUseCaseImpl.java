package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.domain.entity.teachernotebook.StudentCriteriaGroup;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.ClassRubricService;
import org.web.codefm.domain.usecase.teachernotebook.ClassRubricUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassRubricUseCaseImpl implements ClassRubricUseCase {

    private final ClassRubricService classRubricService;
    private final CascadeSoftDeleteService cascadeSoftDeleteService;

    @Override
    public List<ClassRubric> getRubricsByClassId(Integer classId) {
        return this.classRubricService.getRubricsByClassId(classId);
    }

    @Override
    public ClassRubric assignRubricToClass(Integer classId, Integer rubricId) {
        return this.classRubricService.assignRubricToClass(classId, rubricId);
    }

    @Override
    @Transactional
    public void removeRubricFromClass(Integer classRubricId) {
        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfClassRubric(classRubricId);
        this.classRubricService.removeRubricFromClass(classRubricId);
    }

    @Override
    public List<StudentCriteriaGroup> getAllStudentCriteriaByClassId(Integer classId) {
        return this.classRubricService.getAllStudentCriteriaByClassId(classId);
    }

    @Override
    public List<StudentCriteriaGroup> getStudentCriteriaByClassAndStudent(Integer classId, Integer studentId) {
        return this.classRubricService.getStudentCriteriaByClassAndStudent(classId, studentId);
    }

    @Override
    public StudentClassRubricCriteria assignCriterionToStudent(Integer classRubricId, Integer studentId, Integer criterionId) {
        return this.classRubricService.assignCriterionToStudent(classRubricId, studentId, criterionId);
    }

    @Override
    public StudentClassRubricCriteria updateStudentCriterion(Integer id, Integer criterionId) {
        return this.classRubricService.updateStudentCriterion(id, criterionId);
    }

    @Override
    public void removeStudentCriterion(Integer id) {
        this.classRubricService.removeStudentCriterion(id);
    }
}

