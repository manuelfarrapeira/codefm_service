package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.service.teachernotebook.SubjectClassService;
import org.web.codefm.domain.usecase.teachernotebook.SubjectClassUseCase;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectClassUseCaseImpl implements SubjectClassUseCase {

    private final SubjectClassService subjectClassService;

    @Override
    public List<Subject> getSubjectsByClassId(Integer classId) {
        return subjectClassService.getSubjectsByClassId(classId);
    }

    @Override
    public List<ClassWithSubjects> getAllClassesWithSubjects() {
        return subjectClassService.getAllClassesWithSubjects();
    }

    @Override
    public List<Subject> assignSubjectsToClass(Integer classId, List<Integer> subjectIds) {
        return subjectClassService.assignSubjectsToClass(classId, subjectIds);
    }

    @Override
    public void removeSubjectsFromClass(Integer classId, List<Integer> subjectIds) {
        subjectClassService.removeSubjectsFromClass(classId, subjectIds);
    }
}

