package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentJPARepository;
import org.web.codefm.infrastructure.mapper.StudentMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentRepositoryImplTest {

    @Mock
    private StudentJPARepository studentJPARepository;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentRepositoryImpl studentRepository;

    @Test
    void save_shouldMapToEntityAndSaveAndMapBackToModel() {
        // Given
        Student studentToSave = Student.builder()
                .name("Juan")
                .surnames("García López")
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .build();

        StudentEntity studentEntity = new StudentEntity();
        StudentEntity savedStudentEntity = new StudentEntity();
        savedStudentEntity.setId(1);
        savedStudentEntity.setName("Juan");
        savedStudentEntity.setSurnames("García López");

        Student savedStudent = Student.builder()
                .id(1)
                .name("Juan")
                .surnames("García López")
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .build();

        when(studentMapper.toEntity(studentToSave)).thenReturn(studentEntity);
        when(studentJPARepository.save(studentEntity)).thenReturn(savedStudentEntity);
        when(studentMapper.toModel(savedStudentEntity)).thenReturn(savedStudent);

        // When
        Student result = studentRepository.save(studentToSave);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Juan", result.getName());
        assertEquals("García López", result.getSurnames());

        verify(studentMapper, times(1)).toEntity(studentToSave);
        verify(studentJPARepository, times(1)).save(studentEntity);
        verify(studentMapper, times(1)).toModel(savedStudentEntity);
    }

    @Test
    void findByIdAndDeletionDateIsNull_shouldReturnStudentWhenFoundAndNotDeleted() {
        // Given
        Integer studentId = 1;
        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setId(studentId);
        studentEntity.setName("Juan");
        studentEntity.setSurnames("García López");
        studentEntity.setDeletionDate(null);

        Student expectedStudent = Student.builder()
                .id(studentId)
                .name("Juan")
                .surnames("García López")
                .build();

        when(studentJPARepository.findByIdAndDeletionDateIsNull(studentId))
                .thenReturn(Optional.of(studentEntity));
        when(studentMapper.toModel(studentEntity)).thenReturn(expectedStudent);

        // When
        Optional<Student> result = studentRepository.findByIdAndDeletionDateIsNull(studentId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedStudent, result.get());
        assertEquals("Juan", result.get().getName());

        verify(studentJPARepository, times(1)).findByIdAndDeletionDateIsNull(studentId);
        verify(studentMapper, times(1)).toModel(studentEntity);
    }

    @Test
    void findByIdAndDeletionDateIsNull_shouldReturnEmptyWhenNotFound() {
        // Given
        Integer studentId = 999;
        when(studentJPARepository.findByIdAndDeletionDateIsNull(studentId))
                .thenReturn(Optional.empty());

        // When
        Optional<Student> result = studentRepository.findByIdAndDeletionDateIsNull(studentId);

        // Then
        assertFalse(result.isPresent());
        verify(studentJPARepository, times(1)).findByIdAndDeletionDateIsNull(studentId);
        verify(studentMapper, never()).toModel(any(StudentEntity.class));
    }

    @Test
    void update_shouldMapToEntityAndUpdateAndMapBackToModel() {
        // Given
        Student studentToUpdate = Student.builder()
                .id(1)
                .name("Juan Carlos")
                .surnames("García López Pérez")
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .additionalInfo("Updated info")
                .build();

        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setId(1);

        StudentEntity updatedStudentEntity = new StudentEntity();
        updatedStudentEntity.setId(1);
        updatedStudentEntity.setName("Juan Carlos");
        updatedStudentEntity.setSurnames("García López Pérez");

        Student updatedStudent = Student.builder()
                .id(1)
                .name("Juan Carlos")
                .surnames("García López Pérez")
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .additionalInfo("Updated info")
                .build();

        when(studentMapper.toEntity(studentToUpdate)).thenReturn(studentEntity);
        when(studentJPARepository.save(studentEntity)).thenReturn(updatedStudentEntity);
        when(studentMapper.toModel(updatedStudentEntity)).thenReturn(updatedStudent);

        // When
        Student result = studentRepository.update(studentToUpdate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Juan Carlos", result.getName());
        assertEquals("García López Pérez", result.getSurnames());
        assertEquals("Updated info", result.getAdditionalInfo());

        verify(studentMapper, times(1)).toEntity(studentToUpdate);
        verify(studentJPARepository, times(1)).save(studentEntity);
        verify(studentMapper, times(1)).toModel(updatedStudentEntity);
    }

    @Test
    void softDelete_shouldSetDeletionDateAndReturnUpdatedStudent() {
        // Given
        Integer studentId = 1;
        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setId(studentId);
        studentEntity.setName("Juan");
        studentEntity.setSurnames("García López");
        studentEntity.setDeletionDate(null);

        Student deletedStudent = Student.builder()
                .id(studentId)
                .name("Juan")
                .surnames("García López")
                .deletionDate(LocalDate.now())
                .build();

        when(studentJPARepository.findByIdAndDeletionDateIsNull(studentId))
                .thenReturn(Optional.of(studentEntity));
        when(studentJPARepository.save(any(StudentEntity.class))).thenReturn(studentEntity);
        when(studentMapper.toModel(any(StudentEntity.class))).thenReturn(deletedStudent);

        // When
        Student result = studentRepository.softDelete(studentId);

        // Then
        assertNotNull(result);
        assertEquals(studentId, result.getId());
        assertNotNull(result.getDeletionDate());

        verify(studentJPARepository, times(1)).findByIdAndDeletionDateIsNull(studentId);
        verify(studentJPARepository, times(1)).save(any(StudentEntity.class));
        verify(studentMapper, times(1)).toModel(any(StudentEntity.class));
    }

    @Test
    void softDelete_shouldThrowExceptionWhenStudentNotFound() {
        // Given
        Integer studentId = 999;
        when(studentJPARepository.findByIdAndDeletionDateIsNull(studentId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            studentRepository.softDelete(studentId);
        });

        verify(studentJPARepository, times(1)).findByIdAndDeletionDateIsNull(studentId);
        verify(studentJPARepository, never()).save(any(StudentEntity.class));
        verify(studentMapper, never()).toModel(any(StudentEntity.class));
    }

    @Test
    void softDelete_shouldThrowExceptionWhenStudentAlreadyDeleted() {
        // Given
        Integer studentId = 1;
        when(studentJPARepository.findByIdAndDeletionDateIsNull(studentId))
                .thenReturn(Optional.empty()); // Already deleted means not found

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            studentRepository.softDelete(studentId);
        });

        verify(studentJPARepository, times(1)).findByIdAndDeletionDateIsNull(studentId);
        verify(studentJPARepository, never()).save(any(StudentEntity.class));
    }

    @Test
    void searchStudents_shouldReturnStudentsById() {
        // Given
        Integer studentId = 1;
        StudentEntity studentEntity1 = new StudentEntity();
        studentEntity1.setId(1);
        studentEntity1.setName("Juan");
        studentEntity1.setSurnames("García López");

        Student student1 = Student.builder()
                .id(1)
                .name("Juan")
                .surnames("García López")
                .build();

        List<StudentEntity> entities = Arrays.asList(studentEntity1);
        List<Student> expectedStudents = Arrays.asList(student1);

        when(studentJPARepository.searchStudents(studentId, null, null)).thenReturn(entities);
        when(studentMapper.toModelList(entities)).thenReturn(expectedStudents);

        // When
        List<Student> result = studentRepository.searchStudents(studentId, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
        verify(studentJPARepository, times(1)).searchStudents(studentId, null, null);
        verify(studentMapper, times(1)).toModelList(entities);
    }

    @Test
    void searchStudents_shouldReturnStudentsByName() {
        // Given
        String name = "Juan";
        StudentEntity studentEntity1 = new StudentEntity();
        studentEntity1.setId(1);
        studentEntity1.setName("Juan");
        studentEntity1.setSurnames("García López");

        StudentEntity studentEntity2 = new StudentEntity();
        studentEntity2.setId(2);
        studentEntity2.setName("Juan Carlos");
        studentEntity2.setSurnames("Pérez Martín");

        Student student1 = Student.builder()
                .id(1)
                .name("Juan")
                .surnames("García López")
                .build();

        Student student2 = Student.builder()
                .id(2)
                .name("Juan Carlos")
                .surnames("Pérez Martín")
                .build();

        List<StudentEntity> entities = Arrays.asList(studentEntity1, studentEntity2);
        List<Student> expectedStudents = Arrays.asList(student1, student2);

        when(studentJPARepository.searchStudents(null, name, null)).thenReturn(entities);
        when(studentMapper.toModelList(entities)).thenReturn(expectedStudents);

        // When
        List<Student> result = studentRepository.searchStudents(null, name, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Juan", result.get(0).getName());
        assertEquals("Juan Carlos", result.get(1).getName());
        verify(studentJPARepository, times(1)).searchStudents(null, name, null);
        verify(studentMapper, times(1)).toModelList(entities);
    }

    @Test
    void searchStudents_shouldReturnStudentsBySurnames() {
        // Given
        String surnames = "García";
        StudentEntity studentEntity1 = new StudentEntity();
        studentEntity1.setId(1);
        studentEntity1.setName("Juan");
        studentEntity1.setSurnames("García López");

        Student student1 = Student.builder()
                .id(1)
                .name("Juan")
                .surnames("García López")
                .build();

        List<StudentEntity> entities = Arrays.asList(studentEntity1);
        List<Student> expectedStudents = Arrays.asList(student1);

        when(studentJPARepository.searchStudents(null, null, surnames)).thenReturn(entities);
        when(studentMapper.toModelList(entities)).thenReturn(expectedStudents);

        // When
        List<Student> result = studentRepository.searchStudents(null, null, surnames);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("García López", result.get(0).getSurnames());
        verify(studentJPARepository, times(1)).searchStudents(null, null, surnames);
        verify(studentMapper, times(1)).toModelList(entities);
    }

    @Test
    void searchStudents_shouldReturnStudentsByMultipleFilters() {
        // Given
        String name = "Juan";
        String surnames = "García";
        StudentEntity studentEntity1 = new StudentEntity();
        studentEntity1.setId(1);
        studentEntity1.setName("Juan");
        studentEntity1.setSurnames("García López");

        Student student1 = Student.builder()
                .id(1)
                .name("Juan")
                .surnames("García López")
                .build();

        List<StudentEntity> entities = Arrays.asList(studentEntity1);
        List<Student> expectedStudents = Arrays.asList(student1);

        when(studentJPARepository.searchStudents(null, name, surnames)).thenReturn(entities);
        when(studentMapper.toModelList(entities)).thenReturn(expectedStudents);

        // When
        List<Student> result = studentRepository.searchStudents(null, name, surnames);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
        assertEquals("García López", result.get(0).getSurnames());
        verify(studentJPARepository, times(1)).searchStudents(null, name, surnames);
        verify(studentMapper, times(1)).toModelList(entities);
    }

    @Test
    void searchStudents_shouldReturnEmptyListWhenNoStudentsFound() {
        // Given
        Integer studentId = 999;
        List<StudentEntity> emptyEntities = Arrays.asList();
        List<Student> emptyStudents = Arrays.asList();

        when(studentJPARepository.searchStudents(studentId, null, null)).thenReturn(emptyEntities);
        when(studentMapper.toModelList(emptyEntities)).thenReturn(emptyStudents);

        // When
        List<Student> result = studentRepository.searchStudents(studentId, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentJPARepository, times(1)).searchStudents(studentId, null, null);
        verify(studentMapper, times(1)).toModelList(emptyEntities);
    }
}

