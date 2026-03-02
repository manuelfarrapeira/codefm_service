package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.SubjectClassDetail;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SubjectClassService;
import org.web.codefm.domain.usecase.teachernotebook.SubjectClassUseCase;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectClassUseCaseImpl implements SubjectClassUseCase {

    private final SubjectClassService subjectClassService;
    private final CascadeSoftDeleteService cascadeSoftDeleteService;

    @Override
    public List<SubjectClassDetail> getSubjectsByClassId(Integer classId) {
        return subjectClassService.getSubjectsByClassId(classId);
    }

    @Override
    public List<ClassWithSubjects> getAllClassesWithSubjects() {
        return subjectClassService.getAllClassesWithSubjects();
    }

    @Override
    public List<SubjectClassDetail> assignSubjectsToClass(Integer classId, List<Integer> subjectIds) {
        return subjectClassService.assignSubjectsToClass(classId, subjectIds);
    }

    @Override
    @Transactional
    public void removeSubjectsFromClass(Integer classId, List<Integer> subjectIds) {
        List<Integer> subjectClassIds = subjectClassService.findActiveSubjectClassIds(classId, subjectIds);
        for (Integer subjectClassId : subjectClassIds) {
            cascadeSoftDeleteService.cascadeDeleteChildrenOfSubjectClass(subjectClassId);
        }
        subjectClassService.removeSubjectsFromClass(classId, subjectIds);
    }
}
