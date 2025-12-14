package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.School;
import org.web.codefm.domain.repository.teachernotebook.SchoolRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolServiceImplTest {

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SchoolServiceImpl schoolService;

    @Test
    void getSchoolsByTeacherId_shouldReturnSchools() {
        Integer teacherId = 1;
        School school1 = new School(1, teacherId, "School A", "Town A", 123456);
        School school2 = new School(2, teacherId, "School B", "Town B", 789012);
        List<School> expectedSchools = Arrays.asList(school1, school2);

        when(schoolRepository.findByTeacherId(teacherId)).thenReturn(expectedSchools);

        List<School> result = schoolService.getSchoolsByTeacherId(teacherId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("School A", result.get(0).getName());
        verify(schoolRepository, times(1)).findByTeacherId(teacherId);
    }

    @Test
    void getSchoolsByTeacherId_shouldReturnEmptyList_whenNoSchoolsFound() {
        Integer teacherId = 999;
        when(schoolRepository.findByTeacherId(teacherId)).thenReturn(List.of());

        List<School> result = schoolService.getSchoolsByTeacherId(teacherId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(schoolRepository, times(1)).findByTeacherId(teacherId);
    }
}
