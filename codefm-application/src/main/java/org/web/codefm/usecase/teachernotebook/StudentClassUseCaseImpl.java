package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.service.teachernotebook.StudentClassService;
import org.web.codefm.domain.usecase.teachernotebook.StudentClassUseCase;

@Service
@RequiredArgsConstructor
public class StudentClassUseCaseImpl implements StudentClassUseCase {

    private final StudentClassService studentClassService;

    @Override
    public void addStudentToClass(Integer classId, Integer studentId) {
        studentClassService.addStudentToClass(classId, studentId);
    }

    @Override
    public void removeStudentFromClass(Integer classId, Integer studentId) {
        studentClassService.removeStudentFromClass(classId, studentId);
    }
}

