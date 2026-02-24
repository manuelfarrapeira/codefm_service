package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentGradeService;
import org.web.codefm.domain.usecase.teachernotebook.ExerciseStudentGradeUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseStudentGradeUseCaseImpl implements ExerciseStudentGradeUseCase {

    private final ExerciseStudentGradeService exerciseStudentGradeService;

    @Override
    public List<ExerciseStudentGrade> getGradesByClassId(Integer classId) {
        return exerciseStudentGradeService.getGradesByClassId(classId);
    }

    @Override
    public List<ExerciseStudentGrade> getGradesByClassIdAndStudentId(Integer classId, Integer studentId) {
        return exerciseStudentGradeService.getGradesByClassIdAndStudentId(classId, studentId);
    }

    @Override
    public ExerciseStudentGrade createGrade(Integer exerciseId, ExerciseStudentGrade grade) {
        return exerciseStudentGradeService.createGrade(exerciseId, grade);
    }

    @Override
    public ExerciseStudentGrade updateGrade(Integer id, ExerciseStudentGrade grade) {
        return exerciseStudentGradeService.updateGrade(id, grade);
    }

    @Override
    public void deleteGrade(Integer id) {
        exerciseStudentGradeService.deleteGrade(id);
    }
}

