package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookExercisesApi;
import org.web.codefm.api.mapper.ExerciseDTOMapper;
import org.web.codefm.api.mapper.ExerciseRequestMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.usecase.teachernotebook.ExerciseUseCase;
import org.web.codefm.model.ExerciseDTO;
import org.web.codefm.model.ExerciseRequestDTO;
import org.web.codefm.model.ExerciseUpdateRequestDTO;
import org.web.codefm.model.QuarterExercisesDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivateExercises implements TeacherNoteBookExercisesApi {

    private final ExerciseUseCase exerciseUseCase;
    private final ExerciseDTOMapper exerciseDTOMapper;
    private final ExerciseRequestMapper exerciseRequestMapper;

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<QuarterExercisesDTO>> getExercisesByClass(Integer classId, String acceptLanguage) {
        List<Exercise> exercises = exerciseUseCase.getExercisesByClassId(classId);
        return ResponseEntity.ok(exerciseDTOMapper.toGroupedDTOList(exercises));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ExerciseDTO> createExercise(Integer subjectClassId, ExerciseRequestDTO exerciseRequestDTO, String acceptLanguage) {
        Exercise exercise = exerciseRequestMapper.toDomain(exerciseRequestDTO);
        Exercise created = exerciseUseCase.createExercise(subjectClassId, exercise);
        return new ResponseEntity<>(exerciseDTOMapper.toDTO(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ExerciseDTO> updateExercise(Integer id, ExerciseUpdateRequestDTO exerciseUpdateRequestDTO, String acceptLanguage) {
        Exercise exercise = exerciseRequestMapper.toDomainForUpdate(exerciseUpdateRequestDTO);
        Exercise updated = exerciseUseCase.updateExercise(id, exercise);
        return ResponseEntity.ok(exerciseDTOMapper.toDTO(updated));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteExercise(Integer id, String acceptLanguage) {
        exerciseUseCase.deleteExercise(id);
        return ResponseEntity.noContent().build();
    }
}

