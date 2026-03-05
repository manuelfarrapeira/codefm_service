package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentAbsenceNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentAbsenceValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.StudentAbsenceService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentAbsenceServiceImpl implements StudentAbsenceService {

	private static final String FIELD_STUDENT_ID = "studentId";
	private static final String FIELD_SUBJECT_ID = "subjectId";
	private static final String FIELD_DATE = "date";

	private final StudentAbsenceRepository studentAbsenceRepository;
	private final ClassRepository classRepository;
	private final StudentRepository studentRepository;
	private final StudentClassRepository studentClassRepository;
	private final SubjectRepository subjectRepository;
	private final SubjectClassRepository subjectClassRepository;
	private final ScheduleRepository scheduleRepository;
	private final MessageSource messageSource;
	private final SessionUser sessionUser;

	@Override
	@Transactional
	public List<StudentAbsence> createAbsences(Integer classId, Integer studentId, Integer subjectId, LocalDate date) {
		final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
		final Locale locale = this.sessionUser.getLocale();
		final List<ErrorMessage> errors = new ArrayList<>();

		this.validateClassOwnership(classId, teacherId, locale);
		this.validateStudentOwnership(studentId, teacherId, errors, locale);

		if (!errors.isEmpty()) {
			throw new StudentAbsenceValidationException(errors);
		}

		final Integer studentClassId = this.resolveStudentClassId(studentId, classId, errors, locale);

		if (!errors.isEmpty()) {
			throw new StudentAbsenceValidationException(errors);
		}

		final List<Integer> subjectIds;

		if (subjectId != null) {
			final int dayOfWeek = date.getDayOfWeek().getValue();
			this.validateSubjectInClass(subjectId, classId, teacherId, dayOfWeek, errors, locale);
			if (!errors.isEmpty()) {
				throw new StudentAbsenceValidationException(errors);
			}
			subjectIds = List.of(subjectId);
		} else {
			final int dayOfWeek = date.getDayOfWeek().getValue();
			subjectIds = this.scheduleRepository.findSubjectIdsByClassIdAndDay(classId, dayOfWeek);
			if (subjectIds.isEmpty()) {
				final String message = this.messageSource
						.getMessage(MessageKeys.ABSENCE_VALIDATION_NO_SUBJECTS_SCHEDULED, null, locale);
				errors.add(new ErrorMessage(FIELD_DATE, message));
				throw new StudentAbsenceValidationException(errors);
			}
		}

		final List<StudentAbsence> absencesToCreate = subjectIds.stream()
				.filter(sid -> !this.studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(studentClassId,
						sid, date))
				.map(sid -> StudentAbsence.builder().studentClassId(studentClassId).subjectId(sid).absenceDate(date)
						.build())
				.toList();

		if (absencesToCreate.isEmpty()) {
			return this.studentAbsenceRepository.findByStudentClassIdAndDate(studentClassId, date);
		}

		final List<StudentAbsence> saved = this.studentAbsenceRepository.saveAll(absencesToCreate);

		final List<StudentAbsence> existing = this.studentAbsenceRepository.findByStudentClassIdAndDate(studentClassId,
				date);
		return existing.isEmpty() ? saved : existing;
	}

	@Override
	public List<StudentAbsence> getAbsences(Integer classId, Integer studentId, LocalDate date) {
		final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
		final Locale locale = this.sessionUser.getLocale();

		this.validateClassOwnership(classId, teacherId, locale);

		if (studentId != null && date != null) {
			final Integer studentClassId = this.resolveStudentClassIdForQuery(studentId, classId);
			if (studentClassId == null) {
				return List.of();
			}
			return this.studentAbsenceRepository.findByStudentClassIdAndDate(studentClassId, date);
		}
		if (studentId != null) {
			final Integer studentClassId = this.resolveStudentClassIdForQuery(studentId, classId);
			if (studentClassId == null) {
				return List.of();
			}
			return this.studentAbsenceRepository.findByStudentClassId(studentClassId);
		}
		if (date != null) {
			return this.studentAbsenceRepository.findByClassIdAndDate(classId, date);
		}

		return this.studentAbsenceRepository.findByClassId(classId);
	}

	@Override
	@Transactional
	public void deleteAbsence(Integer id) {
		final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
		final Locale locale = this.sessionUser.getLocale();

		this.studentAbsenceRepository.findByIdAndTeacherId(id, teacherId)
				.orElseThrow(() -> new StudentAbsenceNotFoundException(
						this.messageSource.getMessage(MessageKeys.ABSENCE_NOT_FOUND, null, locale)));

		this.studentAbsenceRepository.deleteById(id);
	}

	@Override
	@Transactional
	public void deleteAbsencesByStudentAndDate(Integer classId, Integer studentId, LocalDate date) {
		final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
		final Locale locale = this.sessionUser.getLocale();

		this.validateClassOwnership(classId, teacherId, locale);

		final List<ErrorMessage> errors = new ArrayList<>();
		final Integer studentClassId = this.resolveStudentClassId(studentId, classId, errors, locale);

		if (!errors.isEmpty()) {
			throw new StudentAbsenceValidationException(errors);
		}

		this.studentAbsenceRepository.deleteByStudentClassIdAndDate(studentClassId, date);
	}

	private Integer resolveStudentClassId(Integer studentId, Integer classId, List<ErrorMessage> errors,
			Locale locale) {
		final Optional<StudentClass> studentClass = this.studentClassRepository.findByClassIdAndStudentId(classId,
				studentId);
		if (studentClass.isEmpty() || studentClass.get().getDeletionDate() != null) {
			final String message = this.messageSource.getMessage(MessageKeys.ABSENCE_VALIDATION_STUDENT_NOT_IN_CLASS,
					null, locale);
			errors.add(new ErrorMessage(FIELD_STUDENT_ID, message));
			return null;
		}
		return studentClass.get().getId();
	}

	private Integer resolveStudentClassIdForQuery(Integer studentId, Integer classId) {
		return this.studentClassRepository.findByClassIdAndStudentId(classId, studentId)
				.filter(sc -> sc.getDeletionDate() == null).map(StudentClass::getId).orElse(null);
	}

	private void validateClassOwnership(Integer classId, Integer teacherId, Locale locale) {
		this.classRepository.findById(classId).orElseThrow(() -> new ClassNotFoundException(
				this.messageSource.getMessage(MessageKeys.ABSENCE_VALIDATION_CLASS_NOT_FOUND, null, locale)));

		this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
				.orElseThrow(() -> new ClassForbiddenException(
						this.messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, locale)));
	}

	private void validateStudentOwnership(Integer studentId, Integer teacherId, List<ErrorMessage> errors,
			Locale locale) {
		if (studentId == null) {
			final String message = this.messageSource.getMessage(MessageKeys.ABSENCE_VALIDATION_STUDENT_REQUIRED, null,
					locale);
			errors.add(new ErrorMessage(FIELD_STUDENT_ID, message));
			return;
		}

		if (this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId).isEmpty()) {
			final String message = this.messageSource.getMessage(MessageKeys.ABSENCE_VALIDATION_STUDENT_NOT_FOUND, null,
					locale);
			errors.add(new ErrorMessage(FIELD_STUDENT_ID, message));
		}
	}

	private void validateSubjectInClass(Integer subjectId, Integer classId, Integer teacherId, Integer dayOfWeek,
			List<ErrorMessage> errors, Locale locale) {
		if (this.subjectRepository.findByIdAndTeacherId(subjectId, teacherId).isEmpty()) {
			final String message = this.messageSource.getMessage(MessageKeys.ABSENCE_VALIDATION_SUBJECT_NOT_FOUND, null,
					locale);
			errors.add(new ErrorMessage(FIELD_SUBJECT_ID, message));
			return;
		}

		if (!this.subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(subjectId, classId)) {
			final String message = this.messageSource.getMessage(MessageKeys.ABSENCE_VALIDATION_SUBJECT_NOT_IN_CLASS,
					null, locale);
			errors.add(new ErrorMessage(FIELD_SUBJECT_ID, message));
			return;
		}

		if (!this.scheduleRepository.existsByClassIdAndSubjectIdAndDay(classId, subjectId, dayOfWeek)) {
			final String message = this.messageSource.getMessage(
					MessageKeys.ABSENCE_VALIDATION_SUBJECT_NOT_SCHEDULED_ON_DAY, null, locale);
			errors.add(new ErrorMessage(FIELD_SUBJECT_ID, message));
		}
	}
}
