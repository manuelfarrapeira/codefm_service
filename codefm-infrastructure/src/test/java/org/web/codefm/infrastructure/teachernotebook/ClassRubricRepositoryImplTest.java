package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassRubricEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricCriteriaEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassRubricJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricCriteriaJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricJPARepository;
import org.web.codefm.infrastructure.mapper.ClassRubricMapper;
import org.web.codefm.infrastructure.mapper.SkillRubricCriteriaMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassRubricRepositoryImplTest {

    @Mock
    private ClassRubricJPARepository classRubricJPARepository;
    @Mock
    private SkillRubricJPARepository skillRubricJPARepository;
    @Mock
    private SkillRubricCriteriaJPARepository skillRubricCriteriaJPARepository;
    @Mock
    private ClassRubricMapper classRubricMapper;
    @Mock
    private SkillRubricCriteriaMapper skillRubricCriteriaMapper;

    @InjectMocks
    private ClassRubricRepositoryImpl classRubricRepository;

    @Test
    void findByClassId_shouldReturnEnrichedClassRubrics() {
        final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, null);
        final ClassRubric model = ClassRubric.builder().id(1).classId(10).rubricId(50).build();
        final SkillRubricEntity rubricEntity = new SkillRubricEntity(50, "Rubric Title", 5, null);
        final SkillRubricCriteriaEntity criteriaEntity = new SkillRubricCriteriaEntity(100, "Lo hace bien", 50, 7, 10, null);
        final SkillRubricCriteria criteriaModel = SkillRubricCriteria.builder().id(100).description("Lo hace bien").rubricId(50).gradeStart(7).gradeEnd(10).build();

        when(classRubricJPARepository.findByClassIdAndDeletionDateIsNull(10)).thenReturn(List.of(entity));
        when(classRubricMapper.toModelList(List.of(entity))).thenReturn(List.of(model));
        when(skillRubricJPARepository.findByIdAndDeletionDateIsNull(50)).thenReturn(Optional.of(rubricEntity));
        when(skillRubricCriteriaJPARepository.findActiveByRubricId(50)).thenReturn(List.of(criteriaEntity));
        when(skillRubricCriteriaMapper.toModelList(List.of(criteriaEntity))).thenReturn(List.of(criteriaModel));

        final List<ClassRubric> result = classRubricRepository.findByClassId(10);

        assertEquals(1, result.size());
        assertEquals("Rubric Title", result.get(0).getRubricTitle());
        assertEquals(5, result.get(0).getSkillId());
        assertEquals(1, result.get(0).getCriteria().size());
        assertEquals("Lo hace bien", result.get(0).getCriteria().get(0).getDescription());
    }

    @Test
    void findByClassId_shouldReturnEmptyList_whenNoClassRubricsExist() {
        when(classRubricJPARepository.findByClassIdAndDeletionDateIsNull(10)).thenReturn(List.of());
        when(classRubricMapper.toModelList(List.of())).thenReturn(List.of());

        final List<ClassRubric> result = classRubricRepository.findByClassId(10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByClassId_shouldHandleMissingRubricEntity() {
        final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, null);
        final ClassRubric model = ClassRubric.builder().id(1).classId(10).rubricId(50).build();

        when(classRubricJPARepository.findByClassIdAndDeletionDateIsNull(10)).thenReturn(List.of(entity));
        when(classRubricMapper.toModelList(List.of(entity))).thenReturn(List.of(model));
        when(skillRubricJPARepository.findByIdAndDeletionDateIsNull(50)).thenReturn(Optional.empty());
        when(skillRubricCriteriaJPARepository.findActiveByRubricId(50)).thenReturn(List.of());
        when(skillRubricCriteriaMapper.toModelList(List.of())).thenReturn(List.of());

        final List<ClassRubric> result = classRubricRepository.findByClassId(10);

        assertEquals(1, result.size());
        assertNull(result.get(0).getRubricTitle());
        assertNull(result.get(0).getSkillId());
        assertNotNull(result.get(0).getCriteria());
        assertTrue(result.get(0).getCriteria().isEmpty());
    }

    @Test
    void findByClassId_shouldEnrichMultipleClassRubrics() {
        final ClassRubricEntity entity1 = new ClassRubricEntity(1, 10, 50, null);
        final ClassRubricEntity entity2 = new ClassRubricEntity(2, 10, 60, null);
        final ClassRubric model1 = ClassRubric.builder().id(1).classId(10).rubricId(50).build();
        final ClassRubric model2 = ClassRubric.builder().id(2).classId(10).rubricId(60).build();

        when(classRubricJPARepository.findByClassIdAndDeletionDateIsNull(10)).thenReturn(Arrays.asList(entity1, entity2));
        when(classRubricMapper.toModelList(Arrays.asList(entity1, entity2))).thenReturn(Arrays.asList(model1, model2));
        when(skillRubricJPARepository.findByIdAndDeletionDateIsNull(50)).thenReturn(Optional.of(new SkillRubricEntity(50, "Rubric A", 5, null)));
        when(skillRubricJPARepository.findByIdAndDeletionDateIsNull(60)).thenReturn(Optional.of(new SkillRubricEntity(60, "Rubric B", 6, null)));
        when(skillRubricCriteriaJPARepository.findActiveByRubricId(50)).thenReturn(List.of());
        when(skillRubricCriteriaJPARepository.findActiveByRubricId(60)).thenReturn(List.of());
        when(skillRubricCriteriaMapper.toModelList(List.of())).thenReturn(List.of());

        final List<ClassRubric> result = classRubricRepository.findByClassId(10);

        assertEquals(2, result.size());
        assertEquals("Rubric A", result.get(0).getRubricTitle());
        assertEquals(5, result.get(0).getSkillId());
        assertEquals("Rubric B", result.get(1).getRubricTitle());
        assertEquals(6, result.get(1).getSkillId());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnClassRubric_whenFound() {
        final ClassRubricEntity entity = new ClassRubricEntity(1, 10, 50, null);
        final ClassRubric model = ClassRubric.builder().id(1).classId(10).rubricId(50).build();

        when(classRubricJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, 5)).thenReturn(Optional.of(entity));
        when(classRubricMapper.toModel(entity)).thenReturn(model);

        final Optional<ClassRubric> result = classRubricRepository.findByIdAndTeacherId(1, 5);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnEmpty_whenNotFound() {
        when(classRubricJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(999, 5)).thenReturn(Optional.empty());

        final Optional<ClassRubric> result = classRubricRepository.findByIdAndTeacherId(999, 5);

        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldMapAndPersist() {
        final ClassRubric classRubric = ClassRubric.builder().classId(10).rubricId(50).build();
        final ClassRubricEntity entity = new ClassRubricEntity(null, 10, 50, null);
        final ClassRubricEntity saved = new ClassRubricEntity(1, 10, 50, null);
        final ClassRubric savedModel = ClassRubric.builder().id(1).classId(10).rubricId(50).build();

        when(classRubricMapper.toEntity(classRubric)).thenReturn(entity);
        when(classRubricJPARepository.save(entity)).thenReturn(saved);
        when(classRubricMapper.toModel(saved)).thenReturn(savedModel);

        final ClassRubric result = classRubricRepository.save(classRubric);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(classRubricJPARepository).save(entity);
    }

    @Test
    void softDeleteById_shouldDelegate() {
        classRubricRepository.softDeleteById(1);
        verify(classRubricJPARepository).softDeleteById(1);
    }

    @Test
    void softDeleteByClassId_shouldDelegate() {
        classRubricRepository.softDeleteByClassId(10);
        verify(classRubricJPARepository).softDeleteByClassId(10);
    }

    @Test
    void softDeleteByRubricId_shouldDelegate() {
        classRubricRepository.softDeleteByRubricId(50);
        verify(classRubricJPARepository).softDeleteByRubricId(50);
    }

    @Test
    void findActiveIdsByClassId_shouldReturnIds() {
        when(classRubricJPARepository.findActiveIdsByClassId(10)).thenReturn(Arrays.asList(1, 2, 3));

        final List<Integer> result = classRubricRepository.findActiveIdsByClassId(10);

        assertEquals(3, result.size());
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

    @Test
    void findActiveIdsByClassId_shouldReturnEmptyList_whenNoneExist() {
        when(classRubricJPARepository.findActiveIdsByClassId(10)).thenReturn(List.of());

        final List<Integer> result = classRubricRepository.findActiveIdsByClassId(10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveIdsByRubricId_shouldReturnIds() {
        when(classRubricJPARepository.findActiveIdsByRubricId(50)).thenReturn(Arrays.asList(10, 20));

        final List<Integer> result = classRubricRepository.findActiveIdsByRubricId(50);

        assertEquals(2, result.size());
        assertEquals(Arrays.asList(10, 20), result);
    }

    @Test
    void findActiveIdsByRubricId_shouldReturnEmptyList_whenNoneExist() {
        when(classRubricJPARepository.findActiveIdsByRubricId(50)).thenReturn(List.of());

        final List<Integer> result = classRubricRepository.findActiveIdsByRubricId(50);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void existsByClassIdAndRubricIdAndDeletionDateIsNull_shouldReturnTrue_whenExists() {
        when(classRubricJPARepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(10, 50)).thenReturn(true);

        final boolean result = classRubricRepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(10, 50);

        assertTrue(result);
    }

    @Test
    void existsByClassIdAndRubricIdAndDeletionDateIsNull_shouldReturnFalse_whenNotExists() {
        when(classRubricJPARepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(10, 50)).thenReturn(false);

        final boolean result = classRubricRepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(10, 50);

        assertFalse(result);
    }
}

