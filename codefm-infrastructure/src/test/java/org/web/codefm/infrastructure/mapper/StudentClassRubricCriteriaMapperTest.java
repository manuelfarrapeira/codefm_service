package org.web.codefm.infrastructure.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassRubricCriteriaEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StudentClassRubricCriteriaMapperTest {

    private final StudentClassRubricCriteriaMapper mapper = new StudentClassRubricCriteriaMapperImpl();

    @Test
    void toModel_shouldMapPersistentFields_whenAllFieldsArePresent() {
        final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);

        final StudentClassRubricCriteria result = mapper.toModel(entity);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(100, result.getClassRubricId());
        assertEquals(200, result.getStudentId());
        assertEquals(300, result.getCriterionId());
        assertNull(result.getDeletionDate());
    }

    @Test
    void toModel_shouldMapDeletionDate_whenPresent() {
        final LocalDate deletionDate = LocalDate.of(2026, 3, 15);
        final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, deletionDate);

        final StudentClassRubricCriteria result = mapper.toModel(entity);

        assertNotNull(result);
        assertEquals(deletionDate, result.getDeletionDate());
    }

    @Test
    void toModel_shouldIgnoreEnrichmentFields() {
        final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);

        final StudentClassRubricCriteria result = mapper.toModel(entity);

        assertNotNull(result);
        assertNull(result.getRubricId());
        assertNull(result.getStudentName());
        assertNull(result.getStudentSurnames());
        assertNull(result.getCriterionDescription());
        assertNull(result.getQualification());
        assertNull(result.getGradeStart());
        assertNull(result.getGradeEnd());
    }

    @Test
    void toModel_shouldReturnNull_whenEntityIsNull() {
        final StudentClassRubricCriteria result = mapper.toModel(null);

        assertNull(result);
    }

    @Test
    void toModelList_shouldMapAllEntities() {
        final StudentClassRubricCriteriaEntity entity1 = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
        final StudentClassRubricCriteriaEntity entity2 = new StudentClassRubricCriteriaEntity(2, 101, 201, 301, null);

        final List<StudentClassRubricCriteria> result = mapper.toModelList(Arrays.asList(entity1, entity2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(100, result.get(0).getClassRubricId());
        assertEquals(2, result.get(1).getId());
        assertEquals(101, result.get(1).getClassRubricId());
    }

    @Test
    void toModelList_shouldReturnEmptyList_whenInputIsEmpty() {
        final List<StudentClassRubricCriteria> result = mapper.toModelList(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toModelList_shouldReturnNull_whenInputIsNull() {
        final List<StudentClassRubricCriteria> result = mapper.toModelList(null);

        assertNull(result);
    }

    @Test
    void toEntity_shouldMapPersistentFields() {
        final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(100).studentId(200).criterionId(300).deletionDate(null).build();

        final StudentClassRubricCriteriaEntity result = mapper.toEntity(model);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(100, result.getClassRubricId());
        assertEquals(200, result.getStudentId());
        assertEquals(300, result.getCriterionId());
        assertNull(result.getDeletionDate());
    }

    @Test
    void toEntity_shouldMapDeletionDate_whenPresent() {
        final LocalDate deletionDate = LocalDate.of(2026, 3, 15);
        final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(100).studentId(200).criterionId(300).deletionDate(deletionDate).build();

        final StudentClassRubricCriteriaEntity result = mapper.toEntity(model);

        assertNotNull(result);
        assertEquals(deletionDate, result.getDeletionDate());
    }

    @Test
    void toEntity_shouldReturnNull_whenModelIsNull() {
        final StudentClassRubricCriteriaEntity result = mapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void toEntity_shouldNotFailWhenDomainEnrichmentFieldsAreSet() {
        final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(100).studentId(200).criterionId(300)
                .rubricId(50).rubricTitle("Rubric").studentName("Juan").studentSurnames("García")
                .criterionDescription("Lo hace bien").qualification("Notable").gradeStart(7).gradeEnd(10)
                .build();

        final StudentClassRubricCriteriaEntity result = mapper.toEntity(model);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(100, result.getClassRubricId());
        assertEquals(200, result.getStudentId());
        assertEquals(300, result.getCriterionId());
    }

    @Test
    void toModelAndToEntity_shouldBeReversibleForPersistentFields() {
        final StudentClassRubricCriteriaEntity original =
                new StudentClassRubricCriteriaEntity(1, 100, 200, 300, LocalDate.of(2026, 1, 1));

        final StudentClassRubricCriteria model = mapper.toModel(original);
        final StudentClassRubricCriteriaEntity roundTrip = mapper.toEntity(model);

        assertEquals(original.getId(), roundTrip.getId());
        assertEquals(original.getClassRubricId(), roundTrip.getClassRubricId());
        assertEquals(original.getStudentId(), roundTrip.getStudentId());
        assertEquals(original.getCriterionId(), roundTrip.getCriterionId());
        assertEquals(original.getDeletionDate(), roundTrip.getDeletionDate());
    }
}

