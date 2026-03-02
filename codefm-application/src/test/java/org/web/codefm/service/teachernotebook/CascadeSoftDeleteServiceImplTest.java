package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CascadeSoftDeleteServiceImplTest {

    @Mock
    private ClassRepository classRepository;
    @Mock
    private SubjectClassRepository subjectClassRepository;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private StudentClassRepository studentClassRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private ExerciseStudentGradeRepository exerciseStudentGradeRepository;
    @Mock
    private ExerciseDocumentService exerciseDocumentService;

    @InjectMocks
    private CascadeSoftDeleteServiceImpl cascadeSoftDeleteService;

    @Test
    void cascadeDeleteChildrenOfSchool_shouldCascadeToClassesAndSoftDeleteThem() {
        Integer schoolId = 1;
        Integer classId1 = 10;
        Integer classId2 = 20;

        when(classRepository.findActiveIdsBySchoolId(schoolId)).thenReturn(Arrays.asList(classId1, classId2));
        when(subjectClassRepository.findActiveIdsByClassId(classId1)).thenReturn(Collections.emptyList());
        when(subjectClassRepository.findActiveIdsByClassId(classId2)).thenReturn(Collections.emptyList());

        cascadeSoftDeleteService.cascadeDeleteChildrenOfSchool(schoolId);

        verify(classRepository).findActiveIdsBySchoolId(schoolId);
        verify(subjectClassRepository).findActiveIdsByClassId(classId1);
        verify(subjectClassRepository).softDeleteByClassId(classId1);
        verify(studentClassRepository).softDeleteByClassId(classId1);
        verify(scheduleRepository).softDeleteByClassId(classId1);
        verify(subjectClassRepository).findActiveIdsByClassId(classId2);
        verify(subjectClassRepository).softDeleteByClassId(classId2);
        verify(studentClassRepository).softDeleteByClassId(classId2);
        verify(scheduleRepository).softDeleteByClassId(classId2);
        verify(classRepository).softDeleteBySchoolId(schoolId);
    }

    @Test
    void cascadeDeleteChildrenOfSchool_shouldDoNothingForClasses_whenSchoolHasNone() {
        Integer schoolId = 1;

        when(classRepository.findActiveIdsBySchoolId(schoolId)).thenReturn(Collections.emptyList());

        cascadeSoftDeleteService.cascadeDeleteChildrenOfSchool(schoolId);

        verify(classRepository).findActiveIdsBySchoolId(schoolId);
        verify(classRepository).softDeleteBySchoolId(schoolId);
        verifyNoInteractions(subjectClassRepository, studentClassRepository, scheduleRepository,
                exerciseRepository, exerciseStudentGradeRepository, exerciseDocumentService);
    }

    @Test
    void cascadeDeleteChildrenOfClass_shouldCascadeToSubjectClassesAndSoftDeleteAll() {
        Integer classId = 10;
        Integer subjectClassId1 = 100;
        Integer subjectClassId2 = 200;
        List<Integer> exerciseIds = Arrays.asList(1000, 1001);

        when(subjectClassRepository.findActiveIdsByClassId(classId)).thenReturn(Arrays.asList(subjectClassId1, subjectClassId2));
        when(exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId1))).thenReturn(exerciseIds);
        when(exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId2))).thenReturn(Collections.emptyList());

        cascadeSoftDeleteService.cascadeDeleteChildrenOfClass(classId);

        verify(exerciseStudentGradeRepository).softDeleteByExerciseIds(exerciseIds);
        verify(exerciseDocumentService).deleteDocumentsByExerciseIds(exerciseIds);
        verify(exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId1));
        verify(exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId2));
        verify(subjectClassRepository).softDeleteByClassId(classId);
        verify(studentClassRepository).softDeleteByClassId(classId);
        verify(scheduleRepository).softDeleteByClassId(classId);
    }

    @Test
    void cascadeDeleteChildrenOfClass_shouldSoftDeleteChildrenWithoutExercises_whenNoSubjectClasses() {
        Integer classId = 10;

        when(subjectClassRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());

        cascadeSoftDeleteService.cascadeDeleteChildrenOfClass(classId);

        verify(subjectClassRepository).findActiveIdsByClassId(classId);
        verify(subjectClassRepository).softDeleteByClassId(classId);
        verify(studentClassRepository).softDeleteByClassId(classId);
        verify(scheduleRepository).softDeleteByClassId(classId);
        verifyNoInteractions(exerciseRepository, exerciseStudentGradeRepository, exerciseDocumentService);
    }

    @Test
    void cascadeDeleteChildrenOfSubjectClass_shouldSoftDeleteExercisesGradesAndDocuments() {
        Integer subjectClassId = 100;
        List<Integer> exerciseIds = Arrays.asList(1000, 1001);

        when(exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId))).thenReturn(exerciseIds);

        cascadeSoftDeleteService.cascadeDeleteChildrenOfSubjectClass(subjectClassId);

        var order = inOrder(exerciseStudentGradeRepository, exerciseDocumentService, exerciseRepository);
        order.verify(exerciseStudentGradeRepository).softDeleteByExerciseIds(exerciseIds);
        order.verify(exerciseDocumentService).deleteDocumentsByExerciseIds(exerciseIds);
        order.verify(exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId));
    }

    @Test
    void cascadeDeleteChildrenOfSubjectClass_shouldOnlySoftDeleteExercises_whenNoExercisesExist() {
        Integer subjectClassId = 100;

        when(exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId))).thenReturn(Collections.emptyList());

        cascadeSoftDeleteService.cascadeDeleteChildrenOfSubjectClass(subjectClassId);

        verify(exerciseRepository).findActiveIdsBySubjectClassIds(List.of(subjectClassId));
        verify(exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId));
        verifyNoInteractions(exerciseStudentGradeRepository, exerciseDocumentService);
    }

    @Test
    void cascadeDeleteChildrenOfSubject_shouldCascadeToSubjectClassesAndSoftDeleteSchedules() {
        Integer subjectId = 5;
        Integer subjectClassId1 = 100;
        Integer subjectClassId2 = 200;

        when(subjectClassRepository.findActiveIdsBySubjectId(subjectId)).thenReturn(Arrays.asList(subjectClassId1, subjectClassId2));
        when(exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId1))).thenReturn(Collections.emptyList());
        when(exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId2))).thenReturn(Collections.emptyList());

        cascadeSoftDeleteService.cascadeDeleteChildrenOfSubject(subjectId);

        verify(subjectClassRepository).findActiveIdsBySubjectId(subjectId);
        verify(exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId1));
        verify(exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId2));
        verify(subjectClassRepository).softDeleteBySubjectId(subjectId);
        verify(scheduleRepository).softDeleteBySubjectId(subjectId);
    }

    @Test
    void cascadeDeleteChildrenOfSubject_shouldSoftDeleteSchedules_whenNoSubjectClassesExist() {
        Integer subjectId = 5;

        when(subjectClassRepository.findActiveIdsBySubjectId(subjectId)).thenReturn(Collections.emptyList());

        cascadeSoftDeleteService.cascadeDeleteChildrenOfSubject(subjectId);

        verify(subjectClassRepository).findActiveIdsBySubjectId(subjectId);
        verify(subjectClassRepository).softDeleteBySubjectId(subjectId);
        verify(scheduleRepository).softDeleteBySubjectId(subjectId);
        verifyNoInteractions(exerciseRepository, exerciseStudentGradeRepository, exerciseDocumentService);
    }

    @Test
    void cascadeDeleteChildrenOfExercise_shouldSoftDeleteGradesAndDocuments() {
        Integer exerciseId = 1000;

        cascadeSoftDeleteService.cascadeDeleteChildrenOfExercise(exerciseId);

        var order = inOrder(exerciseStudentGradeRepository, exerciseDocumentService);
        order.verify(exerciseStudentGradeRepository).softDeleteByExerciseIds(List.of(exerciseId));
        order.verify(exerciseDocumentService).deleteDocumentsByExerciseIds(List.of(exerciseId));
    }

    @Test
    void cascadeDeleteChildrenOfStudent_shouldSoftDeleteGradesAndStudentClasses() {
        Integer studentId = 7;

        cascadeSoftDeleteService.cascadeDeleteChildrenOfStudent(studentId);

        var order = inOrder(exerciseStudentGradeRepository, studentClassRepository);
        order.verify(exerciseStudentGradeRepository).softDeleteByStudentId(studentId);
        order.verify(studentClassRepository).softDeleteByStudentId(studentId);
    }
}

