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
		LocalDate date = LocalDate.of(2025, 1, 15);

		StudentAbsenceEntity entity = new StudentAbsenceEntity(1, STUDENT_CLASS_ID, SUBJECT_ID, date);
		StudentAbsence absence = StudentAbsence.builder().id(1).studentClassId(STUDENT_CLASS_ID).subjectId(SUBJECT_ID)
				.absenceDate(date).build();

		StudentClassEntity studentClassEntity = new StudentClassEntity();
		studentClassEntity.setId(STUDENT_CLASS_ID);
		studentClassEntity.setStudentId(STUDENT_ID);
		studentClassEntity.setClassId(CLASS_ID);

		StudentEntity studentEntity = new StudentEntity();
		studentEntity.setId(STUDENT_ID);
		studentEntity.setName("Juan");
		studentEntity.setSurnames("García López");

		SubjectEntity subjectEntity = new SubjectEntity();
		subjectEntity.setId(SUBJECT_ID);
		subjectEntity.setName("Matemáticas");

		when(studentAbsenceJPARepository.findByStudentClassId(STUDENT_CLASS_ID)).thenReturn(List.of(entity));
		when(studentAbsenceMapper.toModelList(List.of(entity))).thenReturn(List.of(absence));
		when(studentClassJPARepository.findAllById(any())).thenReturn(List.of(studentClassEntity));
		when(studentJPARepository.findAllById(any())).thenReturn(List.of(studentEntity));
		when(subjectJPARepository.findAllById(any())).thenReturn(List.of(subjectEntity));

		List<StudentAbsence> result = studentAbsenceRepository.findByStudentClassId(STUDENT_CLASS_ID);

		assertEquals(1, result.size());
		assertEquals(STUDENT_ID, result.get(0).getStudentId());
		assertEquals(CLASS_ID, result.get(0).getClassId());
		assertEquals("Juan", result.get(0).getStudentName());
		assertEquals("García López", result.get(0).getStudentSurnames());
		assertEquals("Matemáticas", result.get(0).getSubjectName());
		verify(studentAbsenceJPARepository).findByStudentClassId(STUDENT_CLASS_ID);
	}

	@Test
	void findByClassIdAndDate_shouldReturnEnrichedAbsences() {
		LocalDate date = LocalDate.of(2025, 3, 10);

		StudentAbsenceEntity entity = new StudentAbsenceEntity(2, STUDENT_CLASS_ID, SUBJECT_ID, date);
		StudentAbsence absence = StudentAbsence.builder().id(2).studentClassId(STUDENT_CLASS_ID).subjectId(SUBJECT_ID)
				.absenceDate(date).build();

		StudentClassEntity studentClassEntity = new StudentClassEntity();
		studentClassEntity.setId(STUDENT_CLASS_ID);
		studentClassEntity.setStudentId(STUDENT_ID);
		studentClassEntity.setClassId(CLASS_ID);

		StudentEntity studentEntity = new StudentEntity();
		studentEntity.setId(STUDENT_ID);
		studentEntity.setName("Ana");
		studentEntity.setSurnames("Martínez Ruiz");

		SubjectEntity subjectEntity = new SubjectEntity();
		subjectEntity.setId(SUBJECT_ID);
		subjectEntity.setName("Lengua");

		when(studentAbsenceJPARepository.findByClassIdAndAbsenceDate(CLASS_ID, date)).thenReturn(List.of(entity));
		when(studentAbsenceMapper.toModelList(List.of(entity))).thenReturn(List.of(absence));
		when(studentClassJPARepository.findAllById(any())).thenReturn(List.of(studentClassEntity));
		when(studentJPARepository.findAllById(any())).thenReturn(List.of(studentEntity));
		when(subjectJPARepository.findAllById(any())).thenReturn(List.of(subjectEntity));

		List<StudentAbsence> result = studentAbsenceRepository.findByClassIdAndDate(CLASS_ID, date);

		assertEquals(1, result.size());
		assertEquals(STUDENT_ID, result.get(0).getStudentId());
		assertEquals(CLASS_ID, result.get(0).getClassId());
		assertEquals("Ana", result.get(0).getStudentName());
		assertEquals("Martínez Ruiz", result.get(0).getStudentSurnames());
		assertEquals("Lengua", result.get(0).getSubjectName());
		verify(studentAbsenceJPARepository).findByClassIdAndAbsenceDate(CLASS_ID, date);
	}

	@Test
	void findByIdAndTeacherId_shouldReturnAbsence_whenFound() {
		Integer id = 1;
		Integer teacherId = 5;
		StudentAbsenceEntity entity = new StudentAbsenceEntity(id, STUDENT_CLASS_ID, SUBJECT_ID,
				LocalDate.of(2025, 2, 1));
		StudentAbsence absence = StudentAbsence.builder().id(id).studentClassId(STUDENT_CLASS_ID).subjectId(SUBJECT_ID)
				.absenceDate(LocalDate.of(2025, 2, 1)).build();

		when(studentAbsenceJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.of(entity));
		when(studentAbsenceMapper.toModel(entity)).thenReturn(absence);

		Optional<StudentAbsence> result = studentAbsenceRepository.findByIdAndTeacherId(id, teacherId);

		assertTrue(result.isPresent());
		assertEquals(id, result.get().getId());
		verify(studentAbsenceJPARepository).findByIdAndTeacherId(id, teacherId);
		verify(studentAbsenceMapper).toModel(entity);
	}

	@Test
	void findByIdAndTeacherId_shouldReturnEmpty_whenNotFound() {
		Integer id = 999;
		Integer teacherId = 5;

		when(studentAbsenceJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.empty());

		Optional<StudentAbsence> result = studentAbsenceRepository.findByIdAndTeacherId(id, teacherId);

		assertTrue(result.isEmpty());
		verify(studentAbsenceJPARepository).findByIdAndTeacherId(id, teacherId);
		verifyNoInteractions(studentAbsenceMapper);
	}

	@Test
	void saveAll_shouldSaveAndEnrich() {
		LocalDate date = LocalDate.of(2025, 4, 5);

		StudentAbsence inputAbsence = StudentAbsence.builder().studentClassId(STUDENT_CLASS_ID).subjectId(SUBJECT_ID)
				.absenceDate(date).build();
		StudentAbsenceEntity inputEntity = new StudentAbsenceEntity(null, STUDENT_CLASS_ID, SUBJECT_ID, date);
		StudentAbsenceEntity savedEntity = new StudentAbsenceEntity(3, STUDENT_CLASS_ID, SUBJECT_ID, date);
		StudentAbsence mappedAbsence = StudentAbsence.builder().id(3).studentClassId(STUDENT_CLASS_ID)
				.subjectId(SUBJECT_ID).absenceDate(date).build();

		StudentClassEntity studentClassEntity = new StudentClassEntity();
		studentClassEntity.setId(STUDENT_CLASS_ID);
		studentClassEntity.setStudentId(STUDENT_ID);
		studentClassEntity.setClassId(CLASS_ID);

		StudentEntity studentEntity = new StudentEntity();
		studentEntity.setId(STUDENT_ID);
		studentEntity.setName("Pedro");
		studentEntity.setSurnames("López Sánchez");

		SubjectEntity subjectEntity = new SubjectEntity();
		subjectEntity.setId(SUBJECT_ID);
		subjectEntity.setName("Ciencias");

		when(studentAbsenceMapper.toEntityList(List.of(inputAbsence))).thenReturn(List.of(inputEntity));
		when(studentAbsenceJPARepository.saveAll(List.of(inputEntity))).thenReturn(List.of(savedEntity));
		when(studentAbsenceMapper.toModelList(List.of(savedEntity))).thenReturn(List.of(mappedAbsence));
		when(studentClassJPARepository.findAllById(any())).thenReturn(List.of(studentClassEntity));
		when(studentJPARepository.findAllById(any())).thenReturn(List.of(studentEntity));
		when(subjectJPARepository.findAllById(any())).thenReturn(List.of(subjectEntity));

		List<StudentAbsence> result = studentAbsenceRepository.saveAll(List.of(inputAbsence));

		assertEquals(1, result.size());
		assertEquals(3, result.get(0).getId());
		assertEquals(STUDENT_ID, result.get(0).getStudentId());
		assertEquals(CLASS_ID, result.get(0).getClassId());
		assertEquals("Pedro", result.get(0).getStudentName());
		assertEquals("López Sánchez", result.get(0).getStudentSurnames());
		assertEquals("Ciencias", result.get(0).getSubjectName());
		verify(studentAbsenceJPARepository).saveAll(List.of(inputEntity));
	}

	@Test
	void deleteById_shouldDelegate() {
		Integer id = 1;

		studentAbsenceRepository.deleteById(id);

		verify(studentAbsenceJPARepository).deleteById(id);
	}

	@Test
	void deleteByStudentClassIdAndDate_shouldDelegate() {
		LocalDate date = LocalDate.of(2025, 5, 20);

		studentAbsenceRepository.deleteByStudentClassIdAndDate(STUDENT_CLASS_ID, date);

		verify(studentAbsenceJPARepository).deleteByStudentClassIdAndAbsenceDate(STUDENT_CLASS_ID, date);
	}

	@Test
	void existsByStudentClassIdAndSubjectIdAndDate_shouldDelegate() {
		LocalDate date = LocalDate.of(2025, 6, 1);

		when(studentAbsenceJPARepository.existsByStudentClassIdAndSubjectIdAndAbsenceDate(STUDENT_CLASS_ID, SUBJECT_ID,
				date)).thenReturn(true);

		boolean result = studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID,
				SUBJECT_ID, date);

		assertTrue(result);
		verify(studentAbsenceJPARepository).existsByStudentClassIdAndSubjectIdAndAbsenceDate(STUDENT_CLASS_ID,
				SUBJECT_ID, date);
	}
}
