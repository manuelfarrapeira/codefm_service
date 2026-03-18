package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.RubricCriterionAssignment;
import org.web.codefm.domain.entity.teachernotebook.StudentCriteriaGroup;
import org.web.codefm.model.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StudentCriteriaGroupDTOMapperTest {

    private final StudentCriteriaGroupDTOMapper mapper = new StudentCriteriaGroupDTOMapperImpl();

    @Test
    void toDTO_shouldMapStudentAndRubricCriteria_whenAllFieldsArePresent() {
        final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                .id(1).classRubricId(10).rubricId(20).rubricTitle("Rubric A")
                .criterionId(30).criterionDescription("Lo hace bien").gradeStart(7).gradeEnd(10)
                .build();
        final StudentCriteriaGroup group = StudentCriteriaGroup.builder()
                .studentId(100).studentName("Juan").studentSurnames("García López")
                .rubricCriteria(List.of(assignment))
                .build();

        final StudentCriteriaGroupDTO result = mapper.toDTO(group);

        assertNotNull(result);
        assertNotNull(result.getStudent());
        assertEquals(100, result.getStudent().getId());
        assertEquals("Juan", result.getStudent().getName());
        assertEquals("García López", result.getStudent().getSurnames());
        assertNotNull(result.getRubricCriteria());
        assertEquals(1, result.getRubricCriteria().size());

        final RubricCriterionAssignmentDTO assignmentDTO = result.getRubricCriteria().get(0);
        assertEquals(1, assignmentDTO.getId());
        assertEquals(10, assignmentDTO.getClassRubricId());
        assertEquals(20, assignmentDTO.getRubric().getId());
        assertEquals("Rubric A", assignmentDTO.getRubric().getTitle());
        assertEquals(30, assignmentDTO.getCriterion().getId());
        assertEquals("Lo hace bien", assignmentDTO.getCriterion().getDescription());
        assertEquals(7, assignmentDTO.getCriterion().getGradeStart());
        assertEquals(10, assignmentDTO.getCriterion().getGradeEnd());
    }

    @Test
    void toDTO_shouldReturnNull_whenGroupIsNull() {
        final StudentCriteriaGroupDTO result = mapper.toDTO(null);

        assertNull(result);
    }

    @Test
    void toDTO_shouldReturnNullRubricCriteria_whenListIsNull() {
        final StudentCriteriaGroup group = StudentCriteriaGroup.builder()
                .studentId(100).studentName("Juan").studentSurnames("García")
                .rubricCriteria(null)
                .build();

        final StudentCriteriaGroupDTO result = mapper.toDTO(group);

        assertNotNull(result);
        assertNotNull(result.getStudent());
        assertNull(result.getRubricCriteria());
    }

    @Test
    void toDTO_shouldReturnEmptyRubricCriteria_whenListIsEmpty() {
        final StudentCriteriaGroup group = StudentCriteriaGroup.builder()
                .studentId(100).studentName("Juan").studentSurnames("García")
                .rubricCriteria(List.of())
                .build();

        final StudentCriteriaGroupDTO result = mapper.toDTO(group);

        assertNotNull(result);
        assertNotNull(result.getRubricCriteria());
        assertTrue(result.getRubricCriteria().isEmpty());
    }

    @Test
    void toDTO_shouldMapMultipleRubricCriteria() {
        final RubricCriterionAssignment assignment1 = RubricCriterionAssignment.builder()
                .id(1).classRubricId(10).rubricId(20).rubricTitle("Rubric A")
                .criterionId(30).criterionDescription("Mal").gradeStart(0).gradeEnd(4)
                .build();
        final RubricCriterionAssignment assignment2 = RubricCriterionAssignment.builder()
                .id(2).classRubricId(11).rubricId(21).rubricTitle("Rubric B")
                .criterionId(31).criterionDescription("Bien").gradeStart(7).gradeEnd(10)
                .build();
        final StudentCriteriaGroup group = StudentCriteriaGroup.builder()
                .studentId(100).studentName("Ana").studentSurnames("López")
                .rubricCriteria(Arrays.asList(assignment1, assignment2))
                .build();

        final StudentCriteriaGroupDTO result = mapper.toDTO(group);

        assertNotNull(result);
        assertEquals(2, result.getRubricCriteria().size());
        assertEquals("Rubric A", result.getRubricCriteria().get(0).getRubric().getTitle());
        assertEquals("Rubric B", result.getRubricCriteria().get(1).getRubric().getTitle());
    }

    @Test
    void toDTOList_shouldMapListCorrectly() {
        final StudentCriteriaGroup group1 = StudentCriteriaGroup.builder()
                .studentId(1).studentName("Juan").studentSurnames("García")
                .rubricCriteria(List.of(RubricCriterionAssignment.builder()
                        .id(10).classRubricId(100).rubricId(20).rubricTitle("R1")
                        .criterionId(30).criterionDescription("D1").gradeStart(0).gradeEnd(4).build()))
                .build();
        final StudentCriteriaGroup group2 = StudentCriteriaGroup.builder()
                .studentId(2).studentName("Ana").studentSurnames("López")
                .rubricCriteria(List.of(RubricCriterionAssignment.builder()
                        .id(11).classRubricId(101).rubricId(21).rubricTitle("R2")
                        .criterionId(31).criterionDescription("D2").gradeStart(5).gradeEnd(6).build()))
                .build();

        final List<StudentCriteriaGroupDTO> result = mapper.toDTOList(Arrays.asList(group1, group2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getStudent().getId());
        assertEquals("Juan", result.get(0).getStudent().getName());
        assertEquals(2, result.get(1).getStudent().getId());
        assertEquals("Ana", result.get(1).getStudent().getName());
    }

    @Test
    void toDTOList_shouldReturnEmptyList_whenInputIsEmpty() {
        final List<StudentCriteriaGroupDTO> result = mapper.toDTOList(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toDTOList_shouldReturnNull_whenInputIsNull() {
        final List<StudentCriteriaGroupDTO> result = mapper.toDTOList(null);

        assertNull(result);
    }

    @Test
    void toAssignmentDTO_shouldMapAllFields() {
        final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                .id(5).classRubricId(50).rubricId(60).rubricTitle("Expresión oral")
                .criterionId(70).criterionDescription("Se expresa con fluidez").gradeStart(7).gradeEnd(10)
                .build();

        final RubricCriterionAssignmentDTO result = mapper.toAssignmentDTO(assignment);

        assertNotNull(result);
        assertEquals(5, result.getId());
        assertEquals(50, result.getClassRubricId());
        assertNotNull(result.getRubric());
        assertEquals(60, result.getRubric().getId());
        assertEquals("Expresión oral", result.getRubric().getTitle());
        assertNotNull(result.getCriterion());
        assertEquals(70, result.getCriterion().getId());
        assertEquals("Se expresa con fluidez", result.getCriterion().getDescription());
        assertEquals(7, result.getCriterion().getGradeStart());
        assertEquals(10, result.getCriterion().getGradeEnd());
    }

    @Test
    void toAssignmentDTO_shouldReturnNull_whenAssignmentIsNull() {
        final RubricCriterionAssignmentDTO result = mapper.toAssignmentDTO(null);

        assertNull(result);
    }

    @Test
    void toAssignmentDTO_shouldHandleNullFields() {
        final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                .id(5).classRubricId(50)
                .rubricId(null).rubricTitle(null)
                .criterionId(null).criterionDescription(null).gradeStart(null).gradeEnd(null)
                .build();

        final RubricCriterionAssignmentDTO result = mapper.toAssignmentDTO(assignment);

        assertNotNull(result);
        assertEquals(5, result.getId());
        assertEquals(50, result.getClassRubricId());
        assertNotNull(result.getRubric());
        assertNull(result.getRubric().getId());
        assertNull(result.getRubric().getTitle());
        assertNotNull(result.getCriterion());
        assertNull(result.getCriterion().getId());
        assertNull(result.getCriterion().getDescription());
        assertNull(result.getCriterion().getGradeStart());
        assertNull(result.getCriterion().getGradeEnd());
    }

    @Test
    void toStudentSummary_shouldMapStudentFields() {
        final StudentCriteriaGroup group = StudentCriteriaGroup.builder()
                .studentId(42).studentName("María").studentSurnames("Pérez Sánchez")
                .build();

        final StudentSummaryDTO result = mapper.toStudentSummary(group);

        assertNotNull(result);
        assertEquals(42, result.getId());
        assertEquals("María", result.getName());
        assertEquals("Pérez Sánchez", result.getSurnames());
    }

    @Test
    void toRubricSummary_shouldMapRubricFields() {
        final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                .rubricId(99).rubricTitle("Competencia lingüística")
                .build();

        final RubricSummaryDTO result = mapper.toRubricSummary(assignment);

        assertNotNull(result);
        assertEquals(99, result.getId());
        assertEquals("Competencia lingüística", result.getTitle());
    }

    @Test
    void toCriterionSummary_shouldMapCriterionFields() {
        final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                .criterionId(77).criterionDescription("Lo hace regular").gradeStart(5).gradeEnd(6)
                .build();

        final CriterionSummaryDTO result = mapper.toCriterionSummary(assignment);

        assertNotNull(result);
        assertEquals(77, result.getId());
        assertEquals("Lo hace regular", result.getDescription());
        assertEquals(5, result.getGradeStart());
        assertEquals(6, result.getGradeEnd());
    }

    @Test
    void toCriterionSummary_shouldHandleZeroGradeRange() {
        final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                .criterionId(1).criterionDescription("Mínimo").gradeStart(0).gradeEnd(0)
                .build();

        final CriterionSummaryDTO result = mapper.toCriterionSummary(assignment);

        assertNotNull(result);
        assertEquals(0, result.getGradeStart());
        assertEquals(0, result.getGradeEnd());
    }

    @Test
    void toCriterionSummary_shouldHandleFullGradeRange() {
        final RubricCriterionAssignment assignment = RubricCriterionAssignment.builder()
                .criterionId(1).criterionDescription("Todo el rango").gradeStart(0).gradeEnd(10)
                .build();

        final CriterionSummaryDTO result = mapper.toCriterionSummary(assignment);

        assertNotNull(result);
        assertEquals(0, result.getGradeStart());
        assertEquals(10, result.getGradeEnd());
    }
}

