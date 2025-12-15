package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.SchoolForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SchoolNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SchoolValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.SchoolRepository;

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
    private MessageSource messageSource;


    private SchoolServiceImpl schoolService;

    private final String defaultAcceptLanguage = "en";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        schoolService = new SchoolServiceImpl(schoolRepository, messageSource);
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
        School createdSchool = schoolService.createSchool(schoolToCreate, defaultAcceptLanguage);

        assertNotNull(createdSchool);
        assertEquals("Valid School", createdSchool.getName());
        verify(schoolRepository, times(1)).save(schoolToCreate);
    }

    @Test
    void createSchool_shouldThrowException_whenNameIsNull() {
        School schoolWithNullName = School.builder().name(null).build();
        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("School name is required.");
        SchoolValidationException exception = assertThrows(SchoolValidationException.class, () -> {
            schoolService.createSchool(schoolWithNullName, defaultAcceptLanguage);
        });

        assertEquals(1, exception.getErrors().size());
        assertEquals("name", exception.getErrors().get(0).getParam());
        assertEquals("School name is required.", exception.getErrors().get(0).getMessage());
        verify(schoolRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class));
    }

    @Test
    void createSchool_shouldThrowException_whenTlfIsInvalid() {
        School schoolWithInvalidTlf = School.builder()
                .name("Valid School")
                .tlf(123)
                .build();
        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class)))
                .thenReturn("Telephone number must be 9 digits.");

        SchoolValidationException exception = assertThrows(SchoolValidationException.class, () -> {
            schoolService.createSchool(schoolWithInvalidTlf, defaultAcceptLanguage);
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

        SchoolValidationException exception = assertThrows(SchoolValidationException.class, () -> {
            schoolService.createSchool(schoolWithMultipleErrors, defaultAcceptLanguage);
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
    void createSchool_shouldUseSpanishLocaleForValidationMessages_whenAcceptLanguageIsEs() {
        School schoolWithNullName = School.builder().name(null).build();
        String spanishAcceptLanguage = "es";
        String expectedSpanishMessage = "El nombre del colegio es obligatorio.";

        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED), eq(null), eq(new Locale("es"))))
                .thenReturn(expectedSpanishMessage);

        SchoolValidationException exception = assertThrows(SchoolValidationException.class, () -> {
            schoolService.createSchool(schoolWithNullName, spanishAcceptLanguage);
        });

        assertEquals(1, exception.getErrors().size());
        assertEquals("name", exception.getErrors().get(0).getParam());
        assertEquals(expectedSpanishMessage, exception.getErrors().get(0).getMessage());
        verify(schoolRepository, never()).save(any());
    }

    @Test
    void softDeleteSchool_shouldCallRepository_whenSchoolExistsAndOwnedByTeacher() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 101;
        School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(schoolRepository.softDeleteSchool(schoolId, teacherId)).thenReturn(school); // Corrected: return a School object

        // When
        schoolService.softDeleteSchool(schoolId, teacherId, defaultAcceptLanguage);

        // Then
        verify(schoolRepository, times(1)).findById(schoolId);
        verify(schoolRepository, times(1)).softDeleteSchool(schoolId, teacherId);
    }

    @Test
    void softDeleteSchool_shouldThrowSchoolNotFoundException_whenSchoolDoesNotExist() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 101;
        String expectedErrorMessage = "School with ID 1 not found.";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_NOT_FOUND), any(), any(Locale.class))) // Changed any(Object[].class) to any()
                .thenReturn(expectedErrorMessage);

        // When / Then
        SchoolNotFoundException exception = assertThrows(SchoolNotFoundException.class, () ->
                schoolService.softDeleteSchool(schoolId, teacherId, defaultAcceptLanguage));

        assertEquals(expectedErrorMessage, exception.getErrorDescription());
        verify(schoolRepository, times(1)).findById(schoolId);
        verify(schoolRepository, never()).softDeleteSchool(any(), any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_NOT_FOUND), any(), any(Locale.class)); // Changed any(Object[].class) to any()
    }

    @Test
    void softDeleteSchool_shouldThrowSchoolForbiddenException_whenSchoolNotOwnedByTeacher() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 101;
        Integer otherTeacherId = 999;
        School school = School.builder().id(schoolId).teacherId(otherTeacherId).name("School A").build();
        String expectedErrorMessage = "You are not authorized to delete this school.";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(messageSource.getMessage(eq(MessageKeys.SCHOOL_FORBIDDEN), any(), any(Locale.class))) // Changed eq(null) to any()
                .thenReturn(expectedErrorMessage);

        // When / Then
        SchoolForbiddenException exception = assertThrows(SchoolForbiddenException.class, () ->
                schoolService.softDeleteSchool(schoolId, teacherId, defaultAcceptLanguage));

        assertEquals(expectedErrorMessage, exception.getErrorDescription());
        verify(schoolRepository, times(1)).findById(schoolId);
        verify(schoolRepository, never()).softDeleteSchool(any(), any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_FORBIDDEN), any(), any(Locale.class)); // Changed eq(null) to any()
    }

    @Test
    void getSchoolById_shouldReturnSchool_whenFound() {
        // Given
        Integer schoolId = 1;
        School expectedSchool = School.builder().id(schoolId).name("Test School").build();
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(expectedSchool));

        // When
        Optional<School> result = schoolService.getSchoolById(schoolId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedSchool, result.get());
        verify(schoolRepository, times(1)).findById(schoolId);
    }

    @Test
    void getSchoolById_shouldReturnEmpty_whenNotFound() {
        // Given
        Integer schoolId = 1;
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());

        // When
        Optional<School> result = schoolService.getSchoolById(schoolId);

        // Then
        assertFalse(result.isPresent());
        verify(schoolRepository, times(1)).findById(schoolId);
    }
}
