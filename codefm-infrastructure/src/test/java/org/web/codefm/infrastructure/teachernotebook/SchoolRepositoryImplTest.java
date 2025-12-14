package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.School;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SchoolEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.SchoolJPARepository;
import org.web.codefm.infrastructure.mapper.SchoolMapper;

import java.util.Arrays;
import java.util.List;

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
    void findByTeacherId_shouldReturnSchools() {
        Integer teacherId = 1;
        SchoolEntity entity1 = new SchoolEntity(1, teacherId, "School A", "Town A", 123456);
        SchoolEntity entity2 = new SchoolEntity(2, teacherId, "School B", "Town B", 789012);
        List<SchoolEntity> entities = Arrays.asList(entity1, entity2);

        School school1 = new School(1, teacherId, "School A", "Town A", 123456);
        School school2 = new School(2, teacherId, "School B", "Town B", 789012);
        List<School> expectedSchools = Arrays.asList(school1, school2);

        when(schoolJPARepository.findByTeacherId(teacherId)).thenReturn(entities);
        when(schoolMapper.toModelList(entities)).thenReturn(expectedSchools);

        List<School> result = schoolRepository.findByTeacherId(teacherId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("School A", result.get(0).getName());
        verify(schoolJPARepository, times(1)).findByTeacherId(teacherId);
        verify(schoolMapper, times(1)).toModelList(entities);
    }

    @Test
    void findByTeacherId_shouldReturnEmptyList_whenNoSchoolsFound() {
        Integer teacherId = 999;
        when(schoolJPARepository.findByTeacherId(teacherId)).thenReturn(List.of());
        when(schoolMapper.toModelList(List.of())).thenReturn(List.of());

        List<School> result = schoolRepository.findByTeacherId(teacherId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(schoolJPARepository, times(1)).findByTeacherId(teacherId);
    }
}
