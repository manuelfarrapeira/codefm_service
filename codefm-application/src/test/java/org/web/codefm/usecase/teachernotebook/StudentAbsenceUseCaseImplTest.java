package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.domain.service.teachernotebook.StudentAbsenceService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentAbsenceUseCaseImplTest {

	@Mock
	private StudentAbsenceService studentAbsenceService;

	@InjectMocks
	private StudentAbsenceUseCaseImpl studentAbsenceUseCase;

	@Test
	void createAbsences_shouldDelegateToService() {
		Integer classId = 1;
		Integer studentId = 2;
		Integer subjectId = 3;
		LocalDate date = LocalDate.of(2025, 3, 15);
		List<StudentAbsence> expected = List.of(StudentAbsence.builder().id(1).build());

		when(studentAbsenceService.createAbsences(classId, studentId, subjectId, date)).thenReturn(expected);

		List<StudentAbsence> result = studentAbsenceUseCase.createAbsences(classId, studentId, subjectId, date);

		assertEquals(expected, result);
		verify(studentAbsenceService).createAbsences(classId, studentId, subjectId, date);
	}

	@Test
	void getAbsences_shouldDelegateToService() {
		Integer classId = 1;
		Integer studentId = 2;
		LocalDate date = LocalDate.of(2025, 3, 15);
		List<StudentAbsence> expected = List.of(StudentAbsence.builder().id(1).build());

		when(studentAbsenceService.getAbsences(classId, studentId, date)).thenReturn(expected);

		List<StudentAbsence> result = studentAbsenceUseCase.getAbsences(classId, studentId, date);

		assertEquals(expected, result);
		verify(studentAbsenceService).getAbsences(classId, studentId, date);
	}

	@Test
	void deleteAbsence_shouldDelegateToService() {
		Integer id = 1;

		studentAbsenceUseCase.deleteAbsence(id);

		verify(studentAbsenceService).deleteAbsence(id);
	}

	@Test
	void deleteAbsencesByStudentAndDate_shouldDelegateToService() {
		Integer classId = 1;
		Integer studentId = 2;
		LocalDate date = LocalDate.of(2025, 3, 15);

		studentAbsenceUseCase.deleteAbsencesByStudentAndDate(classId, studentId, date);

		verify(studentAbsenceService).deleteAbsencesByStudentAndDate(classId, studentId, date);
	}
}
