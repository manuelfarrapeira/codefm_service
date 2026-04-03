package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.*;
import org.web.codefm.infrastructure.jpa.teachernotebook.*;
import org.web.codefm.infrastructure.mapper.StudentClassRubricCriteriaMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentClassRubricCriteriaRepositoryImplTest {

    @Mock
    private StudentClassRubricCriteriaJPARepository studentClassRubricCriteriaJPARepository;
    @Mock
    private ClassRubricJPARepository classRubricJPARepository;
    @Mock
    private StudentJPARepository studentJPARepository;
    @Mock
    private SkillRubricCriteriaJPARepository skillRubricCriteriaJPARepository;
    @Mock
    private SkillRubricJPARepository skillRubricJPARepository;
    @Mock
    private StudentClassRubricCriteriaMapper studentClassRubricCriteriaMapper;

    @InjectMocks
    private StudentClassRubricCriteriaRepositoryImpl studentClassRubricCriteriaRepository;

    @Test
    void findByClassId_shouldReturnEnrichedCriteria() {
        final Integer classId = 10;
        final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
        final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(100).studentId(200).criterionId(300).build();

        when(studentClassRubricCriteriaJPARepository.findByClassIdAndDeletionDateIsNull(classId))
                .thenReturn(List.of(entity));
        when(studentClassRubricCriteriaMapper.toModelList(List.of(entity))).thenReturn(List.of(model));
        when(classRubricJPARepository.findByIdAndDeletionDateIsNull(100))
                .thenReturn(Optional.of(new ClassRubricEntity(100, classId, 50, null)));
        when(skillRubricJPARepository.findById(50))
                .thenReturn(Optional.of(new SkillRubricEntity(50, "Rubric Title", 1, null)));
        when(studentJPARepository.findById(200))
                .thenReturn(Optional.of(new StudentEntity(200, 1, "Juan", "García", null, "M", null, null, null, null)));
        when(skillRubricCriteriaJPARepository.findById(300))
                .thenReturn(Optional.of(new SkillRubricCriteriaEntity(300, "Lo hace bien", "Notable", 50, 5, 10, null)));

        final List<StudentClassRubricCriteria> result = studentClassRubricCriteriaRepository.findByClassId(classId);

        assertEquals(1, result.size());
        final StudentClassRubricCriteria criteria = result.get(0);
        assertEquals(50, criteria.getRubricId());
        assertEquals("Rubric Title", criteria.getRubricTitle());
        assertEquals("Juan", criteria.getStudentName());
        assertEquals("García", criteria.getStudentSurnames());
        assertEquals("Lo hace bien", criteria.getCriterionDescription());
        assertEquals("Notable", criteria.getQualification());
        assertEquals(5, criteria.getGradeStart());
        assertEquals(10, criteria.getGradeEnd());
    }

    @Test
    void findByClassId_shouldReturnEmptyList_whenNoCriteriaExist() {
        final Integer classId = 10;

        when(studentClassRubricCriteriaJPARepository.findByClassIdAndDeletionDateIsNull(classId))
                .thenReturn(List.of());
        when(studentClassRubricCriteriaMapper.toModelList(List.of())).thenReturn(List.of());

        final List<StudentClassRubricCriteria> result = studentClassRubricCriteriaRepository.findByClassId(classId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByClassId_shouldHandlePartialEnrichment_whenClassRubricNotFound() {
        final Integer classId = 10;
        final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
        final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(100).studentId(200).criterionId(300).build();

        when(studentClassRubricCriteriaJPARepository.findByClassIdAndDeletionDateIsNull(classId))
                .thenReturn(List.of(entity));
        when(studentClassRubricCriteriaMapper.toModelList(List.of(entity))).thenReturn(List.of(model));
        when(classRubricJPARepository.findByIdAndDeletionDateIsNull(100)).thenReturn(Optional.empty());
        when(studentJPARepository.findById(200)).thenReturn(Optional.empty());
        when(skillRubricCriteriaJPARepository.findById(300)).thenReturn(Optional.empty());

        final List<StudentClassRubricCriteria> result = studentClassRubricCriteriaRepository.findByClassId(classId);

        assertEquals(1, result.size());
        assertNull(result.get(0).getRubricId());
        assertNull(result.get(0).getRubricTitle());
        assertNull(result.get(0).getStudentName());
        assertNull(result.get(0).getCriterionDescription());
    }

    @Test
    void findByClassId_shouldHandlePartialEnrichment_whenRubricEntityNotFound() {
        final Integer classId = 10;
        final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
        final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(100).studentId(200).criterionId(300).build();

        when(studentClassRubricCriteriaJPARepository.findByClassIdAndDeletionDateIsNull(classId))
                .thenReturn(List.of(entity));
        when(studentClassRubricCriteriaMapper.toModelList(List.of(entity))).thenReturn(List.of(model));
        when(classRubricJPARepository.findByIdAndDeletionDateIsNull(100))
                .thenReturn(Optional.of(new ClassRubricEntity(100, classId, 50, null)));
        when(skillRubricJPARepository.findById(50)).thenReturn(Optional.empty());
        when(studentJPARepository.findById(200)).thenReturn(Optional.empty());
        when(skillRubricCriteriaJPARepository.findById(300)).thenReturn(Optional.empty());

        final List<StudentClassRubricCriteria> result = studentClassRubricCriteriaRepository.findByClassId(classId);

        assertEquals(1, result.size());
        assertEquals(50, result.get(0).getRubricId());
        assertNull(result.get(0).getRubricTitle());
    }

    @Test
    void findByClassIdAndStudentId_shouldReturnEnrichedCriteria() {
        final Integer classId = 10;
        final Integer studentId = 200;
        final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, studentId, 300, null);
        final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(100).studentId(studentId).criterionId(300).build();

        when(studentClassRubricCriteriaJPARepository.findByClassIdAndStudentIdAndDeletionDateIsNull(classId, studentId))
                .thenReturn(List.of(entity));
        when(studentClassRubricCriteriaMapper.toModelList(List.of(entity))).thenReturn(List.of(model));
        when(classRubricJPARepository.findByIdAndDeletionDateIsNull(100))
                .thenReturn(Optional.of(new ClassRubricEntity(100, classId, 50, null)));
        when(skillRubricJPARepository.findById(50))
                .thenReturn(Optional.of(new SkillRubricEntity(50, "Rubric Title", 1, null)));
        when(studentJPARepository.findById(studentId))
                .thenReturn(Optional.of(new StudentEntity(studentId, 1, "Ana", "López", null, "F", null, null, null, null)));
        when(skillRubricCriteriaJPARepository.findById(300))
                .thenReturn(Optional.of(new SkillRubricCriteriaEntity(300, "Lo hace regular", "Suficiente", 50, 5, 6, null)));

        final List<StudentClassRubricCriteria> result =
                studentClassRubricCriteriaRepository.findByClassIdAndStudentId(classId, studentId);

        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).getStudentName());
        assertEquals("López", result.get(0).getStudentSurnames());
        assertEquals("Lo hace regular", result.get(0).getCriterionDescription());
        assertEquals("Suficiente", result.get(0).getQualification());
    }

    @Test
    void findByClassIdAndStudentId_shouldReturnEmptyList_whenNoCriteriaExist() {
        final Integer classId = 10;
        final Integer studentId = 200;

        when(studentClassRubricCriteriaJPARepository.findByClassIdAndStudentIdAndDeletionDateIsNull(classId, studentId))
                .thenReturn(List.of());
        when(studentClassRubricCriteriaMapper.toModelList(List.of())).thenReturn(List.of());

        final List<StudentClassRubricCriteria> result =
                studentClassRubricCriteriaRepository.findByClassIdAndStudentId(classId, studentId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnCriteria_whenFound() {
        final Integer id = 1;
        final Integer teacherId = 5;
        final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(id, 100, 200, 300, null);
        final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                .id(id).classRubricId(100).studentId(200).criterionId(300).build();

        when(studentClassRubricCriteriaJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(id, teacherId))
                .thenReturn(Optional.of(entity));
        when(studentClassRubricCriteriaMapper.toModel(entity)).thenReturn(model);

        final Optional<StudentClassRubricCriteria> result =
                studentClassRubricCriteriaRepository.findByIdAndTeacherId(id, teacherId);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnEmpty_whenNotFound() {
        final Integer id = 999;
        final Integer teacherId = 5;

        when(studentClassRubricCriteriaJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(id, teacherId))
                .thenReturn(Optional.empty());

        final Optional<StudentClassRubricCriteria> result =
                studentClassRubricCriteriaRepository.findByIdAndTeacherId(id, teacherId);

        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldMapAndPersistAndEnrich() {
        final StudentClassRubricCriteria criteria = StudentClassRubricCriteria.builder()
                .classRubricId(100).studentId(200).criterionId(300).build();
        final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(null, 100, 200, 300, null);
        final StudentClassRubricCriteriaEntity saved = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
        final StudentClassRubricCriteria savedModel = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(100).studentId(200).criterionId(300).build();

        when(studentClassRubricCriteriaMapper.toEntity(criteria)).thenReturn(entity);
        when(studentClassRubricCriteriaJPARepository.save(entity)).thenReturn(saved);
        when(studentClassRubricCriteriaMapper.toModel(saved)).thenReturn(savedModel);
        when(classRubricJPARepository.findByIdAndDeletionDateIsNull(100))
                .thenReturn(Optional.of(new ClassRubricEntity(100, 10, 50, null)));
        when(skillRubricJPARepository.findById(50))
                .thenReturn(Optional.of(new SkillRubricEntity(50, "Rubric Title", 1, null)));
        when(studentJPARepository.findById(200))
                .thenReturn(Optional.of(new StudentEntity(200, 1, "Juan", "García", null, "M", null, null, null, null)));
        when(skillRubricCriteriaJPARepository.findById(300))
                .thenReturn(Optional.of(new SkillRubricCriteriaEntity(300, "Lo hace bien", "Notable", 50, 7, 10, null)));

        final StudentClassRubricCriteria result = studentClassRubricCriteriaRepository.save(criteria);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Rubric Title", result.getRubricTitle());
        assertEquals("Juan", result.getStudentName());
        assertEquals("García", result.getStudentSurnames());
        assertEquals("Lo hace bien", result.getCriterionDescription());
        assertEquals("Notable", result.getQualification());
        assertEquals(7, result.getGradeStart());
        assertEquals(10, result.getGradeEnd());
        verify(studentClassRubricCriteriaJPARepository).save(entity);
    }

    @Test
    void softDeleteById_shouldDelegate() {
        studentClassRubricCriteriaRepository.softDeleteById(1);
        verify(studentClassRubricCriteriaJPARepository).softDeleteById(1);
    }

    @Test
    void softDeleteByClassRubricId_shouldDelegate() {
        studentClassRubricCriteriaRepository.softDeleteByClassRubricId(100);
        verify(studentClassRubricCriteriaJPARepository).softDeleteByClassRubricId(100);
    }

    @Test
    void softDeleteByCriterionId_shouldDelegate() {
        studentClassRubricCriteriaRepository.softDeleteByCriterionId(300);
        verify(studentClassRubricCriteriaJPARepository).softDeleteByCriterionId(300);
    }

    @Test
    void softDeleteByClassRubricIds_shouldDelegate_whenNotEmpty() {
        studentClassRubricCriteriaRepository.softDeleteByClassRubricIds(Arrays.asList(1, 2, 3));
        verify(studentClassRubricCriteriaJPARepository).softDeleteByClassRubricIds(Arrays.asList(1, 2, 3));
    }

    @Test
    void softDeleteByClassRubricIds_shouldNotDelegate_whenEmpty() {
        studentClassRubricCriteriaRepository.softDeleteByClassRubricIds(List.of());
        verify(studentClassRubricCriteriaJPARepository, never()).softDeleteByClassRubricIds(any());
    }

    @Test
    void existsByClassRubricIdAndStudentIdAndDeletionDateIsNull_shouldReturnTrue_whenExists() {
        when(studentClassRubricCriteriaJPARepository
                .existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(100, 200)).thenReturn(true);

        final boolean result = studentClassRubricCriteriaRepository
                .existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(100, 200);

        assertTrue(result);
    }

    @Test
    void existsByClassRubricIdAndStudentIdAndDeletionDateIsNull_shouldReturnFalse_whenNotExists() {
        when(studentClassRubricCriteriaJPARepository
                .existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(100, 200)).thenReturn(false);

        final boolean result = studentClassRubricCriteriaRepository
                .existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(100, 200);

        assertFalse(result);
    }

    @Test
    void findByClassId_shouldEnrichMultipleCriteria() {
        final Integer classId = 10;
        final StudentClassRubricCriteriaEntity entity1 = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
        final StudentClassRubricCriteriaEntity entity2 = new StudentClassRubricCriteriaEntity(2, 101, 201, 301, null);
        final StudentClassRubricCriteria model1 = StudentClassRubricCriteria.builder()
                .id(1).classRubricId(100).studentId(200).criterionId(300).build();
        final StudentClassRubricCriteria model2 = StudentClassRubricCriteria.builder()
                .id(2).classRubricId(101).studentId(201).criterionId(301).build();

        when(studentClassRubricCriteriaJPARepository.findByClassIdAndDeletionDateIsNull(classId))
                .thenReturn(Arrays.asList(entity1, entity2));
        when(studentClassRubricCriteriaMapper.toModelList(Arrays.asList(entity1, entity2)))
                .thenReturn(Arrays.asList(model1, model2));

        when(classRubricJPARepository.findByIdAndDeletionDateIsNull(100))
                .thenReturn(Optional.of(new ClassRubricEntity(100, classId, 50, null)));
        when(classRubricJPARepository.findByIdAndDeletionDateIsNull(101))
                .thenReturn(Optional.of(new ClassRubricEntity(101, classId, 51, null)));

        when(skillRubricJPARepository.findById(50))
                .thenReturn(Optional.of(new SkillRubricEntity(50, "Rubric A", 1, null)));
        when(skillRubricJPARepository.findById(51))
                .thenReturn(Optional.of(new SkillRubricEntity(51, "Rubric B", 2, null)));

        when(studentJPARepository.findById(200))
                .thenReturn(Optional.of(new StudentEntity(200, 1, "Juan", "García", null, "M", null, null, null, null)));
        when(studentJPARepository.findById(201))
                .thenReturn(Optional.of(new StudentEntity(201, 1, "Ana", "López", null, "F", null, null, null, null)));

        when(skillRubricCriteriaJPARepository.findById(300))
                .thenReturn(Optional.of(new SkillRubricCriteriaEntity(300, "Mal", "Insuficiente", 50, 0, 4, null)));
        when(skillRubricCriteriaJPARepository.findById(301))
                .thenReturn(Optional.of(new SkillRubricCriteriaEntity(301, "Bien", "Notable", 51, 7, 10, null)));

        final List<StudentClassRubricCriteria> result = studentClassRubricCriteriaRepository.findByClassId(classId);

        assertEquals(2, result.size());
        assertEquals("Rubric A", result.get(0).getRubricTitle());
        assertEquals("Juan", result.get(0).getStudentName());
        assertEquals("Mal", result.get(0).getCriterionDescription());
        assertEquals("Rubric B", result.get(1).getRubricTitle());
        assertEquals("Ana", result.get(1).getStudentName());
        assertEquals("Bien", result.get(1).getCriterionDescription());
    }
}

