package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookExerciseStudentGradesApi;
import org.web.codefm.api.mapper.ExerciseStudentGradeDTOMapper;
import org.web.codefm.api.mapper.ExerciseStudentGradeRequestMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.domain.usecase.teachernotebook.ExerciseStudentGradeUseCase;
import org.web.codefm.model.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivateExerciseStudentGrades implements TeacherNoteBookExerciseStudentGradesApi {

    private final ExerciseStudentGradeUseCase exerciseStudentGradeUseCase;
    private final ExerciseStudentGradeDTOMapper exerciseStudentGradeDTOMapper;
    private final ExerciseStudentGradeRequestMapper exerciseStudentGradeRequestMapper;

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<StudentGradesDTO>> getGradesByClass(Integer classId, String acceptLanguage) {
        List<ExerciseStudentGrade> grades = exerciseStudentGradeUseCase.getGradesByClassId(classId);
        return ResponseEntity.ok(exerciseStudentGradeDTOMapper.toGroupedByStudentDTOList(grades));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<QuarterGradesDTO>> getGradesByClassAndStudent(Integer classId, Integer studentId, String acceptLanguage) {
        List<ExerciseStudentGrade> grades = exerciseStudentGradeUseCase.getGradesByClassIdAndStudentId(classId, studentId);
        return ResponseEntity.ok(exerciseStudentGradeDTOMapper.toGroupedDTOList(grades));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ExerciseStudentGradeDTO> createGrade(Integer exerciseId, ExerciseStudentGradeRequestDTO requestDTO, String acceptLanguage) {
        ExerciseStudentGrade grade = exerciseStudentGradeRequestMapper.toDomain(requestDTO);
        ExerciseStudentGrade created = exerciseStudentGradeUseCase.createGrade(exerciseId, grade);
        return new ResponseEntity<>(exerciseStudentGradeDTOMapper.toDTO(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ExerciseStudentGradeDTO> updateGrade(Integer id, ExerciseStudentGradeUpdateRequestDTO requestDTO, String acceptLanguage) {
        ExerciseStudentGrade grade = exerciseStudentGradeRequestMapper.toDomainForUpdate(requestDTO);
        ExerciseStudentGrade updated = exerciseStudentGradeUseCase.updateGrade(id, grade);
        return ResponseEntity.ok(exerciseStudentGradeDTOMapper.toDTO(updated));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteGrade(Integer id, String acceptLanguage) {
        exerciseStudentGradeUseCase.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }
}

