package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CascadeSoftDeleteServiceImpl implements CascadeSoftDeleteService {

    private final ClassRepository classRepository;
    private final SubjectClassRepository subjectClassRepository;
    private final ScheduleRepository scheduleRepository;
    private final StudentClassRepository studentClassRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseStudentGradeRepository exerciseStudentGradeRepository;
    private final ExerciseDocumentService exerciseDocumentService;

    @Override
    public void cascadeDeleteChildrenOfSchool(Integer schoolId) {
        List<Integer> classIds = classRepository.findActiveIdsBySchoolId(schoolId);
        for (Integer classId : classIds) {
            cascadeDeleteChildrenOfClass(classId);
        }
        classRepository.softDeleteBySchoolId(schoolId);
    }

    @Override
    public void cascadeDeleteChildrenOfClass(Integer classId) {
        List<Integer> subjectClassIds = subjectClassRepository.findActiveIdsByClassId(classId);
        for (Integer subjectClassId : subjectClassIds) {
            cascadeDeleteChildrenOfSubjectClass(subjectClassId);
        }
        subjectClassRepository.softDeleteByClassId(classId);
        studentClassRepository.softDeleteByClassId(classId);
        scheduleRepository.softDeleteByClassId(classId);
    }

    @Override
    public void cascadeDeleteChildrenOfSubjectClass(Integer subjectClassId) {
        List<Integer> exerciseIds = exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId));
        if (!exerciseIds.isEmpty()) {
            exerciseStudentGradeRepository.softDeleteByExerciseIds(exerciseIds);
            exerciseDocumentService.deleteDocumentsByExerciseIds(exerciseIds);
        }
        exerciseRepository.softDeleteBySubjectClassIds(List.of(subjectClassId));
    }

    @Override
    public void cascadeDeleteChildrenOfSubject(Integer subjectId) {
        List<Integer> subjectClassIds = subjectClassRepository.findActiveIdsBySubjectId(subjectId);
        for (Integer subjectClassId : subjectClassIds) {
            cascadeDeleteChildrenOfSubjectClass(subjectClassId);
        }
        subjectClassRepository.softDeleteBySubjectId(subjectId);
        scheduleRepository.softDeleteBySubjectId(subjectId);
    }

    @Override
    public void cascadeDeleteChildrenOfExercise(Integer exerciseId) {
        exerciseStudentGradeRepository.softDeleteByExerciseIds(List.of(exerciseId));
        exerciseDocumentService.deleteDocumentsByExerciseIds(List.of(exerciseId));
    }

    @Override
    public void cascadeDeleteChildrenOfStudent(Integer studentId) {
        exerciseStudentGradeRepository.softDeleteByStudentId(studentId);
        studentClassRepository.softDeleteByStudentId(studentId);
    }

    @Override
    public void cascadeDeleteChildrenOfStudentClass(Integer studentClassId) {
        studentClassRepository.findById(studentClassId).ifPresent(studentClass ->
                exerciseStudentGradeRepository.softDeleteByStudentIdAndClassId(
                        studentClass.getStudentId(), studentClass.getClassId()
                )
        );
    }
}

