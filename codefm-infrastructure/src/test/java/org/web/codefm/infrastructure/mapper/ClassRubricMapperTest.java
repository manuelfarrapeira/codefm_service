package org.web.codefm.infrastructure.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassRubricEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClassRubricMapperTest {

    private final ClassRubricMapper mapper = new ClassRubricMapperImpl();

    @Test
    void toModel_shouldMapAllFields_whenAllFieldsArePresent() {
        final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, null);

        final ClassRubric result = mapper.toModel(entity);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(10, result.getClassId());
        assertEquals(50, result.getRubricId());
        assertNull(result.getDeletionDate());
    }

    @Test
    void toModel_shouldMapDeletionDate_whenPresent() {
        final LocalDate deletionDate = LocalDate.of(2026, 3, 15);
        final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, deletionDate);

        final ClassRubric result = mapper.toModel(entity);

        assertNotNull(result);
        assertEquals(deletionDate, result.getDeletionDate());
    }

    @Test
    void toModel_shouldIgnoreRubricTitleAndSkillIdAndCriteria() {
        final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, null);

        final ClassRubric result = mapper.toModel(entity);

        assertNotNull(result);
        assertNull(result.getRubricTitle());
        assertNull(result.getSkillId());
        assertNull(result.getCriteria());
    }

    @Test
    void toModel_shouldReturnNull_whenEntityIsNull() {
        final ClassRubric result = mapper.toModel(null);

        assertNull(result);
    }

    @Test
    void toModelList_shouldMapAllEntities() {
        final ClassRubricEntity entity1 = new ClassRubricEntity(1, 10, 50, null);
        final ClassRubricEntity entity2 = new ClassRubricEntity(2, 10, 60, null);

        final List<ClassRubric> result = mapper.toModelList(Arrays.asList(entity1, entity2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(50, result.get(0).getRubricId());
        assertEquals(2, result.get(1).getId());
        assertEquals(60, result.get(1).getRubricId());
    }

    @Test
    void toModelList_shouldReturnEmptyList_whenInputIsEmpty() {
        final List<ClassRubric> result = mapper.toModelList(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toModelList_shouldReturnNull_whenInputIsNull() {
        final List<ClassRubric> result = mapper.toModelList(null);

        assertNull(result);
    }

    @Test
    void toEntity_shouldMapAllFields_whenAllFieldsArePresent() {
        final ClassRubric model = ClassRubric.builder()
                .id(1).classId(10).rubricId(50).deletionDate(null).build();

        final ClassRubricEntity result = mapper.toEntity(model);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(10, result.getClassId());
        assertEquals(50, result.getRubricId());
        assertNull(result.getDeletionDate());
    }

    @Test
    void toEntity_shouldMapDeletionDate_whenPresent() {
        final LocalDate deletionDate = LocalDate.of(2026, 3, 15);
        final ClassRubric model = ClassRubric.builder()
                .id(1).classId(10).rubricId(50).deletionDate(deletionDate).build();

        final ClassRubricEntity result = mapper.toEntity(model);

        assertNotNull(result);
        assertEquals(deletionDate, result.getDeletionDate());
    }

    @Test
    void toEntity_shouldReturnNull_whenModelIsNull() {
        final ClassRubricEntity result = mapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void toEntity_shouldNotMapTransientDomainFields() {
        final ClassRubric model = ClassRubric.builder()
                .id(1).classId(10).rubricId(50)
                .rubricTitle("Rubric Title").skillId(5).criteria(List.of())
                .build();

        final ClassRubricEntity result = mapper.toEntity(model);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(10, result.getClassId());
        assertEquals(50, result.getRubricId());
    }

    @Test
    void toModelAndToEntity_shouldBeReversible() {
        final ClassRubricEntity original = new ClassRubricEntity(1, 10, 50, LocalDate.of(2026, 1, 1));

        final ClassRubric model = mapper.toModel(original);
        final ClassRubricEntity roundTrip = mapper.toEntity(model);

        assertEquals(original.getId(), roundTrip.getId());
        assertEquals(original.getClassId(), roundTrip.getClassId());
        assertEquals(original.getRubricId(), roundTrip.getRubricId());
        assertEquals(original.getDeletionDate(), roundTrip.getDeletionDate());
    }
}

