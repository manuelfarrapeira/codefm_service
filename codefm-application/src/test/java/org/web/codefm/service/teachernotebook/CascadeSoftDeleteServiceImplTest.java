package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentDocumentService;

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

	@InjectMocks
	private CascadeSoftDeleteServiceImpl cascadeSoftDeleteService;

	@Test
	void cascadeDeleteChildrenOfSchool_shouldCascadeToClassesAndSoftDeleteThem() {
		final Integer schoolId = 1;
		final Integer classId1 = 10;
		final Integer classId2 = 20;

		when(this.classRepository.findActiveIdsBySchoolId(schoolId)).thenReturn(Arrays.asList(classId1, classId2));
		when(this.subjectClassRepository.findActiveIdsByClassId(classId1)).thenReturn(Collections.emptyList());
		when(this.subjectClassRepository.findActiveIdsByClassId(classId2)).thenReturn(Collections.emptyList());
        when(this.classRubricRepository.findActiveIdsByClassId(classId1)).thenReturn(Collections.emptyList());
        when(this.classRubricRepository.findActiveIdsByClassId(classId2)).thenReturn(Collections.emptyList());

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSchool(schoolId);

		verify(this.classRepository).findActiveIdsBySchoolId(schoolId);
		verify(this.subjectClassRepository).findActiveIdsByClassId(classId1);
		verify(this.subjectClassRepository).softDeleteByClassId(classId1);
		verify(this.studentAbsenceRepository).hardDeleteByClassId(classId1);
		verify(this.studentClassRepository).softDeleteByClassId(classId1);
		verify(this.scheduleRepository).softDeleteByClassId(classId1);
        verify(this.classRubricRepository).softDeleteByClassId(classId1);
		verify(this.subjectClassRepository).findActiveIdsByClassId(classId2);
		verify(this.subjectClassRepository).softDeleteByClassId(classId2);
		verify(this.studentAbsenceRepository).hardDeleteByClassId(classId2);
		verify(this.studentClassRepository).softDeleteByClassId(classId2);
		verify(this.scheduleRepository).softDeleteByClassId(classId2);
        verify(this.classRubricRepository).softDeleteByClassId(classId2);
		verify(this.classRepository).softDeleteBySchoolId(schoolId);
	}

	@Test
	void cascadeDeleteChildrenOfSchool_shouldDoNothingForClasses_whenSchoolHasNone() {
		final Integer schoolId = 1;

		when(this.classRepository.findActiveIdsBySchoolId(schoolId)).thenReturn(Collections.emptyList());

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSchool(schoolId);

		verify(this.classRepository).findActiveIdsBySchoolId(schoolId);
		verify(this.classRepository).softDeleteBySchoolId(schoolId);
		verifyNoInteractions(this.subjectClassRepository, this.studentClassRepository, this.scheduleRepository, this.exerciseRepository,
                this.exerciseStudentGradeRepository, this.exerciseDocumentService, this.studentAbsenceRepository);
	}

	@Test
	void cascadeDeleteChildrenOfClass_shouldCascadeToSubjectClassesAndSoftDeleteAll() {
		final Integer classId = 10;
		final Integer subjectClassId1 = 100;
		final Integer subjectClassId2 = 200;
		final List<Integer> exerciseIds = Arrays.asList(1000, 1001);

		when(this.subjectClassRepository.findActiveIdsByClassId(classId))
				.thenReturn(Arrays.asList(subjectClassId1, subjectClassId2));
		when(this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId1))).thenReturn(exerciseIds);
		when(this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId2)))
				.thenReturn(Collections.emptyList());
        when(this.classRubricRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfClass(classId);

		verify(this.exerciseStudentDocumentService).deleteDocumentsByExerciseIds(exerciseIds);
		verify(this.exerciseStudentGradeRepository).softDeleteByExerciseIds(exerciseIds);
		verify(this.exerciseDocumentService).deleteDocumentsByExerciseIds(exerciseIds);
		verify(this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId1));
		verify(this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId2));
		verify(this.subjectClassRepository).softDeleteByClassId(classId);
		verify(this.studentAbsenceRepository).hardDeleteByClassId(classId);
		verify(this.studentClassRepository).softDeleteByClassId(classId);
		verify(this.scheduleRepository).softDeleteByClassId(classId);
        verify(this.classRubricRepository).softDeleteByClassId(classId);
	}

	@Test
	void cascadeDeleteChildrenOfClass_shouldSoftDeleteChildrenWithoutExercises_whenNoSubjectClasses() {
		final Integer classId = 10;

		when(this.subjectClassRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());
        when(this.classRubricRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfClass(classId);

		verify(this.subjectClassRepository).findActiveIdsByClassId(classId);
		verify(this.subjectClassRepository).softDeleteByClassId(classId);
		verify(this.studentAbsenceRepository).hardDeleteByClassId(classId);
		verify(this.studentClassRepository).softDeleteByClassId(classId);
		verify(this.scheduleRepository).softDeleteByClassId(classId);
        verify(this.classRubricRepository).softDeleteByClassId(classId);
		verifyNoInteractions(this.exerciseRepository, this.exerciseStudentGradeRepository, this.exerciseDocumentService);
	}

	@Test
	void cascadeDeleteChildrenOfSubjectClass_shouldSoftDeleteExercisesGradesAndDocuments() {
		final Integer subjectClassId = 100;
		final List<Integer> exerciseIds = Arrays.asList(1000, 1001);

		when(this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId))).thenReturn(exerciseIds);

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSubjectClass(subjectClassId);

		final var order = inOrder(this.exerciseStudentDocumentService, this.exerciseStudentGradeRepository, this.exerciseDocumentService, this.exerciseRepository,
                this.studentAbsenceRepository);
		order.verify(this.exerciseStudentDocumentService).deleteDocumentsByExerciseIds(exerciseIds);
		order.verify(this.exerciseStudentGradeRepository).softDeleteByExerciseIds(exerciseIds);
		order.verify(this.exerciseDocumentService).deleteDocumentsByExerciseIds(exerciseIds);
		order.verify(this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId));
		order.verify(this.studentAbsenceRepository).hardDeleteBySubjectClassId(subjectClassId);
	}

	@Test
	void cascadeDeleteChildrenOfSubjectClass_shouldOnlySoftDeleteExercises_whenNoExercisesExist() {
		final Integer subjectClassId = 100;

		when(this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId)))
				.thenReturn(Collections.emptyList());

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSubjectClass(subjectClassId);

		verify(this.exerciseRepository).findActiveIdsBySubjectClassIds(List.of(subjectClassId));
		verify(this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId));
		verify(this.studentAbsenceRepository).hardDeleteBySubjectClassId(subjectClassId);
		verifyNoInteractions(this.exerciseStudentGradeRepository, this.exerciseDocumentService);
	}

	@Test
	void cascadeDeleteChildrenOfSubject_shouldCascadeToSubjectClassesAndSoftDeleteSchedules() {
		final Integer subjectId = 5;
		final Integer subjectClassId1 = 100;
		final Integer subjectClassId2 = 200;

		when(this.subjectClassRepository.findActiveIdsBySubjectId(subjectId))
				.thenReturn(Arrays.asList(subjectClassId1, subjectClassId2));
		when(this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId1)))
				.thenReturn(Collections.emptyList());
		when(this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of(subjectClassId2)))
				.thenReturn(Collections.emptyList());

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSubject(subjectId);

		verify(this.subjectClassRepository).findActiveIdsBySubjectId(subjectId);
		verify(this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId1));
		verify(this.studentAbsenceRepository).hardDeleteBySubjectClassId(subjectClassId1);
		verify(this.exerciseRepository).softDeleteBySubjectClassIds(List.of(subjectClassId2));
		verify(this.studentAbsenceRepository).hardDeleteBySubjectClassId(subjectClassId2);
		verify(this.subjectClassRepository).softDeleteBySubjectId(subjectId);
		verify(this.scheduleRepository).softDeleteBySubjectId(subjectId);
	}

	@Test
	void cascadeDeleteChildrenOfSubject_shouldSoftDeleteSchedules_whenNoSubjectClassesExist() {
		final Integer subjectId = 5;

		when(this.subjectClassRepository.findActiveIdsBySubjectId(subjectId)).thenReturn(Collections.emptyList());

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSubject(subjectId);

		verify(this.subjectClassRepository).findActiveIdsBySubjectId(subjectId);
		verify(this.subjectClassRepository).softDeleteBySubjectId(subjectId);
		verify(this.scheduleRepository).softDeleteBySubjectId(subjectId);
		verifyNoInteractions(this.exerciseRepository, this.exerciseStudentGradeRepository, this.exerciseDocumentService,
                this.studentAbsenceRepository);
	}

	@Test
	void cascadeDeleteChildrenOfExercise_shouldSoftDeleteGradesAndDocuments() {
		final Integer exerciseId = 1000;

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfExercise(exerciseId);

		final var order = inOrder(this.exerciseStudentDocumentService, this.exerciseStudentGradeRepository, this.exerciseDocumentService);
		order.verify(this.exerciseStudentDocumentService).deleteDocumentsByExerciseIds(List.of(exerciseId));
		order.verify(this.exerciseStudentGradeRepository).softDeleteByExerciseIds(List.of(exerciseId));
		order.verify(this.exerciseDocumentService).deleteDocumentsByExerciseIds(List.of(exerciseId));
	}

	@Test
	void cascadeDeleteChildrenOfStudent_shouldSoftDeleteGradesAndStudentClasses() {
		final Integer studentId = 7;

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfStudent(studentId);

		final var order = inOrder(this.exerciseStudentDocumentService, this.exerciseStudentGradeRepository, this.studentAbsenceRepository, this.studentClassRepository);
		order.verify(this.exerciseStudentDocumentService).deleteDocumentsByStudentId(studentId);
		order.verify(this.exerciseStudentGradeRepository).softDeleteByStudentId(studentId);
		order.verify(this.studentAbsenceRepository).hardDeleteByStudentId(studentId);
		order.verify(this.studentClassRepository).softDeleteByStudentId(studentId);
	}

	@Test
	void cascadeDeleteChildrenOfStudentClass_shouldSoftDeleteGradesByStudentIdAndClassId_whenStudentClassExists() {
		final Integer studentClassId = 50;
		final StudentClass studentClass = StudentClass.builder().id(studentClassId).studentId(7).classId(10).build();

		when(this.studentClassRepository.findById(studentClassId)).thenReturn(Optional.of(studentClass));

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfStudentClass(studentClassId);

		verify(this.studentClassRepository).findById(studentClassId);
		verify(this.exerciseStudentDocumentService).deleteDocumentsByStudentId(7);
		verify(this.exerciseStudentGradeRepository).softDeleteByStudentIdAndClassId(7, 10);
		verify(this.studentAbsenceRepository).deleteByStudentClassId(studentClassId);
	}

	@Test
	void cascadeDeleteChildrenOfStudentClass_shouldDoNothing_whenStudentClassDoesNotExist() {
		final Integer studentClassId = 999;

		when(this.studentClassRepository.findById(studentClassId)).thenReturn(Optional.empty());

        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfStudentClass(studentClassId);

		verify(this.studentClassRepository).findById(studentClassId);
		verifyNoInteractions(this.exerciseStudentGradeRepository);
		verify(this.studentAbsenceRepository).deleteByStudentClassId(studentClassId);
	}

	@Test
	void cascadeDeleteChildrenOfSkill_shouldSoftDeleteCriteriaAndRubrics_whenRubricsExist() {
		final Integer skillId = 5;

		when(this.skillRubricRepository.findActiveIdsBySkillId(skillId)).thenReturn(Arrays.asList(100, 200));

		this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSkill(skillId);

		final var order = inOrder(this.skillRubricCriteriaRepository, this.skillRubricRepository);
		order.verify(this.skillRubricCriteriaRepository).softDeleteByRubricIds(Arrays.asList(100, 200));
		order.verify(this.skillRubricRepository).softDeleteBySkillId(skillId);
	}

	@Test
	void cascadeDeleteChildrenOfSkill_shouldOnlySoftDeleteRubrics_whenNoRubricsExist() {
		final Integer skillId = 5;

		when(this.skillRubricRepository.findActiveIdsBySkillId(skillId)).thenReturn(Collections.emptyList());

		this.cascadeSoftDeleteService.cascadeDeleteChildrenOfSkill(skillId);

		verify(this.skillRubricRepository).findActiveIdsBySkillId(skillId);
		verify(this.skillRubricRepository).softDeleteBySkillId(skillId);
		verifyNoInteractions(this.skillRubricCriteriaRepository);
	}

	@Test
	void cascadeDeleteChildrenOfRubric_shouldSoftDeleteCriteriaAndClassRubricsWithStudentCriteria_whenClassRubricsExist() {
		final Integer rubricId = 100;

		when(this.classRubricRepository.findActiveIdsByRubricId(rubricId)).thenReturn(Arrays.asList(10, 20));

		this.cascadeSoftDeleteService.cascadeDeleteChildrenOfRubric(rubricId);

		final var order = inOrder(this.skillRubricCriteriaRepository, this.studentClassRubricCriteriaRepository, this.classRubricRepository);
		order.verify(this.skillRubricCriteriaRepository).softDeleteByRubricId(rubricId);
		order.verify(this.studentClassRubricCriteriaRepository).softDeleteByClassRubricIds(Arrays.asList(10, 20));
		order.verify(this.classRubricRepository).softDeleteByRubricId(rubricId);
	}

	@Test
	void cascadeDeleteChildrenOfRubric_shouldSoftDeleteCriteriaAndClassRubrics_whenNoClassRubricsExist() {
		final Integer rubricId = 100;

		when(this.classRubricRepository.findActiveIdsByRubricId(rubricId)).thenReturn(Collections.emptyList());

		this.cascadeSoftDeleteService.cascadeDeleteChildrenOfRubric(rubricId);

		verify(this.skillRubricCriteriaRepository).softDeleteByRubricId(rubricId);
		verify(this.classRubricRepository).findActiveIdsByRubricId(rubricId);
		verify(this.classRubricRepository).softDeleteByRubricId(rubricId);
		verify(this.studentClassRubricCriteriaRepository, never()).softDeleteByClassRubricIds(any());
	}

	@Test
	void cascadeDeleteChildrenOfClassRubric_shouldSoftDeleteStudentCriteria() {
		final Integer classRubricId = 10;

		this.cascadeSoftDeleteService.cascadeDeleteChildrenOfClassRubric(classRubricId);

		verify(this.studentClassRubricCriteriaRepository).softDeleteByClassRubricId(classRubricId);
	}
}
