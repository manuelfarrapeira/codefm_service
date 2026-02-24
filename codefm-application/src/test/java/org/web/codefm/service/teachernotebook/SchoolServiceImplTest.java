package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.SchoolForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SchoolNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SchoolValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolServiceImplTest {

    @Mock
    private SchoolRepository schoolRepository;
    @Mock
    private ClassRepository classRepository;
    @Mock
    private SubjectClassRepository subjectClassRepository;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private StudentClassRepository studentClassRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private ExerciseStudentGradeRepository exerciseStudentGradeRepository;
    @Mock
    private ExerciseDocumentService exerciseDocumentService;
    @Mock
    private MessageSource messageSource;
    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private SchoolServiceImpl schoolService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        schoolService = new SchoolServiceImpl(schoolRepository, classRepository, subjectClassRepository,
                scheduleRepository, studentClassRepository, exerciseRepository, exerciseStudentGradeRepository, exerciseDocumentService, messageSource, sessionUser);
    }

    @Test
    void getSchoolsByTeacherId_shouldReturnSchools_whenFound() {
        Integer teacherId = 1;
        List<School> expectedSchools = Arrays.asList(
                School.builder().id(1).name("School A").build(),
                School.builder().id(2).name("School B").build()
        );

        when(schoolRepository.findByTeacherId(teacherId)).thenReturn(expectedSchools);

        List<School> actualSchools = schoolService.getSchoolsByTeacherId(teacherId);

        assertNotNull(actualSchools);
        assertEquals(2, actualSchools.size());
        assertEquals("School A", actualSchools.get(0).getName());
        verify(schoolRepository, times(1)).findByTeacherId(teacherId);
    }

    @Test
    void getSchoolsByTeacherId_shouldReturnEmptyList_whenNoSchoolsFound() {
        Integer teacherId = 2;
        when(schoolRepository.findByTeacherId(teacherId)).thenReturn(Collections.emptyList());

        List<School> actualSchools = schoolService.getSchoolsByTeacherId(teacherId);

        assertNotNull(actualSchools);
        assertTrue(actualSchools.isEmpty());
        verify(schoolRepository, times(1)).findByTeacherId(teacherId);
    }

    @Test
    void createSchool_shouldSaveSchool_whenDataIsValid() {
        School schoolToCreate = School.builder()
                .name("Valid School")
                .tlf(123456789)
                .build();

        when(schoolRepository.save(schoolToCreate)).thenReturn(schoolToCreate);
        School createdSchool = schoolService.createSchool(schoolToCreate);

        assertNotNull(createdSchool);
        assertEquals("Valid School", createdSchool.getName());
        verify(schoolRepository, times(1)).save(schoolToCreate);
    }

    @Test
    void createSchool_shouldThrowException_whenTlfIsInvalid() {
        School schoolWithInvalidTlf = School.builder()
                .name("Valid School")
                .tlf(123)
                .build();
        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class)))
                .thenReturn("Telephone number must be 9 digits.");
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        SchoolValidationException exception = assertThrows(SchoolValidationException.class, () -> {
            schoolService.createSchool(schoolWithInvalidTlf);
        });

        assertEquals(1, exception.getErrors().size());
        assertEquals("tlf", exception.getErrors().get(0).getParam());
        assertEquals("Telephone number must be 9 digits.", exception.getErrors().get(0).getMessage());
        verify(schoolRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class));
    }

    @Test
    void createSchool_shouldThrowException_whenMultipleFieldsAreInvalid() {
        School schoolWithMultipleErrors = School.builder()
                .name("")
                .tlf(12345)
                .build();

        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("School name is required.");
        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class)))
                .thenReturn("Telephone number must be 9 digits.");
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        SchoolValidationException exception = assertThrows(SchoolValidationException.class, () -> {
            schoolService.createSchool(schoolWithMultipleErrors);
        });

        assertEquals(2, exception.getErrors().size());

        assertTrue(exception.getErrors().stream().anyMatch(e ->
                "name".equals(e.getParam()) && "School name is required.".equals(e.getMessage())));
        assertTrue(exception.getErrors().stream().anyMatch(e ->
                "tlf".equals(e.getParam()) && "Telephone number must be 9 digits.".equals(e.getMessage())));

        verify(schoolRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class));
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class));
    }


    @Test
    void softDeleteSchool_shouldCallRepository_whenSchoolExistsAndOwnedByTeacher() {
        Integer schoolId = 1;
        Integer teacherId = 101;
        School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(classRepository.findActiveIdsBySchoolId(schoolId)).thenReturn(Collections.emptyList());
        when(schoolRepository.softDeleteSchool(schoolId, teacherId)).thenReturn(school);

        schoolService.softDeleteSchool(schoolId, teacherId);

        verify(schoolRepository, times(1)).findById(schoolId);
        verify(classRepository, times(1)).findActiveIdsBySchoolId(schoolId);
        verify(classRepository, times(1)).softDeleteBySchoolId(schoolId);
        verify(schoolRepository, times(1)).softDeleteSchool(schoolId, teacherId);
    }

    @Test
    void softDeleteSchool_shouldCascadeDeleteAllDependencies() {
        Integer schoolId = 1;
        Integer teacherId = 101;
        Integer classId1 = 10;
        Integer classId2 = 20;
        School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(classRepository.findActiveIdsBySchoolId(schoolId)).thenReturn(Arrays.asList(classId1, classId2));
        when(subjectClassRepository.findActiveIdsByClassId(classId1)).thenReturn(Arrays.asList(100, 101));
        when(subjectClassRepository.findActiveIdsByClassId(classId2)).thenReturn(Collections.emptyList());
        when(schoolRepository.softDeleteSchool(schoolId, teacherId)).thenReturn(school);

        schoolService.softDeleteSchool(schoolId, teacherId);

        verify(exerciseRepository, times(1)).softDeleteBySubjectClassIds(Arrays.asList(100, 101));
        verify(studentClassRepository, times(1)).softDeleteByClassId(classId1);
        verify(subjectClassRepository, times(1)).softDeleteByClassId(classId1);
        verify(scheduleRepository, times(1)).softDeleteByClassId(classId1);
        verify(studentClassRepository, times(1)).softDeleteByClassId(classId2);
        verify(subjectClassRepository, times(1)).softDeleteByClassId(classId2);
        verify(scheduleRepository, times(1)).softDeleteByClassId(classId2);
        verify(classRepository, times(1)).softDeleteBySchoolId(schoolId);
        verify(schoolRepository, times(1)).softDeleteSchool(schoolId, teacherId);
    }

    @Test
    void softDeleteSchool_shouldThrowSchoolNotFoundException_whenSchoolDoesNotExist() {
        Integer schoolId = 1;
        Integer teacherId = 101;
        String expectedErrorMessage = "School with ID 1 not found.";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn(expectedErrorMessage);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        SchoolNotFoundException exception = assertThrows(SchoolNotFoundException.class, () ->
                schoolService.softDeleteSchool(schoolId, teacherId));

        assertEquals(expectedErrorMessage, exception.getErrorDescription());
        verify(schoolRepository, times(1)).findById(schoolId);
        verify(schoolRepository, never()).softDeleteSchool(any(), any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_NOT_FOUND), any(), any(Locale.class));
    }

    @Test
    void softDeleteSchool_shouldThrowSchoolForbiddenException_whenSchoolNotOwnedByTeacher() {
        Integer schoolId = 1;
        Integer teacherId = 101;
        Integer otherTeacherId = 999;
        School school = School.builder().id(schoolId).teacherId(otherTeacherId).name("School A").build();
        String expectedErrorMessage = "You are not authorized to delete this school.";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_FORBIDDEN), any(), any(Locale.class)))
                .thenReturn(expectedErrorMessage);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        SchoolForbiddenException exception = assertThrows(SchoolForbiddenException.class, () ->
                schoolService.softDeleteSchool(schoolId, teacherId));

        assertEquals(expectedErrorMessage, exception.getErrorDescription());
        verify(schoolRepository, times(1)).findById(schoolId);
        verify(schoolRepository, never()).softDeleteSchool(any(), any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_FORBIDDEN), any(), any(Locale.class));
    }

    @Test
    void getSchoolById_shouldReturnSchool_whenFound() {
        Integer schoolId = 1;
        School expectedSchool = School.builder().id(schoolId).name("Test School").build();
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(expectedSchool));

        Optional<School> result = schoolService.getSchoolById(schoolId);

        assertTrue(result.isPresent());
        assertEquals(expectedSchool, result.get());
        verify(schoolRepository, times(1)).findById(schoolId);
    }

    @Test
    void getSchoolById_shouldReturnEmpty_whenNotFound() {
        Integer schoolId = 1;
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());

        Optional<School> result = schoolService.getSchoolById(schoolId);

        assertFalse(result.isPresent());
        verify(schoolRepository, times(1)).findById(schoolId);
    }


    @Test
    void updateSchool_shouldUpdateSchool_whenDataIsValidAndOwnedByTeacher() {
        Integer schoolId = 1;
        Integer teacherId = 101;
        School existingSchool = School.builder().id(schoolId).teacherId(teacherId).name("Old Name").town("Old Town").tlf(111222333).build();
        School updatedSchoolData = School.builder().name("New Name").town("New Town").tlf(987654321).build();
        School savedSchool = School.builder().id(schoolId).teacherId(teacherId).name("New Name").town("New Town").tlf(987654321).build();

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(existingSchool));
        when(schoolRepository.save(any(School.class))).thenReturn(savedSchool);

        School result = schoolService.updateSchool(schoolId, updatedSchoolData, teacherId);

        assertNotNull(result);
        assertEquals(schoolId, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("New Town", result.getTown());
        assertEquals(987654321, result.getTlf());
        verify(schoolRepository, times(1)).findById(schoolId);
        verify(schoolRepository, times(1)).save(existingSchool);
    }

    @Test
    void updateSchool_shouldThrowSchoolValidationException_whenNameIsNull() {
        Integer schoolId = 1;
        Integer teacherId = 101;
        School updatedSchoolData = School.builder().name(null).build();
        String expectedErrorMessage = "School name is required.";

        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn(expectedErrorMessage);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        SchoolValidationException exception = assertThrows(SchoolValidationException.class, () ->
                schoolService.updateSchool(schoolId, updatedSchoolData, teacherId));

        assertEquals(1, exception.getErrors().size());
        assertEquals("name", exception.getErrors().get(0).getParam());
        assertEquals(expectedErrorMessage, exception.getErrors().get(0).getMessage());
        verify(schoolRepository, never()).findById(any());
        verify(schoolRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class));
    }

    @Test
    void updateSchool_shouldThrowSchoolValidationException_whenTlfIsInvalid() {
        Integer schoolId = 1;
        Integer teacherId = 101;
        School updatedSchoolData = School.builder().name("Valid Name").tlf(123).build();
        String expectedErrorMessage = "Telephone number must be 9 digits.";

        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class)))
                .thenReturn(expectedErrorMessage);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        SchoolValidationException exception = assertThrows(SchoolValidationException.class, () ->
                schoolService.updateSchool(schoolId, updatedSchoolData, teacherId));

        assertEquals(1, exception.getErrors().size());
        assertEquals("tlf", exception.getErrors().get(0).getParam());
        assertEquals(expectedErrorMessage, exception.getErrors().get(0).getMessage());
        verify(schoolRepository, never()).findById(any());
        verify(schoolRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class));
    }

    @Test
    void updateSchool_shouldThrowSchoolNotFoundException_whenSchoolDoesNotExist() {
        Integer schoolId = 1;
        Integer teacherId = 101;
        School updatedSchoolData = School.builder().name("New Name").build();
        String expectedErrorMessage = "School with ID 1 not found.";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn(expectedErrorMessage);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        SchoolNotFoundException exception = assertThrows(SchoolNotFoundException.class, () ->
                schoolService.updateSchool(schoolId, updatedSchoolData, teacherId));

        assertEquals(expectedErrorMessage, exception.getErrorDescription());
        verify(schoolRepository, times(1)).findById(schoolId);
        verify(schoolRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_NOT_FOUND), any(), any(Locale.class));
    }

    @Test
    void updateSchool_shouldThrowSchoolForbiddenException_whenSchoolNotOwnedByTeacher() {
        Integer schoolId = 1;
        Integer teacherId = 101;
        Integer otherTeacherId = 999;
        School existingSchool = School.builder().id(schoolId).teacherId(otherTeacherId).name("Old Name").build();
        School updatedSchoolData = School.builder().name("New Name").build();
        String expectedErrorMessage = "You are not authorized to update this school.";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(existingSchool));
        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_FORBIDDEN), any(), any(Locale.class)))
                .thenReturn(expectedErrorMessage);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        SchoolForbiddenException exception = assertThrows(SchoolForbiddenException.class, () ->
                schoolService.updateSchool(schoolId, updatedSchoolData, teacherId));

        assertEquals(expectedErrorMessage, exception.getErrorDescription());
        verify(schoolRepository, times(1)).findById(schoolId);
        verify(schoolRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_FORBIDDEN), any(), any(Locale.class));
    }

    @Test
    void updateSchool_shouldUseSpanishLocaleForValidationMessages_whenAcceptLanguageIsEs() {
        Integer schoolId = 1;
        Integer teacherId = 101;
        School updatedSchoolData = School.builder().name(null).build();
        String expectedSpanishMessage = "El nombre del colegio es obligatorio.";
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED, null, Locale.ENGLISH))
                .thenReturn(expectedSpanishMessage);

        SchoolValidationException exception = assertThrows(SchoolValidationException.class, () ->
                schoolService.updateSchool(schoolId, updatedSchoolData, teacherId));

        assertEquals(1, exception.getErrors().size());
        assertEquals("name", exception.getErrors().get(0).getParam());
        assertEquals(expectedSpanishMessage, exception.getErrors().get(0).getMessage());
        verify(schoolRepository, never()).findById(any());
        verify(schoolRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED, null, Locale.ENGLISH);
    }
}
