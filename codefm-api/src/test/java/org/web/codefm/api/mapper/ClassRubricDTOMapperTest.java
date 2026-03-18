package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.model.ClassRubricDTO;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClassRubricDTOMapperTest {

    @Spy
    private SkillRubricCriteriaDTOMapperImpl skillRubricCriteriaDTOMapper;

    @InjectMocks
    private ClassRubricDTOMapperImpl mapper;

    @Test
    void toDTO_shouldMapAllFields_whenAllFieldsArePresent() {
        final SkillRubricCriteria criterion = SkillRubricCriteria.builder()
                .id(10).description("Lo hace bien").gradeStart(7).gradeEnd(10).build();
        final ClassRubric classRubric = ClassRubric.builder()
                .id(1).classId(20).rubricId(30).rubricTitle("Rubric A").skillId(5)
                .criteria(List.of(criterion))
                .build();

        final ClassRubricDTO result = mapper.toDTO(classRubric);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(20, result.getClassId());
        assertEquals(30, result.getRubricId());
        assertEquals("Rubric A", result.getRubricTitle());
        assertEquals(5, result.getSkillId());
        assertNotNull(result.getCriteria());
        assertEquals(1, result.getCriteria().size());
        assertEquals(10, result.getCriteria().get(0).getId());
        assertEquals("Lo hace bien", result.getCriteria().get(0).getDescription());
        assertEquals(7, result.getCriteria().get(0).getGradeStart());
        assertEquals(10, result.getCriteria().get(0).getGradeEnd());
    }

    @Test
    void toDTO_shouldReturnNull_whenInputIsNull() {
        final ClassRubricDTO result = mapper.toDTO(null);

        assertNull(result);
    }

    @Test
    void toDTO_shouldMapWithNullCriteria() {
        final ClassRubric classRubric = ClassRubric.builder()
                .id(1).classId(20).rubricId(30).rubricTitle("Rubric A").skillId(5)
                .criteria(null)
                .build();

        final ClassRubricDTO result = mapper.toDTO(classRubric);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNull(result.getCriteria());
    }

    @Test
    void toDTO_shouldMapWithEmptyCriteria() {
        final ClassRubric classRubric = ClassRubric.builder()
                .id(1).classId(20).rubricId(30).rubricTitle("Rubric A").skillId(5)
                .criteria(List.of())
                .build();

        final ClassRubricDTO result = mapper.toDTO(classRubric);

        assertNotNull(result);
        assertNotNull(result.getCriteria());
        assertTrue(result.getCriteria().isEmpty());
    }

    @Test
    void toDTO_shouldMapMultipleCriteria() {
        final SkillRubricCriteria criterion1 = SkillRubricCriteria.builder()
                .id(10).description("Mal").gradeStart(0).gradeEnd(4).build();
        final SkillRubricCriteria criterion2 = SkillRubricCriteria.builder()
                .id(11).description("Regular").gradeStart(5).gradeEnd(6).build();
        final SkillRubricCriteria criterion3 = SkillRubricCriteria.builder()
                .id(12).description("Bien").gradeStart(7).gradeEnd(10).build();
        final ClassRubric classRubric = ClassRubric.builder()
                .id(1).classId(20).rubricId(30).rubricTitle("Rubric A").skillId(5)
                .criteria(Arrays.asList(criterion1, criterion2, criterion3))
                .build();

        final ClassRubricDTO result = mapper.toDTO(classRubric);

        assertNotNull(result);
        assertEquals(3, result.getCriteria().size());
        assertEquals("Mal", result.getCriteria().get(0).getDescription());
        assertEquals("Regular", result.getCriteria().get(1).getDescription());
        assertEquals("Bien", result.getCriteria().get(2).getDescription());
    }

    @Test
    void toDTO_shouldHandleNullOptionalFields() {
        final ClassRubric classRubric = ClassRubric.builder()
                .id(1).classId(20).rubricId(30)
                .rubricTitle(null).skillId(null).criteria(null)
                .build();

        final ClassRubricDTO result = mapper.toDTO(classRubric);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(20, result.getClassId());
        assertEquals(30, result.getRubricId());
        assertNull(result.getRubricTitle());
        assertNull(result.getSkillId());
        assertNull(result.getCriteria());
    }

    @Test
    void toDTOList_shouldMapAllClassRubrics() {
        final ClassRubric rubric1 = ClassRubric.builder()
                .id(1).classId(20).rubricId(30).rubricTitle("Rubric A").skillId(5)
                .criteria(List.of(SkillRubricCriteria.builder()
                        .id(10).description("Desc A").gradeStart(0).gradeEnd(4).build()))
                .build();
        final ClassRubric rubric2 = ClassRubric.builder()
                .id(2).classId(20).rubricId(40).rubricTitle("Rubric B").skillId(6)
                .criteria(List.of(SkillRubricCriteria.builder()
                        .id(20).description("Desc B").gradeStart(5).gradeEnd(10).build()))
                .build();

        final List<ClassRubricDTO> result = mapper.toDTOList(Arrays.asList(rubric1, rubric2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Rubric A", result.get(0).getRubricTitle());
        assertEquals(1, result.get(0).getCriteria().size());
        assertEquals(2, result.get(1).getId());
        assertEquals("Rubric B", result.get(1).getRubricTitle());
        assertEquals(1, result.get(1).getCriteria().size());
    }

    @Test
    void toDTOList_shouldReturnEmptyList_whenInputIsEmpty() {
        final List<ClassRubricDTO> result = mapper.toDTOList(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toDTOList_shouldReturnNull_whenInputIsNull() {
        final List<ClassRubricDTO> result = mapper.toDTOList(null);

        assertNull(result);
    }
}

