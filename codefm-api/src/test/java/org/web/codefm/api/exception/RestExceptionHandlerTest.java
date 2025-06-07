package org.web.codefm.api.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.web.codefm.api.mapper.ErrorResponseMapper;
import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.UserNotFound;
import org.web.codefm.model.ErrorResponseDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {
    @InjectMocks
    private RestExceptionHandler restExceptionHandler;
    @Mock
    private ExceptionStatusEnum exceptionStatusEnum;
    @Mock
    private ErrorResponseMapper errorResponseMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMapperExceptionWithUserNotFound() {
        // Arrange
        RestExceptionHandler handler = new RestExceptionHandler();
        UserNotFound exception = new UserNotFound(ErrorCodeEnum.RESOURCE_NOT_FOUND, "User not found");

        ExceptionStatusEnum mockEnum = org.mockito.Mockito.mock(ExceptionStatusEnum.class);
        ErrorResponseDTO errorDTO = new ErrorResponseDTO();
        errorDTO.setDetail("User not found");
        errorDTO.setCode("404");
        errorDTO.setDescription("NOT_FOUND");

        try (MockedStatic<ExceptionStatusEnum> statusEnumMock = mockStatic(ExceptionStatusEnum.class);
             MockedStatic<ErrorResponseMapper> mapperMock = mockStatic(ErrorResponseMapper.class)) {

            statusEnumMock.when(() -> ExceptionStatusEnum.getExceptionEnum(UserNotFound.class)).thenReturn(mockEnum);
            org.mockito.Mockito.when(mockEnum.getStatus()).thenReturn(HttpStatus.NOT_FOUND);
            mapperMock.when(() -> ErrorResponseMapper.toDTO(exception)).thenReturn(errorDTO);

            ResponseEntity<ErrorResponseDTO> response = handler.mapperException(exception);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("User not found", response.getBody().getDetail());
            assertEquals("404", response.getBody().getCode());
            assertEquals("NOT_FOUND", response.getBody().getDescription());
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

