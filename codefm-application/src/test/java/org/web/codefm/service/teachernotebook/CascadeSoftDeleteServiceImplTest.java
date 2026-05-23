package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentDocumentService;
import org.web.codefm.domain.service.teachernotebook.GroupAssignmentDocumentService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    @Mock
    private ExerciseStudentDocumentService exerciseStudentDocumentService;
    @Mock
    private StudentAbsenceRepository studentAbsenceRepository;
    @Mock
    private SkillRubricRepository skillRubricRepository;
    @Mock
    private SkillRubricCriteriaRepository skillRubricCriteriaRepository;
    @Mock
    private ClassRubricRepository classRubricRepository;
    @Mock
    private StudentClassRubricCriteriaRepository studentClassRubricCriteriaRepository;
    @Mock
    private SavedStudentGroupRepository savedStudentGroupRepository;
    @Mock
    private GroupAssignmentRepository groupAssignmentRepository;
    @Mock
    private GroupAssignmentGradeRepository groupAssignmentGradeRepository;
    @Mock
    private GroupAssignmentDocumentService groupAssignmentDocumentService;

    private CascadeSoftDeleteServiceImpl cascadeSoftDeleteService;

    @BeforeEach
    void beforeEach() {
        this.cascadeSoftDeleteService = new CascadeSoftDeleteServiceImpl(
                this.classRepository,
                this.subjectClassRepository,
                this.scheduleRepository,
                this.studentClassRepository,
                this.exerciseRepository,
                this.exerciseStudentGradeRepository,
                this.exerciseDocumentService,
                this.exerciseStudentDocumentService,
                this.studentAbsenceRepository,
                this.skillRubricRepository,
                this.skillRubricCriteriaRepository,
                this.classRubricRepository,
                this.studentClassRubricCriteriaRepository,
                this.savedStudentGroupRepository,
                this.groupAssignmentRepository,
                this.groupAssignmentGradeRepository,
                this.groupAssignmentDocumentService
        );
    }

    @Nested
    class CascadeDeleteChildrenOfSchool {

        @Test
        void when_school_has_classes_expect_cascade_to_classes_and_soft_delete_them() {
            final Integer schoolId = 1;
            final Integer classId1 = 10;
            final Integer classId2 = 20;

            when(CascadeSoftDeleteServiceImplTest.this.classRepository.findActiveIdsBySchoolId(schoolId)).thenReturn(Arrays.asList(classId1, classId2));
            when(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository.findActiveIdsByClassId(classId1)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository.findActiveIdsByClassId(classId2)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.classRubricRepository.findActiveIdsByClassId(classId1)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.classRubricRepository.findActiveIdsByClassId(classId2)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.savedStudentGroupRepository.findActiveIdsByClassId(classId1)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.savedStudentGroupRepository.findActiveIdsByClassId(classId2)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.groupAssignmentRepository.findActiveIdsByClassId(classId1)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.groupAssignmentRepository.findActiveIdsByClassId(classId2)).thenReturn(Collections.emptyList());

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSchool(schoolId);

            verify(CascadeSoftDeleteServiceImplTest.this.classRepository).findActiveIdsBySchoolId(schoolId);
            verify(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository).findActiveIdsByClassId(classId1);
            verify(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository).softDeleteByClassId(classId1);
            verify(CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository).hardDeleteByClassId(classId1);
            verify(CascadeSoftDeleteServiceImplTest.this.studentClassRepository).softDeleteByClassId(classId1);
            verify(CascadeSoftDeleteServiceImplTest.this.scheduleRepository).softDeleteByClassId(classId1);
            verify(CascadeSoftDeleteServiceImplTest.this.classRubricRepository).softDeleteByClassId(classId1);
            verify(CascadeSoftDeleteServiceImplTest.this.savedStudentGroupRepository).softDeleteByClassId(classId1);
            verify(CascadeSoftDeleteServiceImplTest.this.groupAssignmentRepository).softDeleteByClassId(classId1);
            verify(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository).findActiveIdsByClassId(classId2);
            verify(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository).softDeleteByClassId(classId2);
            verify(CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository).hardDeleteByClassId(classId2);
            verify(CascadeSoftDeleteServiceImplTest.this.studentClassRepository).softDeleteByClassId(classId2);
            verify(CascadeSoftDeleteServiceImplTest.this.scheduleRepository).softDeleteByClassId(classId2);
            verify(CascadeSoftDeleteServiceImplTest.this.classRubricRepository).softDeleteByClassId(classId2);
            verify(CascadeSoftDeleteServiceImplTest.this.savedStudentGroupRepository).softDeleteByClassId(classId2);
            verify(CascadeSoftDeleteServiceImplTest.this.groupAssignmentRepository).softDeleteByClassId(classId2);
            verify(CascadeSoftDeleteServiceImplTest.this.classRepository).softDeleteBySchoolId(schoolId);
        }

        @Test
        void when_school_has_no_classes_expect_do_nothing_for_classes() {
            final Integer schoolId = 1;

            when(CascadeSoftDeleteServiceImplTest.this.classRepository.findActiveIdsBySchoolId(schoolId)).thenReturn(Collections.emptyList());

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSchool(schoolId);

            verify(CascadeSoftDeleteServiceImplTest.this.classRepository).findActiveIdsBySchoolId(schoolId);
            verify(CascadeSoftDeleteServiceImplTest.this.classRepository).softDeleteBySchoolId(schoolId);
            verifyNoInteractions(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository, CascadeSoftDeleteServiceImplTest.this.studentClassRepository, CascadeSoftDeleteServiceImplTest.this.scheduleRepository, CascadeSoftDeleteServiceImplTest.this.exerciseRepository,
                    CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository, CascadeSoftDeleteServiceImplTest.this.exerciseDocumentService, CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository);
        }
    }

    @Nested
    class CascadeDeleteChildrenOfClass {

        @Test
        void when_class_has_subject_classes_expect_cascade_to_subject_classes_and_soft_delete_all() {
            final Integer classId = 10;
            final Integer subjectClassId1 = 100;
            final Integer subjectClassId2 = 200;
            final List<Integer> exerciseIds = Arrays.asList(1000, 1001);

            when(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository.findActiveIdsByClassId(classId))
                    .thenReturn(Arrays.asList(subjectClassId1, subjectClassId2));
            when(CascadeSoftDeleteServiceImplTest.this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId1))).thenReturn(exerciseIds);
            when(CascadeSoftDeleteServiceImplTest.this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId2)))
                    .thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.classRubricRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.savedStudentGroupRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.groupAssignmentRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfClass(classId);

            verify(CascadeSoftDeleteServiceImplTest.this.exerciseStudentDocumentService).deleteDocumentsByExerciseIds(exerciseIds);
            verify(CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository).softDeleteByExerciseIds(exerciseIds);
            verify(CascadeSoftDeleteServiceImplTest.this.exerciseDocumentService).deleteDocumentsByExerciseIds(exerciseIds);
            verify(CascadeSoftDeleteServiceImplTest.this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId1));
            verify(CascadeSoftDeleteServiceImplTest.this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId2));
            verify(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository).softDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository).hardDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.studentClassRepository).softDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.scheduleRepository).softDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.classRubricRepository).softDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.savedStudentGroupRepository).softDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.groupAssignmentRepository).softDeleteByClassId(classId);
        }

        @Test
        void when_class_has_no_subject_classes_expect_soft_delete_children_without_exercises() {
            final Integer classId = 10;

            when(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.classRubricRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.savedStudentGroupRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.groupAssignmentRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfClass(classId);

            verify(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository).findActiveIdsByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository).softDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository).hardDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.studentClassRepository).softDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.scheduleRepository).softDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.classRubricRepository).softDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.savedStudentGroupRepository).softDeleteByClassId(classId);
            verify(CascadeSoftDeleteServiceImplTest.this.groupAssignmentRepository).softDeleteByClassId(classId);
            verifyNoInteractions(CascadeSoftDeleteServiceImplTest.this.exerciseRepository, CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository, CascadeSoftDeleteServiceImplTest.this.exerciseDocumentService);
        }
    }

    @Nested
    class CascadeDeleteChildrenOfSubjectClass {

        @Test
        void when_exercises_exist_expect_soft_delete_exercises_grades_and_documents() {
            final Integer subjectClassId = 100;
            final List<Integer> exerciseIds = Arrays.asList(1000, 1001);

            when(CascadeSoftDeleteServiceImplTest.this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId))).thenReturn(exerciseIds);

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSubjectClass(subjectClassId);

            final var order = inOrder(CascadeSoftDeleteServiceImplTest.this.exerciseStudentDocumentService, CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository, CascadeSoftDeleteServiceImplTest.this.exerciseDocumentService, CascadeSoftDeleteServiceImplTest.this.exerciseRepository,
                    CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository);
            order.verify(CascadeSoftDeleteServiceImplTest.this.exerciseStudentDocumentService).deleteDocumentsByExerciseIds(exerciseIds);
            order.verify(CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository).softDeleteByExerciseIds(exerciseIds);
            order.verify(CascadeSoftDeleteServiceImplTest.this.exerciseDocumentService).deleteDocumentsByExerciseIds(exerciseIds);
            order.verify(CascadeSoftDeleteServiceImplTest.this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId));
            order.verify(CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository).hardDeleteBySubjectClassId(subjectClassId);
        }

        @Test
        void when_no_exercises_exist_expect_only_soft_delete_exercises() {
            final Integer subjectClassId = 100;

            when(CascadeSoftDeleteServiceImplTest.this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId)))
                    .thenReturn(Collections.emptyList());

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSubjectClass(subjectClassId);

            verify(CascadeSoftDeleteServiceImplTest.this.exerciseRepository).findActiveIdsBySubjectClassIds(List.of(subjectClassId));
            verify(CascadeSoftDeleteServiceImplTest.this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId));
            verify(CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository).hardDeleteBySubjectClassId(subjectClassId);
            verifyNoInteractions(CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository, CascadeSoftDeleteServiceImplTest.this.exerciseDocumentService);
        }
    }

    @Nested
    class CascadeDeleteChildrenOfSubject {

        @Test
        void when_subject_has_subject_classes_expect_cascade_to_subject_classes_and_soft_delete_schedules() {
            final Integer subjectId = 5;
            final Integer subjectClassId1 = 100;
            final Integer subjectClassId2 = 200;

            when(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository.findActiveIdsBySubjectId(subjectId))
                    .thenReturn(Arrays.asList(subjectClassId1, subjectClassId2));
            when(CascadeSoftDeleteServiceImplTest.this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId1)))
                    .thenReturn(Collections.emptyList());
            when(CascadeSoftDeleteServiceImplTest.this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId2)))
                    .thenReturn(Collections.emptyList());

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSubject(subjectId);

            verify(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository).findActiveIdsBySubjectId(subjectId);
            verify(CascadeSoftDeleteServiceImplTest.this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId1));
            verify(CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository).hardDeleteBySubjectClassId(subjectClassId1);
            verify(CascadeSoftDeleteServiceImplTest.this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId2));
            verify(CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository).hardDeleteBySubjectClassId(subjectClassId2);
            verify(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository).softDeleteBySubjectId(subjectId);
            verify(CascadeSoftDeleteServiceImplTest.this.scheduleRepository).softDeleteBySubjectId(subjectId);
        }

        @Test
        void when_no_subject_classes_exist_expect_soft_delete_schedules() {
            final Integer subjectId = 5;

            when(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository.findActiveIdsBySubjectId(subjectId)).thenReturn(Collections.emptyList());

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSubject(subjectId);

            verify(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository).findActiveIdsBySubjectId(subjectId);
            verify(CascadeSoftDeleteServiceImplTest.this.subjectClassRepository).softDeleteBySubjectId(subjectId);
            verify(CascadeSoftDeleteServiceImplTest.this.scheduleRepository).softDeleteBySubjectId(subjectId);
            verifyNoInteractions(CascadeSoftDeleteServiceImplTest.this.exerciseRepository, CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository, CascadeSoftDeleteServiceImplTest.this.exerciseDocumentService,
                    CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository);
        }
    }

    @Nested
    class CascadeDeleteChildrenOfExercise {

        @Test
        void when_called_expect_soft_delete_grades_and_documents() {
            final Integer exerciseId = 1000;

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfExercise(exerciseId);

            final var order = inOrder(CascadeSoftDeleteServiceImplTest.this.exerciseStudentDocumentService, CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository, CascadeSoftDeleteServiceImplTest.this.exerciseDocumentService);
            order.verify(CascadeSoftDeleteServiceImplTest.this.exerciseStudentDocumentService).deleteDocumentsByExerciseIds(List.of(exerciseId));
            order.verify(CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository).softDeleteByExerciseIds(List.of(exerciseId));
            order.verify(CascadeSoftDeleteServiceImplTest.this.exerciseDocumentService).deleteDocumentsByExerciseIds(List.of(exerciseId));
        }
    }

    @Nested
    class CascadeDeleteChildrenOfStudent {

        @Test
        void when_called_expect_soft_delete_grades_and_student_classes() {
            final Integer studentId = 7;

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfStudent(studentId);

            final var order = inOrder(CascadeSoftDeleteServiceImplTest.this.exerciseStudentDocumentService, CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository, CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository, CascadeSoftDeleteServiceImplTest.this.studentClassRepository);
            order.verify(CascadeSoftDeleteServiceImplTest.this.exerciseStudentDocumentService).deleteDocumentsByStudentId(studentId);
            order.verify(CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository).softDeleteByStudentId(studentId);
            order.verify(CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository).hardDeleteByStudentId(studentId);
            order.verify(CascadeSoftDeleteServiceImplTest.this.studentClassRepository).softDeleteByStudentId(studentId);
        }
    }

    @Nested
    class CascadeDeleteChildrenOfStudentClass {

        @Test
        void when_student_class_exists_expect_soft_delete_grades_by_student_id_and_class_id() {
            final Integer studentClassId = 50;
            final StudentClass studentClass = StudentClass.builder().id(studentClassId).studentId(7).classId(10).build();

            when(CascadeSoftDeleteServiceImplTest.this.studentClassRepository.findById(studentClassId)).thenReturn(Optional.of(studentClass));

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfStudentClass(studentClassId);

            verify(CascadeSoftDeleteServiceImplTest.this.studentClassRepository).findById(studentClassId);
            verify(CascadeSoftDeleteServiceImplTest.this.exerciseStudentDocumentService).deleteDocumentsByStudentIdAndClassId(7, 10);
            verify(CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository).softDeleteByStudentIdAndClassId(7, 10);
            verify(CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository).deleteByStudentClassId(studentClassId);
        }

        @Test
        void when_student_class_does_not_exist_expect_do_nothing() {
            final Integer studentClassId = 999;

            when(CascadeSoftDeleteServiceImplTest.this.studentClassRepository.findById(studentClassId)).thenReturn(Optional.empty());

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfStudentClass(studentClassId);

            verify(CascadeSoftDeleteServiceImplTest.this.studentClassRepository).findById(studentClassId);
            verifyNoInteractions(CascadeSoftDeleteServiceImplTest.this.exerciseStudentGradeRepository);
            verify(CascadeSoftDeleteServiceImplTest.this.studentAbsenceRepository).deleteByStudentClassId(studentClassId);
        }
    }

    @Nested
    class CascadeDeleteChildrenOfSkill {

        @Test
        void when_rubrics_exist_expect_soft_delete_criteria_and_rubrics() {
            final Integer skillId = 5;

            when(CascadeSoftDeleteServiceImplTest.this.skillRubricRepository.findActiveIdsBySkillId(skillId)).thenReturn(Arrays.asList(100, 200));

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSkill(skillId);

            final var order = inOrder(CascadeSoftDeleteServiceImplTest.this.skillRubricCriteriaRepository, CascadeSoftDeleteServiceImplTest.this.skillRubricRepository);
            order.verify(CascadeSoftDeleteServiceImplTest.this.skillRubricCriteriaRepository).softDeleteByRubricIds(Arrays.asList(100, 200));
            order.verify(CascadeSoftDeleteServiceImplTest.this.skillRubricRepository).softDeleteBySkillId(skillId);
        }

        @Test
        void when_no_rubrics_exist_expect_only_soft_delete_rubrics() {
            final Integer skillId = 5;

            when(CascadeSoftDeleteServiceImplTest.this.skillRubricRepository.findActiveIdsBySkillId(skillId)).thenReturn(Collections.emptyList());

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSkill(skillId);

            verify(CascadeSoftDeleteServiceImplTest.this.skillRubricRepository).findActiveIdsBySkillId(skillId);
            verify(CascadeSoftDeleteServiceImplTest.this.skillRubricRepository).softDeleteBySkillId(skillId);
            verifyNoInteractions(CascadeSoftDeleteServiceImplTest.this.skillRubricCriteriaRepository);
        }
    }

    @Nested
    class CascadeDeleteChildrenOfRubric {

        @Test
        void when_class_rubrics_exist_expect_soft_delete_criteria_and_class_rubrics_with_student_criteria() {
            final Integer rubricId = 100;

            when(CascadeSoftDeleteServiceImplTest.this.classRubricRepository.findActiveIdsByRubricId(rubricId)).thenReturn(Arrays.asList(10, 20));

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfRubric(rubricId);

            final var order = inOrder(CascadeSoftDeleteServiceImplTest.this.skillRubricCriteriaRepository, CascadeSoftDeleteServiceImplTest.this.studentClassRubricCriteriaRepository, CascadeSoftDeleteServiceImplTest.this.classRubricRepository);
            order.verify(CascadeSoftDeleteServiceImplTest.this.skillRubricCriteriaRepository).softDeleteByRubricId(rubricId);
            order.verify(CascadeSoftDeleteServiceImplTest.this.studentClassRubricCriteriaRepository).softDeleteByClassRubricIds(Arrays.asList(10, 20));
            order.verify(CascadeSoftDeleteServiceImplTest.this.classRubricRepository).softDeleteByRubricId(rubricId);
        }

        @Test
        void when_no_class_rubrics_exist_expect_soft_delete_criteria_and_class_rubrics() {
            final Integer rubricId = 100;

            when(CascadeSoftDeleteServiceImplTest.this.classRubricRepository.findActiveIdsByRubricId(rubricId)).thenReturn(Collections.emptyList());

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfRubric(rubricId);

            verify(CascadeSoftDeleteServiceImplTest.this.skillRubricCriteriaRepository).softDeleteByRubricId(rubricId);
            verify(CascadeSoftDeleteServiceImplTest.this.classRubricRepository).findActiveIdsByRubricId(rubricId);
            verify(CascadeSoftDeleteServiceImplTest.this.classRubricRepository).softDeleteByRubricId(rubricId);
            verify(CascadeSoftDeleteServiceImplTest.this.studentClassRubricCriteriaRepository, never()).softDeleteByClassRubricIds(any());
        }
    }

    @Nested
    class CascadeDeleteChildrenOfClassRubric {

        @Test
        void when_called_expect_soft_delete_student_criteria() {
            final Integer classRubricId = 10;

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfClassRubric(classRubricId);

            verify(CascadeSoftDeleteServiceImplTest.this.studentClassRubricCriteriaRepository).softDeleteByClassRubricId(classRubricId);
        }
    }

    @Nested
    class CascadeDeleteChildrenOfSkillRubricCriteria {

        @Test
        void when_called_expect_soft_delete_student_criteria() {
            final Integer criterionId = 50;

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSkillRubricCriteria(criterionId);

            verify(CascadeSoftDeleteServiceImplTest.this.studentClassRubricCriteriaRepository).softDeleteByCriterionId(criterionId);
        }
    }

    @Nested
    class CascadeDeleteChildrenOfGroupAssignment {

        @Test
        void when_called_expect_soft_delete_grades_and_delete_documents() {
            final Integer assignmentId = 100;

            CascadeSoftDeleteServiceImplTest.this.cascadeSoftDeleteService.cascadeDeleteChildrenOfGroupAssignment(assignmentId);

            final var order = inOrder(CascadeSoftDeleteServiceImplTest.this.groupAssignmentGradeRepository, CascadeSoftDeleteServiceImplTest.this.groupAssignmentDocumentService);
            order.verify(CascadeSoftDeleteServiceImplTest.this.groupAssignmentGradeRepository).softDeleteByGroupAssignmentId(assignmentId);
            order.verify(CascadeSoftDeleteServiceImplTest.this.groupAssignmentDocumentService).deleteDocumentsByGroupAssignmentId(assignmentId);
        }
    }
}
