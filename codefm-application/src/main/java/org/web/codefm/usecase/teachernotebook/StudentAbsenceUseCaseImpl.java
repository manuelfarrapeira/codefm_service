package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.domain.service.teachernotebook.StudentAbsenceService;
import org.web.codefm.domain.usecase.teachernotebook.StudentAbsenceUseCase;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentAbsenceUseCaseImpl implements StudentAbsenceUseCase {

	private final StudentAbsenceService studentAbsenceService;

	@Override
	public List<StudentAbsence> createAbsences(Integer classId, Integer studentId, Integer subjectId, LocalDate date) {
		return this.studentAbsenceService.createAbsences(classId, studentId, subjectId, date);
	}

	@Override
	public List<StudentAbsence> getAbsences(Integer classId, Integer studentId, LocalDate date) {
		return this.studentAbsenceService.getAbsences(classId, studentId, date);
	}

	@Override
	public void deleteAbsence(Integer id) {
		this.studentAbsenceService.deleteAbsence(id);
	}

	@Override
	public void deleteAbsencesByStudentAndDate(Integer classId, Integer studentId, LocalDate date) {
		this.studentAbsenceService.deleteAbsencesByStudentAndDate(classId, studentId, date);
	}
}
