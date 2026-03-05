package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.*;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentAbsenceNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentAbsenceValidationException;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentAbsenceServiceImplTest {

	@Mock
	private StudentAbsenceRepository studentAbsenceRepository;

	@Mock
	private ClassRepository classRepository;

	@Mock
	private StudentRepository studentRepository;

	@Mock
	private SubjectRepository subjectRepository;

	@Mock
	private StudentClassRepository studentClassRepository;

	@Mock
	private SubjectClassRepository subjectClassRepository;

	@Mock
	private ScheduleRepository scheduleRepository;

	@Mock
	private MessageSource messageSource;

	@Mock
	private SessionUser sessionUser;

	@InjectMocks
	private StudentAbsenceServiceImpl studentAbsenceService;

	private static final Integer TEACHER_ID = 1;
	private static final Integer CLASS_ID = 10;
	private static final Integer STUDENT_ID = 20;
	private static final Integer SUBJECT_ID = 30;
	private static final Integer ABSENCE_ID = 100;
	private static final Integer STUDENT_CLASS_ID = 50;
	private static final LocalDate DATE = LocalDate.of(2025, 3, 17);

	@BeforeEach
	void setUp() {
		when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
		when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
		when(this.messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("error message");
	}

	@Test
	void createAbsences_shouldCreateAbsences_whenSubjectIdProvided() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final Student student = Student.builder().id(STUDENT_ID).build();
		when(this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
				.thenReturn(Optional.of(student));

		final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
		when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

		final Subject subject = Subject.builder().id(SUBJECT_ID).build();
		when(this.subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
		when(this.subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID))
				.thenReturn(true);

		when(this.studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID, SUBJECT_ID, DATE))
				.thenReturn(false);

		final StudentAbsence saved = StudentAbsence.builder().id(ABSENCE_ID).studentClassId(STUDENT_CLASS_ID)
				.studentId(STUDENT_ID).classId(CLASS_ID).subjectId(SUBJECT_ID).absenceDate(DATE).build();
		when(this.studentAbsenceRepository.saveAll(anyList())).thenReturn(List.of(saved));
		when(this.studentAbsenceRepository.findByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE)).thenReturn(List.of(saved));

		final List<StudentAbsence> result = this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE);

		assertFalse(result.isEmpty());
		assertEquals(ABSENCE_ID, result.get(0).getId());
		verify(this.studentAbsenceRepository).saveAll(anyList());
	}

	@Test
	void createAbsences_shouldCreateAbsencesFromSchedule_whenSubjectIdIsNull() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final Student student = Student.builder().id(STUDENT_ID).build();
		when(this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
				.thenReturn(Optional.of(student));

		final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
		when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

		final int dayOfWeek = DATE.getDayOfWeek().getValue();
		when(this.scheduleRepository.findSubjectIdsByClassIdAndDay(CLASS_ID, dayOfWeek)).thenReturn(List.of(30, 31));

		when(this.studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID, 30, DATE))
				.thenReturn(false);
		when(this.studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID, 31, DATE))
				.thenReturn(false);

		final StudentAbsence saved1 = StudentAbsence.builder().id(ABSENCE_ID).studentClassId(STUDENT_CLASS_ID)
				.studentId(STUDENT_ID).classId(CLASS_ID).subjectId(30).absenceDate(DATE).build();
		final StudentAbsence saved2 = StudentAbsence.builder().id(101).studentClassId(STUDENT_CLASS_ID).studentId(STUDENT_ID)
				.classId(CLASS_ID).subjectId(31).absenceDate(DATE).build();
		when(this.studentAbsenceRepository.saveAll(anyList())).thenReturn(List.of(saved1, saved2));
		when(this.studentAbsenceRepository.findByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE))
				.thenReturn(List.of(saved1, saved2));

		final List<StudentAbsence> result = this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, null, DATE);

		assertEquals(2, result.size());
		verify(this.scheduleRepository).findSubjectIdsByClassIdAndDay(CLASS_ID, dayOfWeek);
		verify(this.studentAbsenceRepository).saveAll(anyList());
	}

	@Test
	void createAbsences_shouldThrowClassNotFoundException_whenClassNotExists() {
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

		assertThrows(ClassNotFoundException.class,
				() -> this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE));
	}

	@Test
	void createAbsences_shouldThrowClassForbiddenException_whenClassNotOwnedByTeacher() {
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.empty());

		assertThrows(ClassForbiddenException.class,
				() -> this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE));
	}

	@Test
	void createAbsences_shouldThrowValidationException_whenStudentNotFound() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));
		when(this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
				.thenReturn(Optional.empty());

		final StudentAbsenceValidationException exception = assertThrows(StudentAbsenceValidationException.class,
				() -> this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE));

		assertFalse(exception.getErrors().isEmpty());
		assertEquals("studentId", exception.getErrors().get(0).getParam());
	}

	@Test
	void createAbsences_shouldThrowValidationException_whenStudentNotInClass() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final Student student = Student.builder().id(STUDENT_ID).build();
		when(this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
				.thenReturn(Optional.of(student));

		when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.empty());

		final StudentAbsenceValidationException exception = assertThrows(StudentAbsenceValidationException.class,
				() -> this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE));

		assertFalse(exception.getErrors().isEmpty());
		assertEquals("studentId", exception.getErrors().get(0).getParam());
	}

	@Test
	void createAbsences_shouldThrowValidationException_whenStudentInClassButDeleted() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final Student student = Student.builder().id(STUDENT_ID).build();
		when(this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
				.thenReturn(Optional.of(student));

		final StudentClass deletedSc = StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID)
				.deletionDate(LocalDate.now()).build();
		when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(deletedSc));

		final StudentAbsenceValidationException exception = assertThrows(StudentAbsenceValidationException.class,
				() -> this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE));

		assertFalse(exception.getErrors().isEmpty());
		assertEquals("studentId", exception.getErrors().get(0).getParam());
	}

	@Test
	void createAbsences_shouldThrowValidationException_whenSubjectNotBelongsToTeacher() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final Student student = Student.builder().id(STUDENT_ID).build();
		when(this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
				.thenReturn(Optional.of(student));

		final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
		when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

		when(this.subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.empty());

		final StudentAbsenceValidationException exception = assertThrows(StudentAbsenceValidationException.class,
				() -> this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE));

		assertFalse(exception.getErrors().isEmpty());
		assertEquals("subjectId", exception.getErrors().get(0).getParam());
	}

	@Test
	void createAbsences_shouldThrowValidationException_whenSubjectNotAssignedToClass() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final Student student = Student.builder().id(STUDENT_ID).build();
		when(this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
				.thenReturn(Optional.of(student));

		final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
		when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

		final Subject subject = Subject.builder().id(SUBJECT_ID).build();
		when(this.subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
		when(this.subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID))
				.thenReturn(false);

		final StudentAbsenceValidationException exception = assertThrows(StudentAbsenceValidationException.class,
				() -> this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE));

		assertFalse(exception.getErrors().isEmpty());
		assertEquals("subjectId", exception.getErrors().get(0).getParam());
	}

	@Test
	void createAbsences_shouldThrowValidationException_whenNoSubjectsScheduledOnDay() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final Student student = Student.builder().id(STUDENT_ID).build();
		when(this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
				.thenReturn(Optional.of(student));

		final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
		when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

		final int dayOfWeek = DATE.getDayOfWeek().getValue();
		when(this.scheduleRepository.findSubjectIdsByClassIdAndDay(CLASS_ID, dayOfWeek)).thenReturn(List.of());

		final StudentAbsenceValidationException exception = assertThrows(StudentAbsenceValidationException.class,
				() -> this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, null, DATE));

		assertFalse(exception.getErrors().isEmpty());
		assertEquals("date", exception.getErrors().get(0).getParam());
	}

	@Test
	void createAbsences_shouldSkipDuplicates_whenAbsencesAlreadyExist() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final Student student = Student.builder().id(STUDENT_ID).build();
		when(this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
				.thenReturn(Optional.of(student));

		final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
		when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

		final int dayOfWeek = DATE.getDayOfWeek().getValue();
		when(this.scheduleRepository.findSubjectIdsByClassIdAndDay(CLASS_ID, dayOfWeek)).thenReturn(List.of(30, 31));

		when(this.studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID, 30, DATE))
				.thenReturn(true);
		when(this.studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID, 31, DATE))
				.thenReturn(true);

		final StudentAbsence existing = StudentAbsence.builder().id(ABSENCE_ID).studentClassId(STUDENT_CLASS_ID)
				.studentId(STUDENT_ID).classId(CLASS_ID).subjectId(30).absenceDate(DATE).build();
		when(this.studentAbsenceRepository.findByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE))
				.thenReturn(List.of(existing));

		final List<StudentAbsence> result = this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, null, DATE);

		assertFalse(result.isEmpty());
		verify(this.studentAbsenceRepository, never()).saveAll(anyList());
	}

	@Test
	void getAbsences_shouldReturnAbsences_whenStudentIdProvided() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
		when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

		final StudentAbsence absence = StudentAbsence.builder().id(ABSENCE_ID).studentClassId(STUDENT_CLASS_ID)
				.studentId(STUDENT_ID).classId(CLASS_ID).build();
		when(this.studentAbsenceRepository.findByStudentClassId(STUDENT_CLASS_ID)).thenReturn(List.of(absence));

		final List<StudentAbsence> result = this.studentAbsenceService.getAbsences(CLASS_ID, STUDENT_ID, null);

		assertEquals(1, result.size());
		verify(this.studentAbsenceRepository).findByStudentClassId(STUDENT_CLASS_ID);
	}

	@Test
	void getAbsences_shouldReturnAbsences_whenDateProvided() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final StudentAbsence absence = StudentAbsence.builder().id(ABSENCE_ID).classId(CLASS_ID).absenceDate(DATE).build();
		when(this.studentAbsenceRepository.findByClassIdAndDate(CLASS_ID, DATE)).thenReturn(List.of(absence));

		final List<StudentAbsence> result = this.studentAbsenceService.getAbsences(CLASS_ID, null, DATE);

		assertEquals(1, result.size());
		verify(this.studentAbsenceRepository).findByClassIdAndDate(CLASS_ID, DATE);
	}

	@Test
	void getAbsences_shouldReturnAbsences_whenBothStudentIdAndDateProvided() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
		when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

		final StudentAbsence absence = StudentAbsence.builder().id(ABSENCE_ID).studentClassId(STUDENT_CLASS_ID)
				.studentId(STUDENT_ID).classId(CLASS_ID).absenceDate(DATE).build();
		when(this.studentAbsenceRepository.findByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE)).thenReturn(List.of(absence));

		final List<StudentAbsence> result = this.studentAbsenceService.getAbsences(CLASS_ID, STUDENT_ID, DATE);

		assertEquals(1, result.size());
		verify(this.studentAbsenceRepository).findByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE);
	}

	@Test
	void getAbsences_shouldReturnAllAbsences_whenNeitherStudentIdNorDateProvided() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final List<StudentAbsence> expected = List.of(StudentAbsence.builder().id(1).build());
		when(this.studentAbsenceRepository.findByClassId(CLASS_ID)).thenReturn(expected);

		final List<StudentAbsence> result = this.studentAbsenceService.getAbsences(CLASS_ID, null, null);

		assertEquals(expected, result);
		verify(this.studentAbsenceRepository).findByClassId(CLASS_ID);
	}

	@Test
	void getAbsences_shouldThrowClassNotFoundException_whenClassNotExists() {
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

		assertThrows(ClassNotFoundException.class, () -> this.studentAbsenceService.getAbsences(CLASS_ID, STUDENT_ID, DATE));
	}

	@Test
	void getAbsences_shouldThrowClassForbiddenException_whenClassNotOwnedByTeacher() {
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.empty());

		assertThrows(ClassForbiddenException.class,
				() -> this.studentAbsenceService.getAbsences(CLASS_ID, STUDENT_ID, DATE));
	}

	@Test
	void deleteAbsence_shouldDeleteAbsence_whenAbsenceExistsAndOwnedByTeacher() {
		final StudentAbsence absence = StudentAbsence.builder().id(ABSENCE_ID).build();
		when(this.studentAbsenceRepository.findByIdAndTeacherId(ABSENCE_ID, TEACHER_ID)).thenReturn(Optional.of(absence));

        this.studentAbsenceService.deleteAbsence(ABSENCE_ID);

		verify(this.studentAbsenceRepository).deleteById(ABSENCE_ID);
	}

	@Test
	void deleteAbsence_shouldThrowNotFoundException_whenAbsenceNotFound() {
		when(this.studentAbsenceRepository.findByIdAndTeacherId(ABSENCE_ID, TEACHER_ID)).thenReturn(Optional.empty());

		assertThrows(StudentAbsenceNotFoundException.class, () -> this.studentAbsenceService.deleteAbsence(ABSENCE_ID));
	}

	@Test
	void deleteAbsencesByStudentAndDate_shouldDeleteAbsences_whenClassOwnedByTeacher() {
		final Class clazz = Class.builder().id(CLASS_ID).build();
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.of(clazz));

		final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
		when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

        this.studentAbsenceService.deleteAbsencesByStudentAndDate(CLASS_ID, STUDENT_ID, DATE);

		verify(this.studentAbsenceRepository).deleteByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE);
	}

	@Test
	void deleteAbsencesByStudentAndDate_shouldThrowClassNotFoundException_whenClassNotExists() {
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

		assertThrows(ClassNotFoundException.class,
				() -> this.studentAbsenceService.deleteAbsencesByStudentAndDate(CLASS_ID, STUDENT_ID, DATE));
	}

	@Test
	void deleteAbsencesByStudentAndDate_shouldThrowClassForbiddenException_whenClassNotOwnedByTeacher() {
		when(this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
		when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
				.thenReturn(Optional.empty());

		assertThrows(ClassForbiddenException.class,
				() -> this.studentAbsenceService.deleteAbsencesByStudentAndDate(CLASS_ID, STUDENT_ID, DATE));
	}
}
