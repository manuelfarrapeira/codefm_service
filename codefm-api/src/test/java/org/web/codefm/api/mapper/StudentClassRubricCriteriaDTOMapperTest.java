package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.model.StudentClassRubricCriteriaDTO;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StudentClassRubricCriteriaDTOMapperTest {

    private final StudentClassRubricCriteriaDTOMapper mapper = new StudentClassRubricCriteriaDTOMapperImpl();

    @Test
    void toDTO_shouldMapAllFields_whenAllFieldsArePresent() {
        final StudentClassRubricCriteria criteria = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(10).rubricId(20).studentId(30)
                .studentName("Juan").studentSurnames("García López")
                .criterionId(40).criterionDescription("Lo hace bien").qualification("Notable")
                .gradeStart(7).gradeEnd(10)
                .build();

        final StudentClassRubricCriteriaDTO result = mapper.toDTO(criteria);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(10, result.getClassRubricId());
        assertEquals(20, result.getRubricId());
        assertEquals(30, result.getStudentId());
        assertEquals("Juan", result.getStudentName());
        assertEquals("García López", result.getStudentSurnames());
        assertEquals(40, result.getCriterionId());
        assertEquals("Lo hace bien", result.getCriterionDescription());
        assertEquals("Notable", result.getQualification());
        assertEquals(7, result.getGradeStart());
        assertEquals(10, result.getGradeEnd());
    }

    @Test
    void toDTO_shouldReturnNull_whenInputIsNull() {
        final StudentClassRubricCriteriaDTO result = mapper.toDTO(null);

        assertNull(result);
    }

    @Test
    void toDTO_shouldHandleNullOptionalFields() {
        final StudentClassRubricCriteria criteria = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(10).studentId(30).criterionId(40)
                .rubricId(null).studentName(null).studentSurnames(null)
                .criterionDescription(null).qualification(null).gradeStart(null).gradeEnd(null)
                .build();

        final StudentClassRubricCriteriaDTO result = mapper.toDTO(criteria);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(10, result.getClassRubricId());
        assertEquals(30, result.getStudentId());
        assertEquals(40, result.getCriterionId());
        assertNull(result.getRubricId());
        assertNull(result.getStudentName());
        assertNull(result.getStudentSurnames());
        assertNull(result.getCriterionDescription());
        assertNull(result.getQualification());
        assertNull(result.getGradeStart());
        assertNull(result.getGradeEnd());
    }

    @Test
    void toDTOList_shouldMapAllCriteria() {
        final StudentClassRubricCriteria criteria1 = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(10).rubricId(20).studentId(30)
                .studentName("Juan").studentSurnames("García")
                .criterionId(40).criterionDescription("Mal").gradeStart(0).gradeEnd(4)
                .build();
        final StudentClassRubricCriteria criteria2 = StudentClassRubricCriteria.builder()
                .id(2).classRubricId(11).rubricId(21).studentId(31)
                .studentName("Ana").studentSurnames("López")
                .criterionId(41).criterionDescription("Bien").gradeStart(7).gradeEnd(10)
                .build();

        final List<StudentClassRubricCriteriaDTO> result = mapper.toDTOList(Arrays.asList(criteria1, criteria2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Juan", result.get(0).getStudentName());
        assertEquals("Mal", result.get(0).getCriterionDescription());
        assertEquals(2, result.get(1).getId());
        assertEquals("Ana", result.get(1).getStudentName());
        assertEquals("Bien", result.get(1).getCriterionDescription());
    }

    @Test
    void toDTOList_shouldReturnEmptyList_whenInputIsEmpty() {
        final List<StudentClassRubricCriteriaDTO> result = mapper.toDTOList(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toDTOList_shouldReturnNull_whenInputIsNull() {
        final List<StudentClassRubricCriteriaDTO> result = mapper.toDTOList(null);

        assertNull(result);
    }
}

