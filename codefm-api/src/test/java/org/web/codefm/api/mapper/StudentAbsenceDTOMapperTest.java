package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.model.StudentAbsenceDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StudentAbsenceDTOMapperTest {

	@Spy
	@InjectMocks
	private StudentAbsenceDTOMapperImpl mapper;

	@Test
	void toDTO_shouldMapAllFields() {
		StudentAbsence absence = StudentAbsence.builder().id(1).studentId(10).studentName("Juan")
				.studentSurnames("García López").classId(5).subjectId(3).subjectName("Matemáticas")
				.absenceDate(LocalDate.of(2026, 3, 15)).build();

		StudentAbsenceDTO result = mapper.toDTO(absence);

		assertNotNull(result);
		assertEquals(1, result.getId());
		assertEquals(10, result.getStudentId());
		assertEquals("Juan", result.getStudentName());
		assertEquals("García López", result.getStudentSurnames());
		assertEquals(5, result.getClassId());
		assertEquals(3, result.getSubjectId());
		assertEquals("Matemáticas", result.getSubjectName());
		assertEquals("15/03/2026", result.getAbsenceDate());
	}

	@Test
	void toDTO_shouldReturnNullAbsenceDate_whenDateIsNull() {
		StudentAbsence absence = StudentAbsence.builder().id(1).studentId(10).absenceDate(null).build();

		StudentAbsenceDTO result = mapper.toDTO(absence);

		assertNotNull(result);
		assertNull(result.getAbsenceDate());
	}

	@Test
	void toDTOList_shouldMapAllElements() {
		List<StudentAbsence> absences = Arrays.asList(
				StudentAbsence.builder().id(1).absenceDate(LocalDate.of(2026, 3, 15)).build(),
				StudentAbsence.builder().id(2).absenceDate(LocalDate.of(2026, 3, 16)).build());

		List<StudentAbsenceDTO> result = mapper.toDTOList(absences);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("15/03/2026", result.get(0).getAbsenceDate());
		assertEquals("16/03/2026", result.get(1).getAbsenceDate());
	}

	@Test
	void toDTOList_shouldReturnEmptyList_whenInputIsEmpty() {
		List<StudentAbsenceDTO> result = mapper.toDTOList(Collections.emptyList());

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
}
