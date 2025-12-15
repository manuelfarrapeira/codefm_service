package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.SchoolValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.SchoolRepository;

import java.util.Locale;

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
}
