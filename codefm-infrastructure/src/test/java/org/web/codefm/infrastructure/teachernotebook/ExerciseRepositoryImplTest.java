package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectJPARepository;
import org.web.codefm.infrastructure.mapper.ExerciseMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseRepositoryImplTest {

    @Mock
    private ExerciseJPARepository exerciseJPARepository;

    @Mock
    private SubjectClassJPARepository subjectClassJPARepository;

    @Mock
    private SubjectJPARepository subjectJPARepository;

    @Mock
    private ExerciseMapper exerciseMapper;

    @InjectMocks
    private ExerciseRepositoryImpl exerciseRepository;

    @Test
    void findByClassId_shouldReturnExercisesWithSubjectData_whenSubjectClassesExist() {
        Integer classId = 1;
        SubjectClassEntity scEntity = new SubjectClassEntity(5, 1, classId, null);
        ExerciseEntity exerciseEntity = new ExerciseEntity(1, 5, "Exam", "Desc", 1, 30, 10, null);
        Exercise exercise = Exercise.builder().id(1).subjectClassId(5).title("Exam").percentageGrade(30).maxGrade(10).build();
        SubjectEntity subjectEntity = new SubjectEntity(1, "Mathematics", 1, null);

        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(classId)).thenReturn(List.of(scEntity));
        when(exerciseJPARepository.findBySubjectClassIdInAndDeletionDateIsNull(List.of(5))).thenReturn(List.of(exerciseEntity));
        when(exerciseMapper.toModelList(List.of(exerciseEntity))).thenReturn(List.of(exercise));
        when(subjectJPARepository.findAllById(List.of(1))).thenReturn(List.of(subjectEntity));

        List<Exercise> result = exerciseRepository.findByClassId(classId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getSubjectId());
        assertEquals("Mathematics", result.get(0).getSubjectName());
        verify(subjectClassJPARepository).findByClassIdAndDeletionDateIsNull(classId);
        verify(exerciseJPARepository).findBySubjectClassIdInAndDeletionDateIsNull(List.of(5));
    }

    @Test
    void findByClassId_shouldReturnEmptyList_whenNoSubjectClasses() {
        Integer classId = 1;

        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(classId)).thenReturn(new ArrayList<>());

        List<Exercise> result = exerciseRepository.findByClassId(classId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(exerciseJPARepository, never()).findBySubjectClassIdInAndDeletionDateIsNull(any());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnExerciseWithSubjectData_whenFound() {
        Integer id = 1;
        Integer teacherId = 1;
        ExerciseEntity entity = new ExerciseEntity(id, 5, "Exam", "Desc", 1, 30, 10, null);
        Exercise exercise = Exercise.builder().id(id).subjectClassId(5).title("Exam").percentageGrade(30).maxGrade(10).build();
        SubjectClassEntity scEntity = new SubjectClassEntity(5, 1, 10, null);
        SubjectEntity subjectEntity = new SubjectEntity(1, "Mathematics", 1, null);

        when(exerciseJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.of(entity));
        when(exerciseMapper.toModel(entity)).thenReturn(exercise);
        when(subjectClassJPARepository.findById(5)).thenReturn(Optional.of(scEntity));
        when(subjectJPARepository.findById(1)).thenReturn(Optional.of(subjectEntity));

        Optional<Exercise> result = exerciseRepository.findByIdAndTeacherId(id, teacherId);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals(1, result.get().getSubjectId());
        assertEquals("Mathematics", result.get().getSubjectName());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnEmpty_whenNotFound() {
        when(exerciseJPARepository.findByIdAndTeacherId(99, 1)).thenReturn(Optional.empty());

        Optional<Exercise> result = exerciseRepository.findByIdAndTeacherId(99, 1);

        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldSaveAndReturnExerciseWithSubjectData() {
        Exercise exercise = Exercise.builder().subjectClassId(5).title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();
        ExerciseEntity entity = new ExerciseEntity();
        ExerciseEntity savedEntity = new ExerciseEntity(1, 5, "Exam", null, 1, 30, 10, null);
        Exercise savedExercise = Exercise.builder().id(1).subjectClassId(5).title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();
        SubjectClassEntity scEntity = new SubjectClassEntity(5, 1, 10, null);
        SubjectEntity subjectEntity = new SubjectEntity(1, "Mathematics", 1, null);

        when(exerciseMapper.toEntity(exercise)).thenReturn(entity);
        when(exerciseJPARepository.save(entity)).thenReturn(savedEntity);
        when(exerciseMapper.toModel(savedEntity)).thenReturn(savedExercise);
        when(subjectClassJPARepository.findById(5)).thenReturn(Optional.of(scEntity));
        when(subjectJPARepository.findById(1)).thenReturn(Optional.of(subjectEntity));

        Exercise result = exerciseRepository.save(exercise);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getSubjectId());
        assertEquals("Mathematics", result.getSubjectName());
        verify(exerciseJPARepository).save(entity);
    }

    @Test
    void update_shouldUpdateAndReturnExerciseWithSubjectData() {
        Exercise exercise = Exercise.builder().id(1).subjectClassId(5).title("Updated").quarter(2).percentageGrade(50).maxGrade(12).build();
        ExerciseEntity entity = new ExerciseEntity(1, 5, "Updated", null, 2, 50, 12, null);
        ExerciseEntity savedEntity = new ExerciseEntity(1, 5, "Updated", null, 2, 50, 12, null);
        Exercise updatedExercise = Exercise.builder().id(1).subjectClassId(5).title("Updated").quarter(2).percentageGrade(50).maxGrade(12).build();
        SubjectClassEntity scEntity = new SubjectClassEntity(5, 1, 10, null);
        SubjectEntity subjectEntity = new SubjectEntity(1, "Mathematics", 1, null);

        when(exerciseMapper.toEntity(exercise)).thenReturn(entity);
        when(exerciseJPARepository.save(entity)).thenReturn(savedEntity);
        when(exerciseMapper.toModel(savedEntity)).thenReturn(updatedExercise);
        when(subjectClassJPARepository.findById(5)).thenReturn(Optional.of(scEntity));
        when(subjectJPARepository.findById(1)).thenReturn(Optional.of(subjectEntity));

        Exercise result = exerciseRepository.update(exercise);

        assertNotNull(result);
        assertEquals("Updated", result.getTitle());
        assertEquals(1, result.getSubjectId());
        assertEquals("Mathematics", result.getSubjectName());
    }

    @Test
    void softDelete_shouldCallJPARepository() {
        Integer id = 1;

        exerciseRepository.softDelete(id);

        verify(exerciseJPARepository).softDeleteById(id);
    }

    @Test
    void subjectClassBelongsToTeacher_shouldReturnTrue_whenBelongs() {
        when(exerciseJPARepository.subjectClassBelongsToTeacher(5, 1)).thenReturn(true);

        assertTrue(exerciseRepository.subjectClassBelongsToTeacher(5, 1));
    }

    @Test
    void subjectClassBelongsToTeacher_shouldReturnFalse_whenNotBelongs() {
        when(exerciseJPARepository.subjectClassBelongsToTeacher(5, 1)).thenReturn(false);

        assertFalse(exerciseRepository.subjectClassBelongsToTeacher(5, 1));
    }
}

