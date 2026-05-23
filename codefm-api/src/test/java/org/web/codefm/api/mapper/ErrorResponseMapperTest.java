package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.exception.BaseException;
import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;
import org.web.codefm.domain.exception.ListErrorMessageBaseException;
import org.web.codefm.model.ErrorResponseDTO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ErrorResponseMapperTest {

    private final ErrorResponseMapper errorResponseMapper = new ErrorResponseMapper();

    @Nested
    class ToDTO {

        @Test
        void when_exception_has_single_error_message_expect_detail_response() {
            final ErrorMessageBaseException exception = Mockito.mock(ErrorMessageBaseException.class);
            when(exception.getErrorCodeEnum()).thenReturn(ErrorCodeEnum.GENERIC_ERROR);
            when(exception.getErrorDescription()).thenReturn("Error description");

            final ErrorResponseDTO result = ErrorResponseMapperTest.this.errorResponseMapper.toDTO(exception);

            assertThat(result.getCode()).isEqualTo(ErrorCodeEnum.GENERIC_ERROR.getCode());
            assertThat(result.getDescription()).isEqualTo(ErrorCodeEnum.GENERIC_ERROR.getDescription());
            assertThat(result.getDetail()).isEqualTo("Error description");
            assertThat(result.getDetails()).isNull();
        }

        @Test
        void when_error_list_is_empty_expect_empty_details() {
            final ListErrorMessageBaseException exception = Mockito.mock(ListErrorMessageBaseException.class);
            when(exception.getErrorCodeEnum()).thenReturn(ErrorCodeEnum.PARSER_ERROR);
            when(exception.getErrors()).thenReturn(List.of());

            final ErrorResponseDTO result = ErrorResponseMapperTest.this.errorResponseMapper.toDTO(exception);

            assertThat(result.getCode()).isEqualTo(ErrorCodeEnum.PARSER_ERROR.getCode());
            assertThat(result.getDescription()).isEqualTo(ErrorCodeEnum.PARSER_ERROR.getDescription());
            assertThat(result.getDetail()).isNull();
            assertThat(result.getDetails()).isEmpty();
        }

        @Test
        void when_base_exception_has_no_extra_details_expect_basic_response() {
            final BaseException exception = Mockito.mock(BaseException.class);
            when(exception.getErrorCodeEnum()).thenReturn(ErrorCodeEnum.RESOURCE_NOT_FOUND);

            final ErrorResponseDTO result = ErrorResponseMapperTest.this.errorResponseMapper.toDTO(exception);

            assertThat(result.getCode()).isEqualTo(ErrorCodeEnum.RESOURCE_NOT_FOUND.getCode());
            assertThat(result.getDescription()).isEqualTo(ErrorCodeEnum.RESOURCE_NOT_FOUND.getDescription());
            assertThat(result.getDetail()).isNull();
            assertThat(result.getDetails()).isNull();
        }

        @ParameterizedTest
        @MethodSource("errorMessageSets")
        void when_error_messages_are_present_expect_details_response(final String firstReason, final String secondReason) {
            final List<ErrorMessage> errorMessages = Arrays.asList(
                    new ErrorMessage("name", firstReason),
                    new ErrorMessage("tlf", secondReason)
            );
            final ListErrorMessageBaseException exception = Mockito.mock(ListErrorMessageBaseException.class);
            when(exception.getErrorCodeEnum()).thenReturn(ErrorCodeEnum.VALIDATION_ERROR);
            when(exception.getErrors()).thenReturn(errorMessages);

            final ErrorResponseDTO result = ErrorResponseMapperTest.this.errorResponseMapper.toDTO(exception);

            assertThat(result.getCode()).isEqualTo(ErrorCodeEnum.VALIDATION_ERROR.getCode());
            assertThat(result.getDescription()).isEqualTo(ErrorCodeEnum.VALIDATION_ERROR.getDescription());
            assertThat(result.getDetail()).isNull();
            assertThat(result.getDetails()).hasSize(2);
            assertThat(result.getDetails().get(0).getField()).isEqualTo("name");
            assertThat(result.getDetails().get(0).getReason()).isEqualTo(firstReason);
            assertThat(result.getDetails().get(1).getField()).isEqualTo("tlf");
            assertThat(result.getDetails().get(1).getReason()).isEqualTo(secondReason);
        }

        private static Stream<Arguments> errorMessageSets() {
            return Stream.of(
                    Arguments.of("School name is required.", "Telephone number must be 9 digits."),
                    Arguments.of("El nombre del colegio es obligatorio.", "El número de teléfono debe tener 9 dígitos.")
            );
        }
    }
}
