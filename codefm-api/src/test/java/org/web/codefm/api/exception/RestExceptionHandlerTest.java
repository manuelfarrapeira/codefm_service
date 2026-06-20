package org.web.codefm.api.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.web.codefm.api.mapper.ErrorResponseMapper;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.UserNotFound;
import org.web.codefm.domain.exception.teachernotebook.SchoolValidationException;
import org.web.codefm.model.DetailDTO;
import org.web.codefm.model.ErrorResponseDTO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {

    @Mock
    private ErrorResponseMapper errorResponseMapper;

    private RestExceptionHandler restExceptionHandler;

    @BeforeEach
    void beforeEach() {
        this.restExceptionHandler = new RestExceptionHandler(this.errorResponseMapper);
    }

    @Nested
    class MapperException {

        @Test
        void when_user_not_found_is_mapped_expect_not_found_response() {
            final UserNotFound exception = new UserNotFound(ErrorCodeEnum.RESOURCE_NOT_FOUND, "User not found");
            final ErrorResponseDTO expectedErrorDTO = new ErrorResponseDTO();
            expectedErrorDTO.setDetail("User not found");
            expectedErrorDTO.setCode(ErrorCodeEnum.RESOURCE_NOT_FOUND.getCode());
            expectedErrorDTO.setDescription(ErrorCodeEnum.RESOURCE_NOT_FOUND.getDescription());

            try (MockedStatic<ExceptionStatusEnum> statusEnumMock = mockStatic(ExceptionStatusEnum.class)) {
                final ExceptionStatusEnum mockEnum = Mockito.mock(ExceptionStatusEnum.class);
                statusEnumMock.when(() -> ExceptionStatusEnum.getExceptionEnum(UserNotFound.class)).thenReturn(mockEnum);
                when(mockEnum.getStatus()).thenReturn(HttpStatus.NOT_FOUND);
                when(RestExceptionHandlerTest.this.errorResponseMapper.toDTO(any())).thenReturn(expectedErrorDTO);

                final ResponseEntity<ErrorResponseDTO> response = RestExceptionHandlerTest.this.restExceptionHandler.mapperException(exception);

                verify(RestExceptionHandlerTest.this.errorResponseMapper).toDTO(any());
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                assertThat(response.getBody().getDetail()).isEqualTo("User not found");
                assertThat(response.getBody().getCode()).isEqualTo(ErrorCodeEnum.RESOURCE_NOT_FOUND.getCode());
                assertThat(response.getBody().getDescription()).isEqualTo(ErrorCodeEnum.RESOURCE_NOT_FOUND.getDescription());
            }
        }

        @ParameterizedTest
        @MethodSource("schoolValidationResponses")
        void when_school_validation_exception_is_mapped_expect_bad_request_response(final String firstReason,
                                                                                    final String secondReason) {
            final List<ErrorMessage> errorMessages = Arrays.asList(
                    new ErrorMessage("name", firstReason),
                    new ErrorMessage("tlf", secondReason)
            );
            final SchoolValidationException exception = new SchoolValidationException(errorMessages);
            final ErrorResponseDTO expectedErrorDTO = new ErrorResponseDTO();
            expectedErrorDTO.setCode(ErrorCodeEnum.VALIDATION_ERROR.getCode());
            expectedErrorDTO.setDescription(ErrorCodeEnum.VALIDATION_ERROR.getDescription());
            expectedErrorDTO.setDetails(Arrays.asList(
                    new DetailDTO().field("name").reason(firstReason),
                    new DetailDTO().field("tlf").reason(secondReason)
            ));

            try (MockedStatic<ExceptionStatusEnum> statusEnumMock = mockStatic(ExceptionStatusEnum.class)) {
                final ExceptionStatusEnum mockEnum = Mockito.mock(ExceptionStatusEnum.class);
                statusEnumMock.when(() -> ExceptionStatusEnum.getExceptionEnum(SchoolValidationException.class)).thenReturn(mockEnum);
                when(mockEnum.getStatus()).thenReturn(HttpStatus.BAD_REQUEST);
                when(RestExceptionHandlerTest.this.errorResponseMapper.toDTO(any())).thenReturn(expectedErrorDTO);

                final ResponseEntity<ErrorResponseDTO> response = RestExceptionHandlerTest.this.restExceptionHandler.mapperException(exception);

                verify(RestExceptionHandlerTest.this.errorResponseMapper).toDTO(any());
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response.getBody().getCode()).isEqualTo(ErrorCodeEnum.VALIDATION_ERROR.getCode());
                assertThat(response.getBody().getDescription()).isEqualTo(ErrorCodeEnum.VALIDATION_ERROR.getDescription());
                assertThat(response.getBody().getDetails()).hasSize(2);
                assertThat(response.getBody().getDetails().get(0).getField()).isEqualTo("name");
                assertThat(response.getBody().getDetails().get(0).getReason()).isEqualTo(firstReason);
                assertThat(response.getBody().getDetails().get(1).getField()).isEqualTo("tlf");
                assertThat(response.getBody().getDetails().get(1).getReason()).isEqualTo(secondReason);
            }
        }

        private static Stream<Arguments> schoolValidationResponses() {
            return Stream.of(
                    Arguments.of("School name is required.", "Telephone number must be 9 digits."),
                    Arguments.of("El nombre del colegio es obligatorio.", "El número de teléfono debe tener 9 dígitos.")
            );
        }
    }

    @Nested
    class MapperGenericException {

        @Test
        void when_access_is_denied_expect_forbidden_response() {
            final AccessDeniedException accessDeniedException = new AccessDeniedException("Acceso denegado");

            final ResponseEntity<ErrorResponseDTO> response = RestExceptionHandlerTest.this.restExceptionHandler.mapperGenericException(accessDeniedException);

            assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
            assertThat(response.getBody().getDetail()).isEqualTo("Acceso denegado");
            assertThat(response.getBody().getCode()).isEqualTo("403");
            assertThat(response.getBody().getDescription()).isEqualTo("FORBIDDEN");
        }

        @Test
        void when_generic_exception_is_mapped_expect_internal_server_error_response() {
            final Exception genericException = new Exception("Error genérico");

            final ResponseEntity<ErrorResponseDTO> response = RestExceptionHandlerTest.this.restExceptionHandler.mapperGenericException(genericException);

            assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getDetail()).isEqualTo("Error genérico");
            assertThat(response.getBody().getCode()).isEqualTo("1000");
            assertThat(response.getBody().getDescription()).isEqualTo("INTERNAL_SERVER_ERROR");
        }
    }

    @Nested
    class HandleMethodArgumentNotValid {

        @Test
        void when_single_field_error_exists_expect_bad_request_response() throws NoSuchMethodException {
            final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
            bindingResult.addError(new FieldError("request", "name", "must not be blank"));

            final ResponseEntity<ErrorResponseDTO> response = RestExceptionHandlerTest.this.restExceptionHandler.handleMethodArgumentNotValid(
                    buildException(bindingResult));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCodeEnum.VALIDATION_ERROR.getCode());
            assertThat(response.getBody().getDescription()).isEqualTo(ErrorCodeEnum.VALIDATION_ERROR.getDescription());
            assertThat(response.getBody().getDetails()).hasSize(1);
            assertThat(response.getBody().getDetails().get(0).getField()).isEqualTo("name");
            assertThat(response.getBody().getDetails().get(0).getReason()).isEqualTo("must not be blank");
        }

        @Test
        void when_multiple_field_errors_exist_expect_bad_request_response() throws NoSuchMethodException {
            final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
            bindingResult.addError(new FieldError("request", "name", "must not be blank"));
            bindingResult.addError(new FieldError("request", "rubricId", "must not be null"));
            bindingResult.addError(new FieldError("request", "gradeStart", "must be between 0 and 10"));

            final ResponseEntity<ErrorResponseDTO> response = RestExceptionHandlerTest.this.restExceptionHandler.handleMethodArgumentNotValid(
                    buildException(bindingResult));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCodeEnum.VALIDATION_ERROR.getCode());
            assertThat(response.getBody().getDetails()).hasSize(3);
            assertThat(response.getBody().getDetails().get(0).getField()).isEqualTo("name");
            assertThat(response.getBody().getDetails().get(0).getReason()).isEqualTo("must not be blank");
            assertThat(response.getBody().getDetails().get(1).getField()).isEqualTo("rubricId");
            assertThat(response.getBody().getDetails().get(1).getReason()).isEqualTo("must not be null");
            assertThat(response.getBody().getDetails().get(2).getField()).isEqualTo("gradeStart");
            assertThat(response.getBody().getDetails().get(2).getReason()).isEqualTo("must be between 0 and 10");
        }

        @Test
        void when_no_field_errors_exist_expect_empty_details() throws NoSuchMethodException {
            final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");

            final ResponseEntity<ErrorResponseDTO> response = RestExceptionHandlerTest.this.restExceptionHandler.handleMethodArgumentNotValid(
                    buildException(bindingResult));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCodeEnum.VALIDATION_ERROR.getCode());
            assertThat(response.getBody().getDescription()).isEqualTo(ErrorCodeEnum.VALIDATION_ERROR.getDescription());
            assertThat(response.getBody().getDetails()).isEmpty();
        }

        private MethodArgumentNotValidException buildException(final BeanPropertyBindingResult bindingResult)
                throws NoSuchMethodException {
            final MethodParameter methodParameter = new MethodParameter(
                    RestExceptionHandler.class.getDeclaredMethod("handleMethodArgumentNotValid", MethodArgumentNotValidException.class),
                    -1);
            return new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }
}
