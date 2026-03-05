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
	private final StudentAbsenceRepository studentAbsenceRepository;

	@Override
	public void cascadeDeleteChildrenOfSchool(Integer schoolId) {
		final List<Integer> classIds = this.classRepository.findActiveIdsBySchoolId(schoolId);
		for (final Integer classId : classIds) {
			this.cascadeDeleteChildrenOfClass(classId);
		}
		this.classRepository.softDeleteBySchoolId(schoolId);
	}

	@Override
	public void cascadeDeleteChildrenOfClass(Integer classId) {
		final List<Integer> subjectClassIds = this.subjectClassRepository.findActiveIdsByClassId(classId);
		for (final Integer subjectClassId : subjectClassIds) {
			this.cascadeDeleteChildrenOfSubjectClass(subjectClassId);
		}
		this.subjectClassRepository.softDeleteByClassId(classId);
		this.studentAbsenceRepository.hardDeleteByClassId(classId);
		this.studentClassRepository.softDeleteByClassId(classId);
		this.scheduleRepository.softDeleteByClassId(classId);
	}

	@Override
	public void cascadeDeleteChildrenOfSubjectClass(Integer subjectClassId) {
		final List<Integer> exerciseIds = this.exerciseRepository
				.findActiveIdsBySubjectClassIds(List.of(subjectClassId));
		if (!exerciseIds.isEmpty()) {
			this.exerciseStudentGradeRepository.softDeleteByExerciseIds(exerciseIds);
			this.exerciseDocumentService.deleteDocumentsByExerciseIds(exerciseIds);
		}
		this.exerciseRepository.softDeleteBySubjectClassIds(List.of(subjectClassId));
		this.studentAbsenceRepository.hardDeleteBySubjectClassId(subjectClassId);
	}

	@Override
	public void cascadeDeleteChildrenOfSubject(Integer subjectId) {
		final List<Integer> subjectClassIds = this.subjectClassRepository.findActiveIdsBySubjectId(subjectId);
		for (final Integer subjectClassId : subjectClassIds) {
			this.cascadeDeleteChildrenOfSubjectClass(subjectClassId);
		}
		this.subjectClassRepository.softDeleteBySubjectId(subjectId);
		this.scheduleRepository.softDeleteBySubjectId(subjectId);
	}

	@Override
	public void cascadeDeleteChildrenOfExercise(Integer exerciseId) {
		this.exerciseStudentGradeRepository.softDeleteByExerciseIds(List.of(exerciseId));
		this.exerciseDocumentService.deleteDocumentsByExerciseIds(List.of(exerciseId));
	}

	@Override
	public void cascadeDeleteChildrenOfStudent(Integer studentId) {
		this.exerciseStudentGradeRepository.softDeleteByStudentId(studentId);
		this.studentAbsenceRepository.hardDeleteByStudentId(studentId);
		this.studentClassRepository.softDeleteByStudentId(studentId);
	}

	@Override
	public void cascadeDeleteChildrenOfStudentClass(Integer studentClassId) {
		this.studentClassRepository.findById(studentClassId)
				.ifPresent(studentClass -> this.exerciseStudentGradeRepository
						.softDeleteByStudentIdAndClassId(studentClass.getStudentId(), studentClass.getClassId()));
		this.studentAbsenceRepository.deleteByStudentClassId(studentClassId);
	}
}
