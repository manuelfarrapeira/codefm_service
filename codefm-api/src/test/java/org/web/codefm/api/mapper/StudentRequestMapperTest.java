package org.web.codefm.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.exception.teachernotebook.StudentValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.model.StudentRequestDTO;

import java.time.LocalDate;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentRequestMapperTest {

    @Spy
    @InjectMocks
    private StudentRequestMapperImpl mapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    @BeforeEach
    void setUp() {
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_DATE_FORMAT_INVALID), eq(null), any(Locale.class)))
                .thenReturn("Date of birth must be in format dd/MM/yyyy.");
    }

    @Test
    void toDomain_shouldMapCorrectly_whenDateIsValid() {
        // Given
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setName("Juan");
        dto.setSurnames("García López");
        dto.setDateOfBirth("15/03/2010");
        dto.setAdditionalInfo("Test info");
        dto.setShape("CIRCLE");

        // When
        Student result = mapper.toDomain(dto);

        // Then
        assertNotNull(result);
        assertEquals("Juan", result.getName());
        assertEquals("García López", result.getSurnames());
        assertEquals(LocalDate.of(2010, 3, 15), result.getDateOfBirth());
        assertEquals("Test info", result.getAdditionalInfo());
        assertEquals("CIRCLE", result.getShape());
        assertNull(result.getId());
        assertNull(result.getPhoto());
        assertNull(result.getDeletionDate());
    }

    @Test
    void toDomain_shouldMapCorrectly_whenDateIsNull() {
        // Given
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setName("Juan");
        dto.setSurnames("García López");
        dto.setDateOfBirth(null);

        // When
        Student result = mapper.toDomain(dto);

        // Then
        assertNotNull(result);
        assertEquals("Juan", result.getName());
        assertEquals("García López", result.getSurnames());
        assertNull(result.getDateOfBirth());
    }

    @Test
    void toDomain_shouldMapCorrectly_whenDateIsEmpty() {
        // Given
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setName("Juan");
        dto.setSurnames("García López");
        dto.setDateOfBirth("");

        // When
        Student result = mapper.toDomain(dto);

        // Then
        assertNotNull(result);
        assertEquals("Juan", result.getName());
        assertEquals("García López", result.getSurnames());
        assertNull(result.getDateOfBirth());
    }

    @Test
    void toDomain_shouldThrowValidationException_whenDateFormatIsInvalid() {
        // Given
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setName("Juan");
        dto.setSurnames("García López");
        dto.setDateOfBirth("15-03-2010"); // Wrong format

        // When & Then
        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            mapper.toDomain(dto);
        });

        assertNotNull(exception);
        assertFalse(exception.getErrors().isEmpty());
        ErrorMessage error = exception.getErrors().get(0);
        assertEquals("dateOfBirth", error.getParam());
    }

    @Test
    void toDomain_shouldThrowValidationException_whenDateFormatIsInvalid_withSlashes() {
        // Given
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setName("Juan");
        dto.setSurnames("García López");
        dto.setDateOfBirth("2010/03/15"); // Wrong format (year first)

        // When & Then
        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            mapper.toDomain(dto);
        });

        assertNotNull(exception);
        assertFalse(exception.getErrors().isEmpty());
        ErrorMessage error = exception.getErrors().get(0);
        assertEquals("dateOfBirth", error.getParam());
    }

    @Test
    void toDomain_shouldThrowValidationException_whenDateIsInvalidText() {
        // Given
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setName("Juan");
        dto.setSurnames("García López");
        dto.setDateOfBirth("invalid-date");

        // When & Then
        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            mapper.toDomain(dto);
        });

        assertNotNull(exception);
        assertFalse(exception.getErrors().isEmpty());
        ErrorMessage error = exception.getErrors().get(0);
        assertEquals("dateOfBirth", error.getParam());
    }

    @Test
    void toDomain_shouldThrowValidationException_whenDateHasInvalidDay() {
        // Given
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setName("Juan");
        dto.setSurnames("García López");
        dto.setDateOfBirth("32/03/2010"); // Day 32 doesn't exist

        // When & Then
        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            mapper.toDomain(dto);
        });

        assertNotNull(exception);
        assertFalse(exception.getErrors().isEmpty());
    }

    @Test
    void toDomain_shouldThrowValidationException_whenDateHasInvalidMonth() {
        // Given
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setName("Juan");
        dto.setSurnames("García López");
        dto.setDateOfBirth("15/13/2010"); // Month 13 doesn't exist

        // When & Then
        StudentValidationException exception = assertThrows(StudentValidationException.class, () -> {
            mapper.toDomain(dto);
        });

        assertNotNull(exception);
        assertFalse(exception.getErrors().isEmpty());
    }
}

