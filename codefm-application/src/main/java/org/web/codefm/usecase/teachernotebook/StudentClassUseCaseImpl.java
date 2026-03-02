package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.StudentClassService;
import org.web.codefm.domain.usecase.teachernotebook.StudentClassUseCase;

@Service
@RequiredArgsConstructor
public class StudentClassUseCaseImpl implements StudentClassUseCase {

    private final StudentClassService studentClassService;
    private final CascadeSoftDeleteService cascadeSoftDeleteService;

    @Override
    public void addStudentToClass(Integer classId, Integer studentId) {
        studentClassService.addStudentToClass(classId, studentId);
    }

    @Override
    @Transactional
    public void removeStudentFromClass(Integer classId, Integer studentId) {
        StudentClass association = studentClassService.findActiveAssociation(classId, studentId);
        cascadeSoftDeleteService.cascadeDeleteChildrenOfStudentClass(association.getId());
        studentClassService.removeStudentFromClass(classId, studentId);
    }
}
