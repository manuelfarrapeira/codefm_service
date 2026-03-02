package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.ExerciseService;
import org.web.codefm.domain.usecase.teachernotebook.ExerciseUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseUseCaseImpl implements ExerciseUseCase {

    private final ExerciseService exerciseService;
    private final CascadeSoftDeleteService cascadeSoftDeleteService;

    @Override
    public List<Exercise> getExercisesByClassId(Integer classId) {
        return exerciseService.getExercisesByClassId(classId);
    }

    @Override
    public Exercise createExercise(Integer subjectClassId, Exercise exercise) {
        return exerciseService.createExercise(subjectClassId, exercise);
    }

    @Override
    public Exercise updateExercise(Integer id, Exercise exercise) {
        return exerciseService.updateExercise(id, exercise);
    }

    @Override
    @Transactional
    public void deleteExercise(Integer id) {
        cascadeSoftDeleteService.cascadeDeleteChildrenOfExercise(id);
        exerciseService.deleteExercise(id);
    }
}

