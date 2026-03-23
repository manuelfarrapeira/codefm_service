package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookStudentGroupsApi;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.usecase.teachernotebook.StudentGroupUseCase;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivateStudentGroups implements TeacherNoteBookStudentGroupsApi {

    private final StudentGroupUseCase studentGroupUseCase;

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<List<Integer>>> generateStudentGroups(Integer classId, Boolean prioritizeShapeDiversity, String acceptLanguage) {
        final List<List<Integer>> groups = this.studentGroupUseCase.generateGroups(classId, prioritizeShapeDiversity);
        return ResponseEntity.ok(groups);
    }
}
