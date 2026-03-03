package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentClassJPARepository;
import org.web.codefm.infrastructure.mapper.StudentClassMapper;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentClassRepositoryImplTest {

    @Mock
    private StudentClassJPARepository studentClassJPARepository;

    @Mock
    private StudentClassMapper studentClassMapper;

    @InjectMocks
    private StudentClassRepositoryImpl studentClassRepository;

    private final Integer classId = 10;
    private final Integer studentId = 20;

    @Test
    void findByClassIdAndStudentId_shouldReturnStudentClass_whenExists() {
        StudentClassEntity entity = new StudentClassEntity(1, classId, studentId, null);
        StudentClass studentClass = StudentClass.builder()
                .id(1)
                .classId(classId)
                .studentId(studentId)
                .build();

        when(studentClassJPARepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.of(entity));
        when(studentClassMapper.toModel(entity)).thenReturn(studentClass);

        Optional<StudentClass> result = studentClassRepository.findByClassIdAndStudentId(classId, studentId);

        assertTrue(result.isPresent());
        assertEquals(classId, result.get().getClassId());
        assertEquals(studentId, result.get().getStudentId());
        verify(studentClassJPARepository).findByClassIdAndStudentId(classId, studentId);
        verify(studentClassMapper).toModel(entity);
    }

    @Test
    void findByClassIdAndStudentId_shouldReturnEmpty_whenNotExists() {
        when(studentClassJPARepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.empty());

        Optional<StudentClass> result = studentClassRepository.findByClassIdAndStudentId(classId, studentId);

        assertFalse(result.isPresent());
        verify(studentClassJPARepository).findByClassIdAndStudentId(classId, studentId);
        verify(studentClassMapper, never()).toModel(any());
    }

    @Test
    void save_shouldSaveAndReturnStudentClass() {
        StudentClass studentClass = StudentClass.builder()
                .classId(classId)
                .studentId(studentId)
                .build();
        StudentClassEntity entity = new StudentClassEntity(null, classId, studentId, null);
        StudentClassEntity savedEntity = new StudentClassEntity(1, classId, studentId, null);
        StudentClass savedStudentClass = StudentClass.builder()
                .id(1)
                .classId(classId)
                .studentId(studentId)
                .build();

        when(studentClassMapper.toEntity(studentClass)).thenReturn(entity);
        when(studentClassJPARepository.save(entity)).thenReturn(savedEntity);
        when(studentClassMapper.toModel(savedEntity)).thenReturn(savedStudentClass);

        StudentClass result = studentClassRepository.save(studentClass);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(classId, result.getClassId());
        assertEquals(studentId, result.getStudentId());
        verify(studentClassMapper).toEntity(studentClass);
        verify(studentClassJPARepository).save(entity);
        verify(studentClassMapper).toModel(savedEntity);
    }

    @Test
    void update_shouldUpdateAndReturnStudentClass() {
        StudentClass studentClass = StudentClass.builder()
                .id(1)
                .classId(classId)
                .studentId(studentId)
                .deletionDate(null)
                .build();
        StudentClassEntity entity = new StudentClassEntity(1, classId, studentId, null);
        StudentClassEntity updatedEntity = new StudentClassEntity(1, classId, studentId, null);
        StudentClass updatedStudentClass = StudentClass.builder()
                .id(1)
                .classId(classId)
                .studentId(studentId)
                .deletionDate(null)
                .build();

        when(studentClassMapper.toEntity(studentClass)).thenReturn(entity);
        when(studentClassJPARepository.save(entity)).thenReturn(updatedEntity);
        when(studentClassMapper.toModel(updatedEntity)).thenReturn(updatedStudentClass);

        StudentClass result = studentClassRepository.update(studentClass);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNull(result.getDeletionDate());
        verify(studentClassMapper).toEntity(studentClass);
        verify(studentClassJPARepository).save(entity);
        verify(studentClassMapper).toModel(updatedEntity);
    }

    @Test
    void softDelete_shouldSetDeletionDate_whenAssociationExists() {
        StudentClassEntity entity = new StudentClassEntity(1, classId, studentId, null);

        when(studentClassJPARepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.of(entity));
        when(studentClassJPARepository.save(any(StudentClassEntity.class))).thenReturn(entity);

        studentClassRepository.softDelete(classId, studentId);

        assertNotNull(entity.getDeletionDate());
        assertEquals(LocalDate.now(), entity.getDeletionDate());
        verify(studentClassJPARepository).findByClassIdAndStudentId(classId, studentId);
        verify(studentClassJPARepository).save(entity);
    }

    @Test
    void findClassIdsByStudentId_shouldReturnListOfClassIds() {
        Integer studentId = 1;
        List<Integer> expectedClassIds = Arrays.asList(10, 20, 30);

        when(studentClassJPARepository.findClassIdsByStudentIdAndDeletionDateIsNull(studentId))
                .thenReturn(expectedClassIds);

        List<Integer> result = studentClassRepository.findClassIdsByStudentId(studentId);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedClassIds, result);
        verify(studentClassJPARepository).findClassIdsByStudentIdAndDeletionDateIsNull(studentId);
    }

    @Test
    void findClassIdsByStudentId_shouldReturnEmptyList_whenNoClassesFound() {
        Integer studentId = 1;
        List<Integer> emptyList = Collections.emptyList();

        when(studentClassJPARepository.findClassIdsByStudentIdAndDeletionDateIsNull(studentId))
                .thenReturn(emptyList);

        List<Integer> result = studentClassRepository.findClassIdsByStudentId(studentId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentClassJPARepository).findClassIdsByStudentIdAndDeletionDateIsNull(studentId);
    }

    @Test
    void findClassIdsByTeacherId_shouldReturnMapOfStudentIdsToClassIds() {
        Integer teacherId = 1;
        StudentClassEntity entity1 = new StudentClassEntity(1, 10, 100, null);
        StudentClassEntity entity2 = new StudentClassEntity(2, 20, 100, null);
        StudentClassEntity entity3 = new StudentClassEntity(3, 30, 200, null);
        StudentClassEntity entity4 = new StudentClassEntity(4, 40, 200, null);
        List<StudentClassEntity> entities = Arrays.asList(entity1, entity2, entity3, entity4);

        when(studentClassJPARepository.findAllByTeacherIdAndDeletionDateIsNull(teacherId))
                .thenReturn(entities);

        Map<Integer, List<Integer>> result = studentClassRepository.findClassIdsByTeacherId(teacherId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(100));
        assertTrue(result.containsKey(200));
        assertEquals(Arrays.asList(10, 20), result.get(100));
        assertEquals(Arrays.asList(30, 40), result.get(200));
        verify(studentClassJPARepository).findAllByTeacherIdAndDeletionDateIsNull(teacherId);
    }

    @Test
    void findClassIdsByTeacherId_shouldReturnEmptyMap_whenNoAssociationsFound() {
        Integer teacherId = 1;
        List<StudentClassEntity> emptyList = Collections.emptyList();

        when(studentClassJPARepository.findAllByTeacherIdAndDeletionDateIsNull(teacherId))
                .thenReturn(emptyList);

        Map<Integer, List<Integer>> result = studentClassRepository.findClassIdsByTeacherId(teacherId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentClassJPARepository).findAllByTeacherIdAndDeletionDateIsNull(teacherId);
    }

    @Test
    void findClassIdsByTeacherId_shouldGroupMultipleClassesByStudent() {
        Integer teacherId = 1;
        Integer studentId = 100;
        StudentClassEntity entity1 = new StudentClassEntity(1, 10, studentId, null);
        StudentClassEntity entity2 = new StudentClassEntity(2, 20, studentId, null);
        StudentClassEntity entity3 = new StudentClassEntity(3, 30, studentId, null);
        List<StudentClassEntity> entities = Arrays.asList(entity1, entity2, entity3);

        when(studentClassJPARepository.findAllByTeacherIdAndDeletionDateIsNull(teacherId))
                .thenReturn(entities);

        Map<Integer, List<Integer>> result = studentClassRepository.findClassIdsByTeacherId(teacherId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(studentId));
        assertEquals(3, result.get(studentId).size());
        assertEquals(Arrays.asList(10, 20, 30), result.get(studentId));
        verify(studentClassJPARepository).findAllByTeacherIdAndDeletionDateIsNull(teacherId);
    }

    @Test
    void softDelete_shouldThrowException_whenAssociationNotFound() {
        when(studentClassJPARepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                studentClassRepository.softDelete(classId, studentId));

        verify(studentClassJPARepository).findByClassIdAndStudentId(classId, studentId);
        verify(studentClassJPARepository, never()).save(any());
    }

    @Test
    void findById_shouldReturnStudentClass_whenExists() {
        Integer id = 1;
        StudentClassEntity entity = new StudentClassEntity(id, classId, studentId, null);
        StudentClass studentClass = StudentClass.builder()
                .id(id)
                .classId(classId)
                .studentId(studentId)
                .build();

        when(studentClassJPARepository.findById(id)).thenReturn(Optional.of(entity));
        when(studentClassMapper.toModel(entity)).thenReturn(studentClass);

        Optional<StudentClass> result = studentClassRepository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals(classId, result.get().getClassId());
        assertEquals(studentId, result.get().getStudentId());
        verify(studentClassJPARepository).findById(id);
        verify(studentClassMapper).toModel(entity);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Integer id = 999;

        when(studentClassJPARepository.findById(id)).thenReturn(Optional.empty());

        Optional<StudentClass> result = studentClassRepository.findById(id);

        assertFalse(result.isPresent());
        verify(studentClassJPARepository).findById(id);
        verify(studentClassMapper, never()).toModel(any());
    }

    @Test
    void softDeleteByClassId_shouldCallJpaRepository() {
        studentClassRepository.softDeleteByClassId(classId);

        verify(studentClassJPARepository).softDeleteByClassId(classId);
    }

    @Test
    void softDeleteByStudentId_shouldCallJpaRepository() {
        studentClassRepository.softDeleteByStudentId(studentId);

        verify(studentClassJPARepository).softDeleteByStudentId(studentId);
    }
}

