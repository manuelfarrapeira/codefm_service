package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentAbsenceEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentAbsenceJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectJPARepository;
import org.web.codefm.infrastructure.mapper.StudentAbsenceMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentAbsenceRepositoryImplTest {

	@Mock
	private StudentAbsenceJPARepository studentAbsenceJPARepository;

	@Mock
	private StudentClassJPARepository studentClassJPARepository;

	@Mock
	private StudentJPARepository studentJPARepository;

	@Mock
	private SubjectJPARepository subjectJPARepository;

	@Mock
	private StudentAbsenceMapper studentAbsenceMapper;

	@InjectMocks
	private StudentAbsenceRepositoryImpl studentAbsenceRepository;

	private static final Integer STUDENT_CLASS_ID = 50;
	private static final Integer STUDENT_ID = 10;
	private static final Integer CLASS_ID = 1;
	private static final Integer SUBJECT_ID = 20;

	@Test
	void findByStudentClassId_shouldReturnEnrichedAbsences() {
		final LocalDate date = LocalDate.of(2025, 1, 15);

		final StudentAbsenceEntity entity = new StudentAbsenceEntity(1, STUDENT_CLASS_ID, SUBJECT_ID, date);
		final StudentAbsence absence = StudentAbsence.builder().id(1).studentClassId(STUDENT_CLASS_ID)
				.subjectId(SUBJECT_ID).absenceDate(date).build();

		final StudentClassEntity studentClassEntity = new StudentClassEntity();
		studentClassEntity.setId(STUDENT_CLASS_ID);
		studentClassEntity.setStudentId(STUDENT_ID);
		studentClassEntity.setClassId(CLASS_ID);

		final StudentEntity studentEntity = new StudentEntity();
		studentEntity.setId(STUDENT_ID);
		studentEntity.setName("Juan");
		studentEntity.setSurnames("García López");

		final SubjectEntity subjectEntity = new SubjectEntity();
		subjectEntity.setId(SUBJECT_ID);
		subjectEntity.setName("Matemáticas");

		when(this.studentAbsenceJPARepository.findByStudentClassId(STUDENT_CLASS_ID)).thenReturn(List.of(entity));
		when(this.studentAbsenceMapper.toModelList(List.of(entity))).thenReturn(List.of(absence));
		when(this.studentClassJPARepository.findAllById(any())).thenReturn(List.of(studentClassEntity));
		when(this.studentJPARepository.findAllById(any())).thenReturn(List.of(studentEntity));
		when(this.subjectJPARepository.findAllById(any())).thenReturn(List.of(subjectEntity));

		final List<StudentAbsence> result = this.studentAbsenceRepository.findByStudentClassId(STUDENT_CLASS_ID);

		assertEquals(1, result.size());
		assertEquals(STUDENT_ID, result.get(0).getStudentId());
		assertEquals(CLASS_ID, result.get(0).getClassId());
		assertEquals("Juan", result.get(0).getStudentName());
		assertEquals("García López", result.get(0).getStudentSurnames());
		assertEquals("Matemáticas", result.get(0).getSubjectName());
		verify(this.studentAbsenceJPARepository).findByStudentClassId(STUDENT_CLASS_ID);
	}

	@Test
	void findByClassIdAndDate_shouldReturnEnrichedAbsences() {
		final LocalDate date = LocalDate.of(2025, 3, 10);

		final StudentAbsenceEntity entity = new StudentAbsenceEntity(2, STUDENT_CLASS_ID, SUBJECT_ID, date);
		final StudentAbsence absence = StudentAbsence.builder().id(2).studentClassId(STUDENT_CLASS_ID)
				.subjectId(SUBJECT_ID).absenceDate(date).build();

		final StudentClassEntity studentClassEntity = new StudentClassEntity();
		studentClassEntity.setId(STUDENT_CLASS_ID);
		studentClassEntity.setStudentId(STUDENT_ID);
		studentClassEntity.setClassId(CLASS_ID);

		final StudentEntity studentEntity = new StudentEntity();
		studentEntity.setId(STUDENT_ID);
		studentEntity.setName("Ana");
		studentEntity.setSurnames("Martínez Ruiz");

		final SubjectEntity subjectEntity = new SubjectEntity();
		subjectEntity.setId(SUBJECT_ID);
		subjectEntity.setName("Lengua");

		when(this.studentAbsenceJPARepository.findByClassIdAndAbsenceDate(CLASS_ID, date)).thenReturn(List.of(entity));
		when(this.studentAbsenceMapper.toModelList(List.of(entity))).thenReturn(List.of(absence));
		when(this.studentClassJPARepository.findAllById(any())).thenReturn(List.of(studentClassEntity));
		when(this.studentJPARepository.findAllById(any())).thenReturn(List.of(studentEntity));
		when(this.subjectJPARepository.findAllById(any())).thenReturn(List.of(subjectEntity));

		final List<StudentAbsence> result = this.studentAbsenceRepository.findByClassIdAndDate(CLASS_ID, date);

		assertEquals(1, result.size());
		assertEquals(STUDENT_ID, result.get(0).getStudentId());
		assertEquals(CLASS_ID, result.get(0).getClassId());
		assertEquals("Ana", result.get(0).getStudentName());
		assertEquals("Martínez Ruiz", result.get(0).getStudentSurnames());
		assertEquals("Lengua", result.get(0).getSubjectName());
		verify(this.studentAbsenceJPARepository).findByClassIdAndAbsenceDate(CLASS_ID, date);
	}

	@Test
	void findByIdAndTeacherId_shouldReturnAbsence_whenFound() {
		final Integer id = 1;
		final Integer teacherId = 5;
		final StudentAbsenceEntity entity = new StudentAbsenceEntity(id, STUDENT_CLASS_ID, SUBJECT_ID,
				LocalDate.of(2025, 2, 1));
		final StudentAbsence absence = StudentAbsence.builder().id(id).studentClassId(STUDENT_CLASS_ID)
				.subjectId(SUBJECT_ID).absenceDate(LocalDate.of(2025, 2, 1)).build();

		when(this.studentAbsenceJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.of(entity));
		when(this.studentAbsenceMapper.toModel(entity)).thenReturn(absence);

		final Optional<StudentAbsence> result = this.studentAbsenceRepository.findByIdAndTeacherId(id, teacherId);

		assertTrue(result.isPresent());
		assertEquals(id, result.get().getId());
		verify(this.studentAbsenceJPARepository).findByIdAndTeacherId(id, teacherId);
		verify(this.studentAbsenceMapper).toModel(entity);
	}

	@Test
	void findByIdAndTeacherId_shouldReturnEmpty_whenNotFound() {
		final Integer id = 999;
		final Integer teacherId = 5;

		when(this.studentAbsenceJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.empty());

		final Optional<StudentAbsence> result = this.studentAbsenceRepository.findByIdAndTeacherId(id, teacherId);

		assertTrue(result.isEmpty());
		verify(this.studentAbsenceJPARepository).findByIdAndTeacherId(id, teacherId);
		verifyNoInteractions(this.studentAbsenceMapper);
	}

	@Test
	void saveAll_shouldSaveAndEnrich() {
		final LocalDate date = LocalDate.of(2025, 4, 5);

		final StudentAbsence inputAbsence = StudentAbsence.builder().studentClassId(STUDENT_CLASS_ID)
				.subjectId(SUBJECT_ID).absenceDate(date).build();
		final StudentAbsenceEntity inputEntity = new StudentAbsenceEntity(null, STUDENT_CLASS_ID, SUBJECT_ID, date);
		final StudentAbsenceEntity savedEntity = new StudentAbsenceEntity(3, STUDENT_CLASS_ID, SUBJECT_ID, date);
		final StudentAbsence mappedAbsence = StudentAbsence.builder().id(3).studentClassId(STUDENT_CLASS_ID)
				.subjectId(SUBJECT_ID).absenceDate(date).build();

		final StudentClassEntity studentClassEntity = new StudentClassEntity();
		studentClassEntity.setId(STUDENT_CLASS_ID);
		studentClassEntity.setStudentId(STUDENT_ID);
		studentClassEntity.setClassId(CLASS_ID);

		final StudentEntity studentEntity = new StudentEntity();
		studentEntity.setId(STUDENT_ID);
		studentEntity.setName("Pedro");
		studentEntity.setSurnames("López Sánchez");

		final SubjectEntity subjectEntity = new SubjectEntity();
		subjectEntity.setId(SUBJECT_ID);
		subjectEntity.setName("Ciencias");

		when(this.studentAbsenceMapper.toEntityList(List.of(inputAbsence))).thenReturn(List.of(inputEntity));
		when(this.studentAbsenceJPARepository.saveAll(List.of(inputEntity))).thenReturn(List.of(savedEntity));
		when(this.studentAbsenceMapper.toModelList(List.of(savedEntity))).thenReturn(List.of(mappedAbsence));
		when(this.studentClassJPARepository.findAllById(any())).thenReturn(List.of(studentClassEntity));
		when(this.studentJPARepository.findAllById(any())).thenReturn(List.of(studentEntity));
		when(this.subjectJPARepository.findAllById(any())).thenReturn(List.of(subjectEntity));

		final List<StudentAbsence> result = this.studentAbsenceRepository.saveAll(List.of(inputAbsence));

		assertEquals(1, result.size());
		assertEquals(3, result.get(0).getId());
		assertEquals(STUDENT_ID, result.get(0).getStudentId());
		assertEquals(CLASS_ID, result.get(0).getClassId());
		assertEquals("Pedro", result.get(0).getStudentName());
		assertEquals("López Sánchez", result.get(0).getStudentSurnames());
		assertEquals("Ciencias", result.get(0).getSubjectName());
		verify(this.studentAbsenceJPARepository).saveAll(List.of(inputEntity));
	}

	@Test
	void deleteById_shouldDelegate() {
		final Integer id = 1;

		this.studentAbsenceRepository.deleteById(id);

		verify(this.studentAbsenceJPARepository).deleteById(id);
	}

	@Test
	void deleteByStudentClassIdAndDate_shouldDelegate() {
		final LocalDate date = LocalDate.of(2025, 5, 20);

		this.studentAbsenceRepository.deleteByStudentClassIdAndDate(STUDENT_CLASS_ID, date);

		verify(this.studentAbsenceJPARepository).deleteByStudentClassIdAndAbsenceDate(STUDENT_CLASS_ID, date);
	}

	@Test
	void existsByStudentClassIdAndSubjectIdAndDate_shouldDelegate() {
		final LocalDate date = LocalDate.of(2025, 6, 1);

		when(this.studentAbsenceJPARepository.existsByStudentClassIdAndSubjectIdAndAbsenceDate(STUDENT_CLASS_ID,
				SUBJECT_ID, date)).thenReturn(true);

		final boolean result = this.studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID,
				SUBJECT_ID, date);

		assertTrue(result);
		verify(this.studentAbsenceJPARepository).existsByStudentClassIdAndSubjectIdAndAbsenceDate(STUDENT_CLASS_ID,
				SUBJECT_ID, date);
	}

	@Test
	void deleteByStudentClassId_shouldDelegate() {
		this.studentAbsenceRepository.deleteByStudentClassId(STUDENT_CLASS_ID);

		verify(this.studentAbsenceJPARepository).deleteByStudentClassId(STUDENT_CLASS_ID);
	}

	@Test
	void hardDeleteByClassId_shouldDelegate() {
		this.studentAbsenceRepository.hardDeleteByClassId(CLASS_ID);

		verify(this.studentAbsenceJPARepository).hardDeleteByClassId(CLASS_ID);
	}

	@Test
	void hardDeleteByStudentId_shouldDelegate() {
		this.studentAbsenceRepository.hardDeleteByStudentId(STUDENT_ID);

		verify(this.studentAbsenceJPARepository).hardDeleteByStudentId(STUDENT_ID);
	}

	@Test
	void hardDeleteBySubjectClassId_shouldDelegate() {
		Integer subjectClassId = 99;

		this.studentAbsenceRepository.hardDeleteBySubjectClassId(subjectClassId);

		verify(this.studentAbsenceJPARepository).hardDeleteBySubjectClassId(subjectClassId);
	}
}
