package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookStudentAbsencesApi;
import org.web.codefm.api.mapper.StudentAbsenceDTOMapper;
import org.web.codefm.api.mapper.StudentAbsenceRequestMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.domain.usecase.teachernotebook.StudentAbsenceUseCase;
import org.web.codefm.model.StudentAbsenceDTO;
import org.web.codefm.model.StudentAbsenceRequestDTO;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateStudentAbsences implements TeacherNoteBookStudentAbsencesApi {

	private final StudentAbsenceUseCase studentAbsenceUseCase;
	private final StudentAbsenceDTOMapper studentAbsenceDTOMapper;
	private final StudentAbsenceRequestMapper studentAbsenceRequestMapper;

	@Logged
	@Override
	@Locale(2)
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<List<StudentAbsenceDTO>> createAbsences(Integer classId, StudentAbsenceRequestDTO dto,
			String acceptLanguage) {
		LocalDate date = studentAbsenceRequestMapper.parseDate(dto.getDate());
		List<StudentAbsence> absences = studentAbsenceUseCase.createAbsences(classId, dto.getStudentId(),
				dto.getSubjectId(), date);
		return new ResponseEntity<>(studentAbsenceDTOMapper.toDTOList(absences), HttpStatus.CREATED);
	}

	@Logged
	@Override
	@Locale(3)
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<List<StudentAbsenceDTO>> getAbsences(Integer classId, Integer studentId, String date,
			String acceptLanguage) {
		LocalDate parsedDate = (date != null && !date.trim().isEmpty())
				? studentAbsenceRequestMapper.parseDate(date)
				: null;
		List<StudentAbsence> absences = studentAbsenceUseCase.getAbsences(classId, studentId, parsedDate);
		return ResponseEntity.ok(studentAbsenceDTOMapper.toDTOList(absences));
	}

	@Logged
	@Override
	@Locale(1)
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<Void> deleteAbsence(Integer id, String acceptLanguage) {
		studentAbsenceUseCase.deleteAbsence(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Logged
	@Override
	@Locale(3)
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<Void> deleteAbsencesByStudentAndDate(Integer classId, Integer studentId, String date,
			String acceptLanguage) {
		LocalDate parsedDate = studentAbsenceRequestMapper.parseDate(date);
		studentAbsenceUseCase.deleteAbsencesByStudentAndDate(classId, studentId, parsedDate);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
