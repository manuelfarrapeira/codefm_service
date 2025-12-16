package org.web.codefm.api.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.web.codefm.api.mapper.ErrorResponseMapper;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.UserNotFound;
import org.web.codefm.domain.exception.teachernotebook.SchoolValidationException;
import org.web.codefm.model.DetailDTO;
import org.web.codefm.model.ErrorResponseDTO;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {
    @InjectMocks
    private RestExceptionHandler restExceptionHandler;
    @Mock
    private ExceptionStatusEnum exceptionStatusEnum; // This mock is not used directly in the current RestExceptionHandler logic
    @Mock
    private ErrorResponseMapper errorResponseMapper;
    // MessageSource is no longer directly used by RestExceptionHandler for error message translation
    // @Mock
    // private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // RestExceptionHandler constructor no longer takes MessageSource
        restExceptionHandler = new RestExceptionHandler(errorResponseMapper);
    }

    @Test
    void testMapperExceptionWithUserNotFound() {
        // Arrange
        UserNotFound exception = new UserNotFound(ErrorCodeEnum.RESOURCE_NOT_FOUND, "User not found");
        ErrorResponseDTO expectedErrorDTO = new ErrorResponseDTO();
        expectedErrorDTO.setDetail("User not found");
        expectedErrorDTO.setCode(ErrorCodeEnum.RESOURCE_NOT_FOUND.getCode());
        expectedErrorDTO.setDescription(ErrorCodeEnum.RESOURCE_NOT_FOUND.getDescription());

        try (MockedStatic<ExceptionStatusEnum> statusEnumMock = mockStatic(ExceptionStatusEnum.class)) {
            // Mock static method call for ExceptionStatusEnum
            ExceptionStatusEnum mockEnum = org.mockito.Mockito.mock(ExceptionStatusEnum.class);
            statusEnumMock.when(() -> ExceptionStatusEnum.getExceptionEnum(UserNotFound.class)).thenReturn(mockEnum);
            when(mockEnum.getStatus()).thenReturn(HttpStatus.NOT_FOUND);

            // Mock the injected ErrorResponseMapper
            // For ErrorMessageBaseException, the detail comes directly from the exception, not MessageSource
            when(errorResponseMapper.toDTO(any())).thenReturn(expectedErrorDTO); // Updated toDTO call

            // Act
            // mapperException no longer takes acceptLanguage
            ResponseEntity<ErrorResponseDTO> response = restExceptionHandler.mapperException(exception);

            // Verify that toDTO was called
            Mockito.verify(errorResponseMapper).toDTO(any()); // Updated verify call

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("User not found", response.getBody().getDetail());
            assertEquals(ErrorCodeEnum.RESOURCE_NOT_FOUND.getCode(), response.getBody().getCode());
            assertEquals(ErrorCodeEnum.RESOURCE_NOT_FOUND.getDescription(), response.getBody().getDescription());
        }
    }

    @Test
    void testMapperExceptionWithSchoolValidationException_enLocale() {
        // Arrange
        List<ErrorMessage> errorMessages = Arrays.asList(
                // ErrorMessage now contains the pre-translated message
                new ErrorMessage("name", "School name is required."),
                new ErrorMessage("tlf", "Telephone number must be 9 digits.")
        );
        SchoolValidationException exception = new SchoolValidationException(errorMessages);

        ErrorResponseDTO expectedErrorDTO = new ErrorResponseDTO();
        expectedErrorDTO.setCode(ErrorCodeEnum.VALIDATION_ERROR.getCode());
        expectedErrorDTO.setDescription(ErrorCodeEnum.VALIDATION_ERROR.getDescription());
        expectedErrorDTO.setDetails(Arrays.asList(
                new DetailDTO().field("name").reason("School name is required."),
                new DetailDTO().field("tlf").reason("Telephone number must be 9 digits.")
        ));

        try (MockedStatic<ExceptionStatusEnum> statusEnumMock = mockStatic(ExceptionStatusEnum.class)) {
            // Mock static method call for ExceptionStatusEnum
            ExceptionStatusEnum mockEnum = org.mockito.Mockito.mock(ExceptionStatusEnum.class);
            statusEnumMock.when(() -> ExceptionStatusEnum.getExceptionEnum(SchoolValidationException.class)).thenReturn(mockEnum);
            when(mockEnum.getStatus()).thenReturn(HttpStatus.BAD_REQUEST);

            // Mock the injected ErrorResponseMapper to return the expected DTO with translated messages
            when(errorResponseMapper.toDTO(any())).thenReturn(expectedErrorDTO); // Updated toDTO call

            // Act
            // mapperException no longer takes acceptLanguage
            ResponseEntity<ErrorResponseDTO> response = restExceptionHandler.mapperException(exception);

            // Verify that toDTO was called
            Mockito.verify(errorResponseMapper).toDTO(any()); // Updated verify call

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals(ErrorCodeEnum.VALIDATION_ERROR.getCode(), response.getBody().getCode());
            assertEquals(ErrorCodeEnum.VALIDATION_ERROR.getDescription(), response.getBody().getDescription());
            assertEquals(2, response.getBody().getDetails().size());
            assertEquals("name", response.getBody().getDetails().get(0).getField());
            assertEquals("School name is required.", response.getBody().getDetails().get(0).getReason());
            assertEquals("tlf", response.getBody().getDetails().get(1).getField());
            assertEquals("Telephone number must be 9 digits.", response.getBody().getDetails().get(1).getReason());
        }
    }

    @Test
    void testMapperExceptionWithSchoolValidationException_esLocale() {
        // Arrange
        List<ErrorMessage> errorMessages = Arrays.asList(
                // ErrorMessage now contains the pre-translated message
                new ErrorMessage("name", "El nombre del colegio es obligatorio."),
                new ErrorMessage("tlf", "El número de teléfono debe tener 9 dígitos.")
        );
        SchoolValidationException exception = new SchoolValidationException(errorMessages);

        ErrorResponseDTO expectedErrorDTO = new ErrorResponseDTO();
        expectedErrorDTO.setCode(ErrorCodeEnum.VALIDATION_ERROR.getCode());
        expectedErrorDTO.setDescription(ErrorCodeEnum.VALIDATION_ERROR.getDescription());
        expectedErrorDTO.setDetails(Arrays.asList(
                new DetailDTO().field("name").reason("El nombre del colegio es obligatorio."),
                new DetailDTO().field("tlf").reason("El número de teléfono debe tener 9 dígitos.")
        ));

        try (MockedStatic<ExceptionStatusEnum> statusEnumMock = mockStatic(ExceptionStatusEnum.class)) {
            // Mock static method call for ExceptionStatusEnum
            ExceptionStatusEnum mockEnum = org.mockito.Mockito.mock(ExceptionStatusEnum.class);
            statusEnumMock.when(() -> ExceptionStatusEnum.getExceptionEnum(SchoolValidationException.class)).thenReturn(mockEnum);
            when(mockEnum.getStatus()).thenReturn(HttpStatus.BAD_REQUEST);

            // Mock the injected ErrorResponseMapper to return the expected DTO with translated messages
            when(errorResponseMapper.toDTO(any())).thenReturn(expectedErrorDTO); // Updated toDTO call

            // Act
            // mapperException no longer takes acceptLanguage
            ResponseEntity<ErrorResponseDTO> response = restExceptionHandler.mapperException(exception);

            // Verify that toDTO was called
            Mockito.verify(errorResponseMapper).toDTO(any()); // Updated verify call

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals(ErrorCodeEnum.VALIDATION_ERROR.getCode(), response.getBody().getCode());
            assertEquals(ErrorCodeEnum.VALIDATION_ERROR.getDescription(), response.getBody().getDescription());
            assertEquals(2, response.getBody().getDetails().size());
            assertEquals("name", response.getBody().getDetails().get(0).getField());
            assertEquals("El nombre del colegio es obligatorio.", response.getBody().getDetails().get(0).getReason());
            assertEquals("tlf", response.getBody().getDetails().get(1).getField());
            assertEquals("El número de teléfono debe tener 9 dígitos.", response.getBody().getDetails().get(1).getReason());
        }
    }

    @Test
    void testMapperAccessDeniedException() {
        AccessDeniedException accessDeniedException = new AccessDeniedException("Acceso denegado");
        ResponseEntity<ErrorResponseDTO> response = restExceptionHandler.mapperGenericException(accessDeniedException);
        assertEquals(FORBIDDEN, response.getStatusCode());
        assertEquals("Acceso denegado", response.getBody().getDetail());
        assertEquals("403", response.getBody().getCode());
        assertEquals("FORBIDDEN", response.getBody().getDescription());
    }

    @Test
    void testMapperGenericException() {
        Exception genericException = new Exception("Error genérico");
        ResponseEntity<ErrorResponseDTO> response = restExceptionHandler.mapperGenericException(genericException);
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error genérico", response.getBody().getDetail());
        assertEquals("1000", response.getBody().getCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getDescription());
    }
}
