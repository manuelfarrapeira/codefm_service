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

        Class s1c1 = Class.builder().id(1).schoolId(1).name("Class A").schoolYear("23/24").build();
        Class s1c2 = Class.builder().id(2).schoolId(1).name("Class B").schoolYear("25/26").build();
        Class s1c3 = Class.builder().id(3).schoolId(1).name("Class C").schoolYear("24/25").build();
        School school1 = School.builder()
                .id(1)
                .teacherId(teacherId)
                .name("School A (Max 25/26)")
                .town("Town A").tlf(123456)
                .classes(Arrays.asList(s1c1, s1c2, s1c3))
                .build();

        Class s2c1 = Class.builder().id(4).schoolId(2).name("Class D").schoolYear("22/23").build();
        Class s2c2 = Class.builder().id(5).schoolId(2).name("Class E").schoolYear("24/25").build();
        School school2 = School.builder()
                .id(2)
                .teacherId(teacherId)
                .name("School B (Max 24/25)")
                .town("Town B").tlf(789012)
                .classes(Arrays.asList(s2c1, s2c2))
                .build();

        Class s3c1 = Class.builder().id(6).schoolId(3).name("Class F").schoolYear("26/27").build();
        Class s3c2 = Class.builder().id(7).schoolId(3).name("Class G").schoolYear("25/26").build();
        School school3 = School.builder()
                .id(3)
                .teacherId(teacherId)
                .name("School C (Max 26/27)")
                .town("Town C").tlf(345678)
                .classes(Arrays.asList(s3c1, s3c2))
                .build();

        School school4 = School.builder()
                .id(4)
                .teacherId(teacherId)
                .name("School D (No Classes)")
                .town("Town D").tlf(901234)
                .classes(Collections.emptyList())
                .build();

        Class s5c1 = Class.builder().id(8).schoolId(5).name("Class H").schoolYear("INVALID").build();
        Class s5c2 = Class.builder().id(9).schoolId(5).name("Class I").schoolYear("21/22").build();
        School school5 = School.builder()
                .id(5)
                .teacherId(teacherId)
                .name("School E (Max 21/22, Invalid)")
                .town("Town E").tlf(567890)
                .classes(Arrays.asList(s5c1, s5c2))
                .build();


        List<School> schoolsFromService = Arrays.asList(school2, school4, school1, school5, school3);

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(schoolService.getSchoolsByTeacherId(teacherId)).thenReturn(schoolsFromService);

        List<School> result = schoolUseCase.getSchoolsByTeacher();

        assertNotNull(result);
        assertEquals(5, result.size());
        verify(schoolService, times(1)).getSchoolsByTeacherId(teacherId);

        assertEquals("School C (Max 26/27)", result.get(0).getName());
        assertEquals("School A (Max 25/26)", result.get(1).getName());
        assertEquals("School B (Max 24/25)", result.get(2).getName());
        assertEquals("School E (Max 21/22, Invalid)", result.get(3).getName());
        assertEquals("School D (No Classes)", result.get(4).getName());

        List<Class> classesS3 = result.get(0).getClasses();
        assertEquals("26/27", classesS3.get(0).getSchoolYear());
        assertEquals("25/26", classesS3.get(1).getSchoolYear());

        List<Class> classesS1 = result.get(1).getClasses();
        assertEquals("25/26", classesS1.get(0).getSchoolYear());
        assertEquals("24/25", classesS1.get(1).getSchoolYear());
        assertEquals("23/24", classesS1.get(2).getSchoolYear());

        List<Class> classesS2 = result.get(2).getClasses();
        assertEquals("24/25", classesS2.get(0).getSchoolYear());
        assertEquals("22/23", classesS2.get(1).getSchoolYear());

        List<Class> classesS5 = result.get(3).getClasses();
        assertEquals("21/22", classesS5.get(0).getSchoolYear());
        assertEquals("INVALID", classesS5.get(1).getSchoolYear());

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
                .classes(null)
                .build();

        List<School> schoolsFromService = Collections.singletonList(schoolWithNullClasses);

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(schoolService.getSchoolsByTeacherId(teacherId)).thenReturn(schoolsFromService);

        List<School> result = schoolUseCase.getSchoolsByTeacher();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getClasses());
        verify(schoolService, times(1)).getSchoolsByTeacherId(teacherId);
    }

    @Test
    void createSchool_shouldSetTeacherIdAndCallService() {
        Integer teacherId = 123;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), String.valueOf(teacherId));

        School schoolToCreate = School.builder()
                .name("New School")
                .town("New Town")
                .build();

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(schoolService.createSchool(any(School.class))).thenAnswer(invocation -> invocation.getArgument(0));

        schoolUseCase.createSchool(schoolToCreate);

        ArgumentCaptor<School> schoolCaptor = ArgumentCaptor.forClass(School.class);
        verify(schoolService, times(1)).createSchool(schoolCaptor.capture());

        School capturedSchool = schoolCaptor.getValue();
        assertEquals(teacherId, capturedSchool.getTeacherId());
        assertEquals("New School", capturedSchool.getName());
    }

    @Test
    void softDeleteSchool_shouldGetTeacherIdFromSessionAndCallService() {
        Integer schoolId = 1;
        Integer teacherId = 101;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), String.valueOf(teacherId));

        when(sessionUser.getParameters()).thenReturn(parameters);
        doNothing().when(schoolService).softDeleteSchool(schoolId, teacherId);

        schoolUseCase.softDeleteSchool(schoolId);

        verify(sessionUser, times(1)).getParameters();
        verify(schoolService, times(1)).softDeleteSchool(schoolId, teacherId);
    }

    @Test
    void updateSchool_shouldGetTeacherIdFromSessionAndCallService() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 101;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.TEACHER_ID.getClaimName(), String.valueOf(teacherId));

        School schoolToUpdate = School.builder()
                .name("Updated School Name")
                .town("Updated Town")
                .tlf(987654321)
                .build();
        School updatedSchool = School.builder()
                .id(schoolId)
                .teacherId(teacherId)
                .name("Updated School Name")
                .town("Updated Town")
                .tlf(987654321)
                .build();

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(schoolService.updateSchool(eq(schoolId), any(School.class), eq(teacherId)))
                .thenReturn(updatedSchool);

        // When
        School result = schoolUseCase.updateSchool(schoolId, schoolToUpdate);

        // Then
        assertNotNull(result);
        assertEquals(schoolId, result.getId());
        assertEquals("Updated School Name", result.getName());
        assertEquals("Updated Town", result.getTown());
        assertEquals(987654321, result.getTlf());
        verify(sessionUser, times(1)).getParameters();
        verify(schoolService, times(1)).updateSchool(eq(schoolId), any(School.class), eq(teacherId));
    }
}
