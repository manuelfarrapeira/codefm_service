package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.*;
import org.web.codefm.infrastructure.jpa.teachernotebook.*;
import org.web.codefm.infrastructure.mapper.ExerciseStudentGradeMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseStudentGradeRepositoryImplTest {

    @Mock
    private ExerciseStudentGradeJPARepository exerciseStudentGradeJPARepository;

    @Mock
    private ExerciseJPARepository exerciseJPARepository;

    @Mock
    private SubjectClassJPARepository subjectClassJPARepository;

    @Mock
    private SubjectJPARepository subjectJPARepository;

    @Mock
    private StudentJPARepository studentJPARepository;

    @Mock
    private ExerciseStudentGradeMapper exerciseStudentGradeMapper;

    @InjectMocks
    private ExerciseStudentGradeRepositoryImpl exerciseStudentGradeRepository;

    @Test
    void findByClassId_shouldReturnEnrichedGrades_whenDataExists() {
        SubjectClassEntity scEntity = new SubjectClassEntity(10, 1, 1, null);
        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(1)).thenReturn(List.of(scEntity));
        when(exerciseJPARepository.findActiveIdsBySubjectClassIds(List.of(10))).thenReturn(List.of(100));

        ExerciseStudentGradeEntity gradeEntity = new ExerciseStudentGradeEntity(1, 5, 100, 8, "Good", null);
        when(exerciseStudentGradeJPARepository.findByExerciseIdInAndDeletionDateIsNull(List.of(100))).thenReturn(List.of(gradeEntity));
        ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(1).studentId(5).exerciseId(100).grade(8).build();
        when(exerciseStudentGradeMapper.toModelList(List.of(gradeEntity))).thenReturn(new ArrayList<>(List.of(grade)));

        ExerciseEntity exerciseEntity = new ExerciseEntity(100, 10, "Exam", "Desc", 1, 30, 10, null);
        when(exerciseJPARepository.findAllById(List.of(100))).thenReturn(List.of(exerciseEntity));
        when(subjectClassJPARepository.findAllById(List.of(10))).thenReturn(List.of(scEntity));
        SubjectEntity subjectEntity = new SubjectEntity(1, "Math", 1, null);
        when(subjectJPARepository.findAllById(List.of(1))).thenReturn(List.of(subjectEntity));
        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setId(5);
        studentEntity.setName("Juan");
        studentEntity.setSurnames("García");
        when(studentJPARepository.findAllById(List.of(5))).thenReturn(List.of(studentEntity));

        List<ExerciseStudentGrade> result = exerciseStudentGradeRepository.findByClassId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Math", result.get(0).getSubjectName());
        assertEquals("Juan", result.get(0).getStudentName());
    }

    @Test
    void findByClassId_shouldReturnEmptyList_whenNoSubjectClasses() {
        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(1)).thenReturn(new ArrayList<>());

        List<ExerciseStudentGrade> result = exerciseStudentGradeRepository.findByClassId(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByClassIdAndStudentId_shouldReturnGrades_whenDataExists() {
        SubjectClassEntity scEntity = new SubjectClassEntity(10, 1, 1, null);
        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(1)).thenReturn(List.of(scEntity));
        when(exerciseJPARepository.findActiveIdsBySubjectClassIds(List.of(10))).thenReturn(List.of(100));

        ExerciseStudentGradeEntity gradeEntity = new ExerciseStudentGradeEntity(1, 5, 100, 8, "Good", null);
        when(exerciseStudentGradeJPARepository.findByExerciseIdInAndStudentIdAndDeletionDateIsNull(List.of(100), 5)).thenReturn(List.of(gradeEntity));
        ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(1).studentId(5).exerciseId(100).grade(8).build();
        when(exerciseStudentGradeMapper.toModelList(List.of(gradeEntity))).thenReturn(new ArrayList<>(List.of(grade)));

        ExerciseEntity exerciseEntity = new ExerciseEntity(100, 10, "Exam", "Desc", 1, 30, 10, null);
        when(exerciseJPARepository.findAllById(List.of(100))).thenReturn(List.of(exerciseEntity));
        when(subjectClassJPARepository.findAllById(List.of(10))).thenReturn(List.of(scEntity));
        SubjectEntity subjectEntity = new SubjectEntity(1, "Math", 1, null);
        when(subjectJPARepository.findAllById(List.of(1))).thenReturn(List.of(subjectEntity));
        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setId(5);
        studentEntity.setName("Juan");
        studentEntity.setSurnames("García");
        when(studentJPARepository.findAllById(List.of(5))).thenReturn(List.of(studentEntity));

        List<ExerciseStudentGrade> result = exerciseStudentGradeRepository.findByClassIdAndStudentId(1, 5);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findByClassIdAndStudentId_shouldReturnEmptyList_whenNoExercises() {
        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(1)).thenReturn(new ArrayList<>());

        List<ExerciseStudentGrade> result = exerciseStudentGradeRepository.findByClassIdAndStudentId(1, 5);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnEnrichedGrade_whenFound() {
        ExerciseStudentGradeEntity entity = new ExerciseStudentGradeEntity(1, 5, 100, 8, "Good", null);
        when(exerciseStudentGradeJPARepository.findByIdAndTeacherId(1, 1)).thenReturn(Optional.of(entity));
        ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(1).studentId(5).exerciseId(100).grade(8).build();
        when(exerciseStudentGradeMapper.toModel(entity)).thenReturn(grade);

        ExerciseEntity exerciseEntity = new ExerciseEntity(100, 10, "Exam", "Desc", 1, 30, 10, null);
        when(exerciseJPARepository.findById(100)).thenReturn(Optional.of(exerciseEntity));
        SubjectClassEntity scEntity = new SubjectClassEntity(10, 1, 1, null);
        when(subjectClassJPARepository.findById(10)).thenReturn(Optional.of(scEntity));
        SubjectEntity subjectEntity = new SubjectEntity(1, "Math", 1, null);
        when(subjectJPARepository.findById(1)).thenReturn(Optional.of(subjectEntity));
        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setId(5);
        studentEntity.setName("Juan");
        studentEntity.setSurnames("García");
        when(studentJPARepository.findById(5)).thenReturn(Optional.of(studentEntity));

        Optional<ExerciseStudentGrade> result = exerciseStudentGradeRepository.findByIdAndTeacherId(1, 1);

        assertTrue(result.isPresent());
        assertEquals("Math", result.get().getSubjectName());
        assertEquals("Juan", result.get().getStudentName());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnEmpty_whenNotFound() {
        when(exerciseStudentGradeJPARepository.findByIdAndTeacherId(99, 1)).thenReturn(Optional.empty());

        Optional<ExerciseStudentGrade> result = exerciseStudentGradeRepository.findByIdAndTeacherId(99, 1);

        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldSaveAndReturnEnrichedGrade() {
        ExerciseStudentGrade grade = ExerciseStudentGrade.builder().studentId(5).exerciseId(100).grade(8).build();
        ExerciseStudentGradeEntity entity = new ExerciseStudentGradeEntity();
        ExerciseStudentGradeEntity savedEntity = new ExerciseStudentGradeEntity(1, 5, 100, 8, null, null);
        ExerciseStudentGrade saved = ExerciseStudentGrade.builder().id(1).studentId(5).exerciseId(100).grade(8).build();

        when(exerciseStudentGradeMapper.toEntity(grade)).thenReturn(entity);
        when(exerciseStudentGradeJPARepository.save(entity)).thenReturn(savedEntity);
        when(exerciseStudentGradeMapper.toModel(savedEntity)).thenReturn(saved);

        ExerciseEntity exerciseEntity = new ExerciseEntity(100, 10, "Exam", "Desc", 1, 30, 10, null);
        when(exerciseJPARepository.findById(100)).thenReturn(Optional.of(exerciseEntity));
        SubjectClassEntity scEntity = new SubjectClassEntity(10, 1, 1, null);
        when(subjectClassJPARepository.findById(10)).thenReturn(Optional.of(scEntity));
        SubjectEntity subjectEntity = new SubjectEntity(1, "Math", 1, null);
        when(subjectJPARepository.findById(1)).thenReturn(Optional.of(subjectEntity));
        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setId(5);
        studentEntity.setName("Juan");
        studentEntity.setSurnames("García");
        when(studentJPARepository.findById(5)).thenReturn(Optional.of(studentEntity));

        ExerciseStudentGrade result = exerciseStudentGradeRepository.save(grade);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void softDelete_shouldCallJPARepository() {
        exerciseStudentGradeRepository.softDelete(1);

        verify(exerciseStudentGradeJPARepository).softDeleteById(1);
    }

    @Test
    void existsByStudentIdAndExerciseIdAndDeletionDateIsNull_shouldReturnTrue() {
        when(exerciseStudentGradeJPARepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(5, 100)).thenReturn(true);

        assertTrue(exerciseStudentGradeRepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(5, 100));
    }

    @Test
    void existsByStudentIdAndExerciseIdAndDeletionDateIsNull_shouldReturnFalse() {
        when(exerciseStudentGradeJPARepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(5, 100)).thenReturn(false);

        assertFalse(exerciseStudentGradeRepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(5, 100));
    }

    @Test
    void softDeleteByExerciseIds_shouldCallJPARepository_whenListNotEmpty() {
        List<Integer> exerciseIds = List.of(1, 2, 3);

        exerciseStudentGradeRepository.softDeleteByExerciseIds(exerciseIds);

        verify(exerciseStudentGradeJPARepository).softDeleteByExerciseIds(exerciseIds);
    }

    @Test
    void softDeleteByExerciseIds_shouldNotCallJPARepository_whenListEmpty() {
        exerciseStudentGradeRepository.softDeleteByExerciseIds(List.of());

        verify(exerciseStudentGradeJPARepository, never()).softDeleteByExerciseIds(any());
    }

    @Test
    void softDeleteByExerciseIds_shouldNotCallJPARepository_whenListNull() {
        exerciseStudentGradeRepository.softDeleteByExerciseIds(null);

        verify(exerciseStudentGradeJPARepository, never()).softDeleteByExerciseIds(any());
    }
}

