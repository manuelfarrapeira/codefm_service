package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SubjectService;
import org.web.codefm.domain.usecase.teachernotebook.SubjectUseCase;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectUseCaseImpl implements SubjectUseCase {

    private final SubjectService subjectService;
    private final CascadeSoftDeleteService cascadeSoftDeleteService;

    @Override
    public List<Subject> getSubjectsByTeacher() {
        return subjectService.getSubjectsByTeacher();
    }

    @Override
    public Subject createSubject(Subject subject) {
        return subjectService.createSubject(subject);
    }

    @Override
    @Transactional
    public void softDeleteSubject(Integer subjectId) {
        cascadeSoftDeleteService.cascadeDeleteChildrenOfSubject(subjectId);
        subjectService.softDeleteSubject(subjectId);
    }

    @Override
    public Subject updateSubject(Integer subjectId, Subject subject) {
        return subjectService.updateSubject(subjectId, subject);
    }
}
