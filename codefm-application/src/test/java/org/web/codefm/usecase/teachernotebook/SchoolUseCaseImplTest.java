package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.School;
import org.web.codefm.domain.service.teachernotebook.SchoolService;
import org.web.codefm.domain.session.SessionUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolUseCaseImplTest {

    @Mock
    private SchoolService schoolService;

    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private SchoolUseCaseImpl schoolUseCase;

    @Test
    void getSchoolsByTeacher_shouldReturnSchools() {
        Integer teacherId = 1;
        Map<String, String> parameters = new HashMap<>();
        parameters.put("teacher_id", String.valueOf(teacherId));

        School school1 = new School(1, teacherId, "School A", "Town A", 123456);
        School school2 = new School(2, teacherId, "School B", "Town B", 789012);
        List<School> expectedSchools = Arrays.asList(school1, school2);

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(schoolService.getSchoolsByTeacherId(teacherId)).thenReturn(expectedSchools);

        List<School> result = schoolUseCase.getSchoolsByTeacher();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("School A", result.get(0).getName());
        verify(schoolService, times(1)).getSchoolsByTeacherId(teacherId);
    }

    @Test
    void getSchoolsByTeacher_shouldReturnEmptyList_whenNoSchoolsFound() {
        Integer teacherId = 999;
        Map<String, String> parameters = new HashMap<>();
        parameters.put("teacher_id", String.valueOf(teacherId));

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(schoolService.getSchoolsByTeacherId(teacherId)).thenReturn(List.of());

        List<School> result = schoolUseCase.getSchoolsByTeacher();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(schoolService, times(1)).getSchoolsByTeacherId(teacherId);
    }
}
