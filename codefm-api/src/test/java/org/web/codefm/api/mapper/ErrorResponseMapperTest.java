package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.web.codefm.domain.exception.BaseException;
import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;
import org.web.codefm.domain.exception.ListErrorMessageBaseException;
import org.web.codefm.model.ErrorResponseDTO;

import java.util.List;

import static org.mockito.Mockito.when;

class ErrorResponseMapperTest {
    @Test
    void shouldParseErrorMessageBaseException() {
        ErrorMessageBaseException exception = Mockito.mock(ErrorMessageBaseException.class);
        when(exception.getErrorCodeEnum()).thenReturn(ErrorCodeEnum.GENERIC_ERROR);
        when(exception.getErrorDescription()).thenReturn("Error description");

        ErrorResponseDTO result = ErrorResponseMapper.toDTO(exception);

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

        ErrorResponseDTO result = ErrorResponseMapper.toDTO(exception);

        Assertions.assertEquals(ErrorCodeEnum.PARSER_ERROR.getCode(), result.getCode());
        Assertions.assertEquals(ErrorCodeEnum.PARSER_ERROR.getDescription(), result.getDescription());
        Assertions.assertNull(result.getDetail());
        Assertions.assertTrue(result.getDetails().isEmpty());
    }

    @Test
    void shouldParseBaseExceptionWithoutDetails() {
        BaseException exception = Mockito.mock(BaseException.class);
        when(exception.getErrorCodeEnum()).thenReturn(ErrorCodeEnum.RESOURCE_NOT_FOUND);

        ErrorResponseDTO result = ErrorResponseMapper.toDTO(exception);

        Assertions.assertEquals(ErrorCodeEnum.RESOURCE_NOT_FOUND.getCode(), result.getCode());
        Assertions.assertEquals(ErrorCodeEnum.RESOURCE_NOT_FOUND.getDescription(), result.getDescription());
        Assertions.assertNull(result.getDetail());
        Assertions.assertNull(result.getDetails());
    }


}