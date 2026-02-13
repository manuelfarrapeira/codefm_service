package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookSubjectClassesApi;
import org.web.codefm.api.mapper.ClassWithSubjectsDTOMapper;
import org.web.codefm.api.mapper.SubjectDTOMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.usecase.teachernotebook.SubjectClassUseCase;
import org.web.codefm.model.ClassWithSubjectsDTO;
import org.web.codefm.model.SubjectClassAssignRequestDTO;
import org.web.codefm.model.SubjectClassRemoveRequestDTO;
import org.web.codefm.model.SubjectDTO;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateSubjectClasses implements TeacherNoteBookSubjectClassesApi {

    private final SubjectClassUseCase subjectClassUseCase;
    private final SubjectDTOMapper subjectDTOMapper;
    private final ClassWithSubjectsDTOMapper classWithSubjectsDTOMapper;

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SubjectDTO>> getSubjectsByClass(Integer classId, String acceptLanguage) {
        List<Subject> subjects = subjectClassUseCase.getSubjectsByClassId(classId);
        return ResponseEntity.ok(subjectDTOMapper.toDTOList(subjects));
    }

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ClassWithSubjectsDTO>> getAllClassesWithSubjects() {
        List<ClassWithSubjects> classesWithSubjects = subjectClassUseCase.getAllClassesWithSubjects();
        return ResponseEntity.ok(classWithSubjectsDTOMapper.toDTOList(classesWithSubjects));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SubjectDTO>> assignSubjectsToClass(Integer classId, SubjectClassAssignRequestDTO request, String acceptLanguage) {
        List<Subject> subjects = subjectClassUseCase.assignSubjectsToClass(classId, request.getSubjectIds());
        return new ResponseEntity<>(subjectDTOMapper.toDTOList(subjects), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> removeSubjectsFromClass(Integer classId, SubjectClassRemoveRequestDTO request, String acceptLanguage) {
        subjectClassUseCase.removeSubjectsFromClass(classId, request.getSubjectIds());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

