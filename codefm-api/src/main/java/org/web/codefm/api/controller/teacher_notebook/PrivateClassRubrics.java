package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookClassRubricsApi;
import org.web.codefm.api.mapper.ClassRubricDTOMapper;
import org.web.codefm.api.mapper.StudentClassRubricCriteriaDTOMapper;
import org.web.codefm.api.mapper.StudentCriteriaGroupDTOMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.domain.entity.teachernotebook.StudentCriteriaGroup;
import org.web.codefm.domain.usecase.teachernotebook.ClassRubricUseCase;
import org.web.codefm.model.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivateClassRubrics implements TeacherNoteBookClassRubricsApi {

    private final ClassRubricUseCase classRubricUseCase;
    private final ClassRubricDTOMapper classRubricDTOMapper;
    private final StudentClassRubricCriteriaDTOMapper studentClassRubricCriteriaDTOMapper;
    private final StudentCriteriaGroupDTOMapper studentCriteriaGroupDTOMapper;

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ClassRubricDTO>> getRubricsByClass(Integer classId) {
        final List<ClassRubric> rubrics = this.classRubricUseCase.getRubricsByClassId(classId);
        return ResponseEntity.ok(this.classRubricDTOMapper.toDTOList(rubrics));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ClassRubricDTO> assignRubricToClass(Integer classId, ClassRubricRequestDTO classRubricRequestDTO, String acceptLanguage) {
        final ClassRubric assigned = this.classRubricUseCase.assignRubricToClass(classId, classRubricRequestDTO.getRubricId());
        return new ResponseEntity<>(this.classRubricDTOMapper.toDTO(assigned), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> removeRubricFromClass(Integer classRubricId, String acceptLanguage) {
        this.classRubricUseCase.removeRubricFromClass(classRubricId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<StudentCriteriaGroupDTO>> getAllStudentCriteriaByClass(Integer classId) {
        final List<StudentCriteriaGroup> groups = this.classRubricUseCase.getAllStudentCriteriaByClassId(classId);
        return ResponseEntity.ok(this.studentCriteriaGroupDTOMapper.toDTOList(groups));
    }

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<StudentCriteriaGroupDTO>> getStudentCriteriaByStudent(Integer classId, Integer studentId) {
        final List<StudentCriteriaGroup> groups = this.classRubricUseCase.getStudentCriteriaByClassAndStudent(classId, studentId);
        return ResponseEntity.ok(this.studentCriteriaGroupDTOMapper.toDTOList(groups));
    }

    @Logged
    @Override
    @Locale(3)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<StudentClassRubricCriteriaDTO> assignCriterionToStudent(Integer classRubricId, Integer studentId, StudentClassRubricCriteriaRequestDTO studentClassRubricCriteriaRequestDTO, String acceptLanguage) {
        final StudentClassRubricCriteria created = this.classRubricUseCase.assignCriterionToStudent(classRubricId, studentId, studentClassRubricCriteriaRequestDTO.getCriterionId());
        return new ResponseEntity<>(this.studentClassRubricCriteriaDTOMapper.toDTO(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<StudentClassRubricCriteriaDTO> updateStudentCriterion(Integer id, StudentClassRubricCriteriaRequestDTO studentClassRubricCriteriaRequestDTO, String acceptLanguage) {
        final StudentClassRubricCriteria updated = this.classRubricUseCase.updateStudentCriterion(id, studentClassRubricCriteriaRequestDTO.getCriterionId());
        return ResponseEntity.ok(this.studentClassRubricCriteriaDTOMapper.toDTO(updated));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> removeStudentCriterion(Integer id, String acceptLanguage) {
        this.classRubricUseCase.removeStudentCriterion(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
