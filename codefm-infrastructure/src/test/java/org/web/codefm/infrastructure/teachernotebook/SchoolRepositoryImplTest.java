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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
