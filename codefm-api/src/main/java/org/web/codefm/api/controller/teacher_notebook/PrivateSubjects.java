package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookSubjectsApi;
import org.web.codefm.api.mapper.SubjectDTOMapper;
import org.web.codefm.api.mapper.SubjectRequestMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.usecase.teachernotebook.SubjectUseCase;
import org.web.codefm.model.SubjectDTO;
import org.web.codefm.model.SubjectRequestDTO;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateSubjects implements TeacherNoteBookSubjectsApi {

    private final SubjectUseCase subjectUseCase;
    private final SubjectDTOMapper subjectDTOMapper;
    private final SubjectRequestMapper subjectRequestMapper;

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SubjectDTO>> subjects() {
        return ResponseEntity.ok(subjectDTOMapper.toDTOList(subjectUseCase.getSubjectsByTeacher()));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SubjectDTO> createSubject(SubjectRequestDTO subjectRequestDTO, String acceptLanguage) {
        Subject createdSubject = subjectUseCase.createSubject(subjectRequestMapper.toDomain(subjectRequestDTO));
        return new ResponseEntity<>(subjectDTOMapper.toDTO(createdSubject), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SubjectDTO> updateSubject(Integer id, SubjectRequestDTO subjectRequestDTO, String acceptLanguage) {
        Subject updatedSubject = subjectUseCase.updateSubject(id, subjectRequestMapper.toDomain(subjectRequestDTO));
        return ResponseEntity.ok(subjectDTOMapper.toDTO(updatedSubject));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteSubject(Integer id, String acceptLanguage) {
        subjectUseCase.softDeleteSubject(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
