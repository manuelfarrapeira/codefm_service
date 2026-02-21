package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SchoolEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.SchoolJPARepository;
import org.web.codefm.infrastructure.mapper.SchoolMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolRepositoryImplTest {

    @Mock
    private SchoolJPARepository schoolJPARepository;

    @Mock
    private SchoolMapper schoolMapper;

    @InjectMocks
    private SchoolRepositoryImpl schoolRepository;

    @Test
    void save_shouldMapToEntityAndSaveAndMapBackToModel() {
        // Given
        School schoolToSave = School.builder().name("New School").build();
        SchoolEntity schoolEntity = new SchoolEntity();
        SchoolEntity savedSchoolEntity = new SchoolEntity();
        School savedSchool = School.builder().id(1).name("New School").build();

        when(schoolMapper.toEntity(schoolToSave)).thenReturn(schoolEntity);
        when(schoolJPARepository.save(schoolEntity)).thenReturn(savedSchoolEntity);
        when(schoolMapper.toModel(savedSchoolEntity)).thenReturn(savedSchool);

        // When
        School result = schoolRepository.save(schoolToSave);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("New School", result.getName());

        verify(schoolMapper, times(1)).toEntity(schoolToSave);
        verify(schoolJPARepository, times(1)).save(schoolEntity);
        verify(schoolMapper, times(1)).toModel(savedSchoolEntity);
    }

    @Test
    void findById_shouldReturnSchoolWhenFoundAndNotDeleted() {
        // Given
        Integer schoolId = 1;
        SchoolEntity schoolEntity = new SchoolEntity(schoolId, 101, "School A", "Town A", 123456789, null, null);
        School expectedSchool = School.builder().id(schoolId).name("School A").build();

        when(schoolJPARepository.findByIdAndDeletionDateIsNull(schoolId)).thenReturn(Optional.of(schoolEntity));
        when(schoolMapper.toModel(schoolEntity)).thenReturn(expectedSchool);

        // When
        Optional<School> result = schoolRepository.findById(schoolId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedSchool, result.get());
        verify(schoolJPARepository, times(1)).findByIdAndDeletionDateIsNull(schoolId);
        verify(schoolMapper, times(1)).toModel(schoolEntity);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        // Given
        Integer schoolId = 1;
        when(schoolJPARepository.findByIdAndDeletionDateIsNull(schoolId)).thenReturn(Optional.empty());

        // When
        Optional<School> result = schoolRepository.findById(schoolId);

        // Then
        assertFalse(result.isPresent());
        verify(schoolJPARepository, times(1)).findByIdAndDeletionDateIsNull(schoolId);
        verify(schoolMapper, never()).toModel(any(SchoolEntity.class));
    }

    @Test
    void softDeleteSchool_shouldSetDeletionDateAndReturnUpdatedSchool() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 101;
        SchoolEntity schoolEntity = new SchoolEntity(schoolId, teacherId, "School A", "Town A", 123456789, null, null);
        School updatedSchool = School.builder().id(schoolId).teacherId(teacherId).name("School A").deletionDate(LocalDate.now()).build();

        when(schoolJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId)).thenReturn(Optional.of(schoolEntity));
        when(schoolJPARepository.save(any(SchoolEntity.class))).thenReturn(schoolEntity); // Return the same entity, deletionDate will be set
        when(schoolMapper.toModel(any(SchoolEntity.class))).thenReturn(updatedSchool);

        // When
        School result = schoolRepository.softDeleteSchool(schoolId, teacherId);

        // Then
        assertNotNull(result);
        assertEquals(schoolId, result.getId());
        assertEquals(teacherId, result.getTeacherId());
        assertNotNull(result.getDeletionDate());
        verify(schoolJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId);
        verify(schoolJPARepository, times(1)).save(schoolEntity);
        verify(schoolMapper, times(1)).toModel(schoolEntity);
    }

    @Test
    void softDeleteSchool_shouldThrowExceptionWhenSchoolNotFoundOrNotOwned() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 101;

        when(schoolJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> schoolRepository.softDeleteSchool(schoolId, teacherId));
        verify(schoolJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId);
        verify(schoolJPARepository, never()).save(any(SchoolEntity.class));
        verify(schoolMapper, never()).toModel(any(SchoolEntity.class));
    }
}
