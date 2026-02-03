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
import java.util.Optional;

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
        // Given
        StudentClassEntity entity = new StudentClassEntity(1, classId, studentId, null);
        StudentClass studentClass = StudentClass.builder()
                .id(1)
                .classId(classId)
                .studentId(studentId)
                .build();

        when(studentClassJPARepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.of(entity));
        when(studentClassMapper.toModel(entity)).thenReturn(studentClass);

        // When
        Optional<StudentClass> result = studentClassRepository.findByClassIdAndStudentId(classId, studentId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(classId, result.get().getClassId());
        assertEquals(studentId, result.get().getStudentId());
        verify(studentClassJPARepository).findByClassIdAndStudentId(classId, studentId);
        verify(studentClassMapper).toModel(entity);
    }

    @Test
    void findByClassIdAndStudentId_shouldReturnEmpty_whenNotExists() {
        // Given
        when(studentClassJPARepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.empty());

        // When
        Optional<StudentClass> result = studentClassRepository.findByClassIdAndStudentId(classId, studentId);

        // Then
        assertFalse(result.isPresent());
        verify(studentClassJPARepository).findByClassIdAndStudentId(classId, studentId);
        verify(studentClassMapper, never()).toModel(any());
    }

    @Test
    void save_shouldSaveAndReturnStudentClass() {
        // Given
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

        // When
        StudentClass result = studentClassRepository.save(studentClass);

        // Then
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
        // Given
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

        // When
        StudentClass result = studentClassRepository.update(studentClass);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNull(result.getDeletionDate());
        verify(studentClassMapper).toEntity(studentClass);
        verify(studentClassJPARepository).save(entity);
        verify(studentClassMapper).toModel(updatedEntity);
    }

    @Test
    void softDelete_shouldSetDeletionDate_whenAssociationExists() {
        // Given
        StudentClassEntity entity = new StudentClassEntity(1, classId, studentId, null);

        when(studentClassJPARepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.of(entity));
        when(studentClassJPARepository.save(any(StudentClassEntity.class))).thenReturn(entity);

        // When
        studentClassRepository.softDelete(classId, studentId);

        // Then
        assertNotNull(entity.getDeletionDate());
        assertEquals(LocalDate.now(), entity.getDeletionDate());
        verify(studentClassJPARepository).findByClassIdAndStudentId(classId, studentId);
        verify(studentClassJPARepository).save(entity);
    }

    @Test
    void softDelete_shouldThrowException_whenAssociationNotFound() {
        // Given
        when(studentClassJPARepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                studentClassRepository.softDelete(classId, studentId));

        verify(studentClassJPARepository).findByClassIdAndStudentId(classId, studentId);
        verify(studentClassJPARepository, never()).save(any());
    }
}

