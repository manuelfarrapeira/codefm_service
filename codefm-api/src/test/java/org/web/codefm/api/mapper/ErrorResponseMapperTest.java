package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.exception.BaseException;
import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;
import org.web.codefm.domain.exception.ListErrorMessageBaseException;
import org.web.codefm.model.ErrorResponseDTO;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorResponseMapperTest {

    @InjectMocks
    private ErrorResponseMapper errorResponseMapper;

    @Test
    void shouldParseErrorMessageBaseException() {
        ErrorMessageBaseException exception = Mockito.mock(ErrorMessageBaseException.class);
        when(exception.getErrorCodeEnum()).thenReturn(ErrorCodeEnum.GENERIC_ERROR);
        when(exception.getErrorDescription()).thenReturn("Error description");

        ErrorResponseDTO result = errorResponseMapper.toDTO(exception); // Updated call

        Assertions.assertEquals(ErrorCodeEnum.GENERIC_ERROR.getCode(), result.getCode());
        Assertions.assertEquals(ErrorCodeEnum.GENERIC_ERROR.getDescription(), result.getDescription());
        Assertions.assertEquals("Error description", result.getDetail());
        Assertions.assertNull(result.getDetails());
    }

    @Test
    void shouldHandleEmptyErrorList() {
        ListErrorMessageBaseException exception = Mockito.mock(ListErrorMessageBaseException.class);
        when(exception.getErrorCodeEnum()).thenReturn(ErrorCodeEnum.PARSER_ERROR);
        when(exception.getErrors()).thenReturn(List.of());

        ErrorResponseDTO result = errorResponseMapper.toDTO(exception); // Updated call

        Assertions.assertEquals(ErrorCodeEnum.PARSER_ERROR.getCode(), result.getCode());
        Assertions.assertEquals(ErrorCodeEnum.PARSER_ERROR.getDescription(), result.getDescription());
        Assertions.assertNull(result.getDetail());
        Assertions.assertTrue(result.getDetails().isEmpty());
    }

    @Test
    void shouldParseBaseExceptionWithoutDetails() {
        BaseException exception = Mockito.mock(BaseException.class);
        when(exception.getErrorCodeEnum()).thenReturn(ErrorCodeEnum.RESOURCE_NOT_FOUND);

        ErrorResponseDTO result = errorResponseMapper.toDTO(exception); // Updated call

        Assertions.assertEquals(ErrorCodeEnum.RESOURCE_NOT_FOUND.getCode(), result.getCode());
        Assertions.assertEquals(ErrorCodeEnum.RESOURCE_NOT_FOUND.getDescription(), result.getDescription());
        Assertions.assertNull(result.getDetail());
        Assertions.assertNull(result.getDetails());
    }

    @Test
    void shouldParseListErrorMessageBaseExceptionWithTranslatedMessages() {
        List<ErrorMessage> errorMessages = Arrays.asList(
                new ErrorMessage("name", "School name is required."), // Pre-translated message
                new ErrorMessage("tlf", "Telephone number must be 9 digits.") // Pre-translated message
        );
        ListErrorMessageBaseException exception = Mockito.mock(ListErrorMessageBaseException.class);
        when(exception.getErrorCodeEnum()).thenReturn(ErrorCodeEnum.VALIDATION_ERROR);
        when(exception.getErrors()).thenReturn(errorMessages);

        ErrorResponseDTO result = errorResponseMapper.toDTO(exception); // Updated call

        Assertions.assertEquals(ErrorCodeEnum.VALIDATION_ERROR.getCode(), result.getCode());
        Assertions.assertEquals(ErrorCodeEnum.VALIDATION_ERROR.getDescription(), result.getDescription());
        Assertions.assertNull(result.getDetail());
        Assertions.assertNotNull(result.getDetails());
        Assertions.assertEquals(2, result.getDetails().size());

        Assertions.assertEquals("name", result.getDetails().get(0).getField());
        Assertions.assertEquals("School name is required.", result.getDetails().get(0).getReason());

        Assertions.assertEquals("tlf", result.getDetails().get(1).getField());
        Assertions.assertEquals("Telephone number must be 9 digits.", result.getDetails().get(1).getReason());
    }

    @Test
    void shouldParseListErrorMessageBaseExceptionWithTranslatedMessages_esLocale() {
        List<ErrorMessage> errorMessages = Arrays.asList(
                new ErrorMessage("name", "El nombre del colegio es obligatorio."), // Pre-translated message
                new ErrorMessage("tlf", "El número de teléfono debe tener 9 dígitos.") // Pre-translated message
        );
        ListErrorMessageBaseException exception = Mockito.mock(ListErrorMessageBaseException.class);
        when(exception.getErrorCodeEnum()).thenReturn(ErrorCodeEnum.VALIDATION_ERROR);
        when(exception.getErrors()).thenReturn(errorMessages);

        ErrorResponseDTO result = errorResponseMapper.toDTO(exception); // Updated call

        Assertions.assertEquals(ErrorCodeEnum.VALIDATION_ERROR.getCode(), result.getCode());
        Assertions.assertEquals(ErrorCodeEnum.VALIDATION_ERROR.getDescription(), result.getDescription());
        Assertions.assertNull(result.getDetail());
        Assertions.assertNotNull(result.getDetails());
        Assertions.assertEquals(2, result.getDetails().size());

        Assertions.assertEquals("name", result.getDetails().get(0).getField());
        Assertions.assertEquals("El nombre del colegio es obligatorio.", result.getDetails().get(0).getReason());

        Assertions.assertEquals("tlf", result.getDetails().get(1).getField());
        Assertions.assertEquals("El número de teléfono debe tener 9 dígitos.", result.getDetails().get(1).getReason());
    }
}
