package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.service.teachernotebook.SchoolService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;

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
    void getSchoolsByTeacher_shouldSortSchoolsByMaxSchoolYearAndClassesInternallyDescending() {
        Integer teacherId = 1;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), String.valueOf(teacherId));

        // --- School 1: Max schoolYear 25/26 ---
        Class s1c1 = Class.builder().id(1).schoolId(1).name("Class A").schoolYear("23/24").build();
        Class s1c2 = Class.builder().id(2).schoolId(1).name("Class B").schoolYear("25/26").build(); // Max
        Class s1c3 = Class.builder().id(3).schoolId(1).name("Class C").schoolYear("24/25").build();
        School school1 = School.builder()
                .id(1)
                .teacherId(teacherId)
                .name("School A (Max 25/26)")
                .town("Town A").tlf(123456)
                .classes(Arrays.asList(s1c1, s1c2, s1c3))
                .build();

        // --- School 2: Max schoolYear 24/25 ---
        Class s2c1 = Class.builder().id(4).schoolId(2).name("Class D").schoolYear("22/23").build();
        Class s2c2 = Class.builder().id(5).schoolId(2).name("Class E").schoolYear("24/25").build(); // Max
        School school2 = School.builder()
                .id(2)
                .teacherId(teacherId)
                .name("School B (Max 24/25)")
                .town("Town B").tlf(789012)
                .classes(Arrays.asList(s2c1, s2c2))
                .build();

        // --- School 3: Max schoolYear 26/27 (should be first) ---
        Class s3c1 = Class.builder().id(6).schoolId(3).name("Class F").schoolYear("26/27").build(); // Max
        Class s3c2 = Class.builder().id(7).schoolId(3).name("Class G").schoolYear("25/26").build();
        School school3 = School.builder()
                .id(3)
                .teacherId(teacherId)
                .name("School C (Max 26/27)")
                .town("Town C").tlf(345678)
                .classes(Arrays.asList(s3c1, s3c2))
                .build();

        // --- School 4: No classes ---
        School school4 = School.builder()
                .id(4)
                .teacherId(teacherId)
                .name("School D (No Classes)")
                .town("Town D").tlf(901234)
                .classes(Collections.emptyList())
                .build();

        // --- School 5: Classes with invalid year (should be treated as 0) ---
        Class s5c1 = Class.builder().id(8).schoolId(5).name("Class H").schoolYear("INVALID").build();
        Class s5c2 = Class.builder().id(9).schoolId(5).name("Class I").schoolYear("21/22").build(); // Max valid
        School school5 = School.builder()
                .id(5)
                .teacherId(teacherId)
                .name("School E (Max 21/22, Invalid)")
                .town("Town E").tlf(567890)
                .classes(Arrays.asList(s5c1, s5c2))
                .build();


        // Escuelas en un orden inicial desordenado para el servicio
        List<School> schoolsFromService = Arrays.asList(school2, school4, school1, school5, school3);

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(schoolService.getSchoolsByTeacherId(teacherId)).thenReturn(schoolsFromService);

        // Ejecutar el caso de uso
        List<School> result = schoolUseCase.getSchoolsByTeacher();

        // --- Verificaciones ---
        assertNotNull(result);
        assertEquals(5, result.size());
        verify(schoolService, times(1)).getSchoolsByTeacherId(teacherId);

        // 1. Verificar el ordenamiento de las escuelas (por max schoolYear descendente)
        assertEquals("School C (Max 26/27)", result.get(0).getName()); // Max 26/27
        assertEquals("School A (Max 25/26)", result.get(1).getName()); // Max 25/26
        assertEquals("School B (Max 24/25)", result.get(2).getName()); // Max 24/25
        assertEquals("School E (Max 21/22, Invalid)", result.get(3).getName()); // Max 21/22 (invalid treated as 0)
        assertEquals("School D (No Classes)", result.get(4).getName()); // No classes (treated as 0)

        // 2. Verificar el ordenamiento interno de las clases para cada escuela
        // School C (Max 26/27)
        List<Class> classesS3 = result.get(0).getClasses();
        assertEquals("26/27", classesS3.get(0).getSchoolYear());
        assertEquals("25/26", classesS3.get(1).getSchoolYear());

        // School A (Max 25/26)
        List<Class> classesS1 = result.get(1).getClasses();
        assertEquals("25/26", classesS1.get(0).getSchoolYear());
        assertEquals("24/25", classesS1.get(1).getSchoolYear());
        assertEquals("23/24", classesS1.get(2).getSchoolYear());

        // School B (Max 24/25)
        List<Class> classesS2 = result.get(2).getClasses();
        assertEquals("24/25", classesS2.get(0).getSchoolYear());
        assertEquals("22/23", classesS2.get(1).getSchoolYear());

        // School E (Max 21/22, Invalid)
        List<Class> classesS5 = result.get(3).getClasses();
        assertEquals("21/22", classesS5.get(0).getSchoolYear()); // Valid year comes first
        assertEquals("INVALID", classesS5.get(1).getSchoolYear()); // Invalid year comes last (treated as 0)

        // School D (No Classes)
        assertTrue(result.get(4).getClasses().isEmpty());
    }

    @Test
    void getSchoolsByTeacher_shouldHandleSchoolsWithNullClassesList() {
        Integer teacherId = 1;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), String.valueOf(teacherId));

        School schoolWithNullClasses = School.builder()
                .id(1)
                .teacherId(teacherId)
                .name("School A (Null Classes)")
                .town("Town A").tlf(123456)
                .classes(null) // Clases nulas
                .build();

        List<School> schoolsFromService = Collections.singletonList(schoolWithNullClasses);

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(schoolService.getSchoolsByTeacherId(teacherId)).thenReturn(schoolsFromService);

        List<School> result = schoolUseCase.getSchoolsByTeacher();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getClasses()); // La lista de clases debe seguir siendo nula
        verify(schoolService, times(1)).getSchoolsByTeacherId(teacherId);
    }

    @Test
    void createSchool_shouldSetTeacherIdAndCallService() {
        // Given
        Integer teacherId = 123;
        String acceptLanguage = "en"; // Added acceptLanguage
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), String.valueOf(teacherId));

        School schoolToCreate = School.builder()
                .name("New School")
                .town("New Town")
                .build();

        when(sessionUser.getParameters()).thenReturn(parameters);
        // Updated mock to accept acceptLanguage parameter
        when(schoolService.createSchool(any(School.class), eq(acceptLanguage))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        schoolUseCase.createSchool(schoolToCreate, acceptLanguage); // Added acceptLanguage

        // Then
        ArgumentCaptor<School> schoolCaptor = ArgumentCaptor.forClass(School.class);
        // Updated verify to accept acceptLanguage parameter
        verify(schoolService, times(1)).createSchool(schoolCaptor.capture(), eq(acceptLanguage));

        School capturedSchool = schoolCaptor.getValue();
        assertEquals(teacherId, capturedSchool.getTeacherId());
        assertEquals("New School", capturedSchool.getName());
    }

    @Test
    void softDeleteSchool_shouldGetTeacherIdFromSessionAndCallService() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 101;
        String acceptLanguage = "en";
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), String.valueOf(teacherId));

        when(sessionUser.getParameters()).thenReturn(parameters);
        doNothing().when(schoolService).softDeleteSchool(schoolId, teacherId, acceptLanguage);

        // When
        schoolUseCase.softDeleteSchool(schoolId, acceptLanguage);

        // Then
        verify(sessionUser, times(1)).getParameters();
        verify(schoolService, times(1)).softDeleteSchool(schoolId, teacherId, acceptLanguage);
    }
}
