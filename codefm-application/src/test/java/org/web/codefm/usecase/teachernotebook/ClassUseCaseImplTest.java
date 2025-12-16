package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.service.teachernotebook.ClassService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassUseCaseImplTest {

    @Mock
    private ClassService classService;

    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private ClassUseCaseImpl classUseCase;

    @Test
    void getClassesBySchoolId_shouldReturnSortedClasses() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), String.valueOf(teacherId));

        Class class1 = Class.builder().id(1).schoolId(schoolId).name("Math 2023").schoolYear("23/24").build();
        Class class2 = Class.builder().id(2).schoolId(schoolId).name("Math 2024").schoolYear("24/25").build();
        Class class3 = Class.builder().id(3).schoolId(schoolId).name("Math 2022").schoolYear("22/23").build();
        List<Class> unsortedClasses = Arrays.asList(class1, class2, class3);

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(classService.getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)).thenReturn(unsortedClasses);

        // When
        List<Class> result = classUseCase.getClassesBySchoolId(schoolId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        // Verify sorted in descending order (24/25, 23/24, 22/23)
        assertEquals("24/25", result.get(0).getSchoolYear());
        assertEquals("23/24", result.get(1).getSchoolYear());
        assertEquals("22/23", result.get(2).getSchoolYear());
        verify(classService, times(1)).getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
    }

    @Test
    void getClassesBySchoolId_shouldReturnEmptyList_whenNoClassesFound() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), String.valueOf(teacherId));

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(classService.getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)).thenReturn(new ArrayList<>());

        // When
        List<Class> result = classUseCase.getClassesBySchoolId(schoolId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(classService, times(1)).getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
    }

    @Test
    void createClass_shouldCreateClass() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), String.valueOf(teacherId));

        Class classToCreate = Class.builder()
                .schoolId(schoolId)
                .name("Math Class")
                .schoolYear("24/25")
                .build();

        Class createdClass = Class.builder()
                .id(1)
                .schoolId(schoolId)
                .name("Math Class")
                .schoolYear("24/25")
                .build();

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(classService.createClass(classToCreate, teacherId)).thenReturn(createdClass);

        // When
        Class result = classUseCase.createClass(classToCreate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Math Class", result.getName());
        assertEquals("24/25", result.getSchoolYear());
        verify(classService, times(1)).createClass(classToCreate, teacherId);
    }

    @Test
    void softDeleteClass_shouldGetTeacherIdFromSessionAndCallService() {
        // Given
        Integer classId = 1;
        Integer teacherId = 1;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), String.valueOf(teacherId));

        when(sessionUser.getParameters()).thenReturn(parameters);
        doNothing().when(classService).softDeleteClass(classId, teacherId);

        // When
        classUseCase.softDeleteClass(classId);

        // Then
        verify(sessionUser, times(1)).getParameters();
        verify(classService, times(1)).softDeleteClass(classId, teacherId);
    }
}

