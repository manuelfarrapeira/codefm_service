package org.web.codefm.domain.exception;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.web.codefm.domain.entity.exception.ErrorMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListErrorMessageBaseExceptionTest {

    @Nested
    class Constructor {

        @Test
        void when_created_with_error_list_and_throwable_expect_errors_and_cause() {
            final ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
            final List<ErrorMessage> errorMessages = Arrays.asList(
                    new ErrorMessage("field1", "Error message 1"),
                    new ErrorMessage("field2", "Error message 2")
            );
            final Throwable throwable = new Exception("Test exception");

            final ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum,
                    errorMessages, throwable);

            assertThat(exception.getErrors()).hasSize(2);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("field1");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo("Error message 1");
            assertThat(exception.getErrors().get(1).getParam()).isEqualTo("field2");
            assertThat(exception.getErrors().get(1).getMessage()).isEqualTo("Error message 2");
            assertThat(exception.getCause()).hasMessage("Test exception");
        }

        @Test
        void when_created_with_single_error_and_throwable_expect_single_error_and_cause() {
            final ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
            final ErrorMessage errorMessage = new ErrorMessage("field1", "Single error message");
            final Throwable throwable = new Exception("Test exception");

            final ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum,
                    errorMessage, throwable);

            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("field1");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo("Single error message");
            assertThat(exception.getCause()).hasMessage("Test exception");
        }

        @Test
        void when_created_with_param_and_message_expect_single_error() {
            final ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;

            final ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum,
                    "field1", "Error message for field1");

            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("field1");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo("Error message for field1");
        }

        @Test
        void when_created_with_error_list_expect_all_errors() {
            final ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
            final List<ErrorMessage> errorMessages = Arrays.asList(
                    new ErrorMessage("field1", "Error message 1"),
                    new ErrorMessage("field2", "Error message 2")
            );

            final ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum,
                    errorMessages);

            assertThat(exception.getErrors()).hasSize(2);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("field1");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo("Error message 1");
            assertThat(exception.getErrors().get(1).getParam()).isEqualTo("field2");
            assertThat(exception.getErrors().get(1).getMessage()).isEqualTo("Error message 2");
        }

        @Test
        void when_created_with_single_error_expect_single_error() {
            final ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
            final ErrorMessage errorMessage = new ErrorMessage("field1", "Single error message");

            final ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum,
                    errorMessage);

            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("field1");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo("Single error message");
        }
    }

    @Nested
    class AddError {

        @Test
        void when_error_message_is_added_expect_error_in_list() {
            final ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
            final ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum);

            exception.addError(new ErrorMessage("field1", "New error message"));

            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("field1");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo("New error message");
        }

        @Test
        void when_error_list_is_added_expect_all_errors_in_list() {
            final ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
            final ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum);
            final List<ErrorMessage> errorMessages = Arrays.asList(
                    new ErrorMessage("field2", "Error message 2"),
                    new ErrorMessage("field3", "Error message 3")
            );

            exception.addError(errorMessages);

            assertThat(exception.getErrors()).hasSize(2);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("field2");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo("Error message 2");
            assertThat(exception.getErrors().get(1).getParam()).isEqualTo("field3");
            assertThat(exception.getErrors().get(1).getMessage()).isEqualTo("Error message 3");
        }
    }

    @Nested
    class GetMessage {

        @Test
        void when_message_is_requested_expect_formatted_message() {
            final ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
            final List<ErrorMessage> errorMessages = Arrays.asList(
                    new ErrorMessage("field1", "First error"),
                    new ErrorMessage("field2", "Second error")
            );
            final ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum,
                    errorMessages);

            final String message = exception.getMessage();

            assertThat(message).isEqualTo("[Code: 1000, CodeDescription: GENERIC_ERROR, Errors: (ErrorMessage(param=field1, message=First error) | ErrorMessage(param=field2, message=Second error))]");
        }
    }

    private static class MockListErrorMessageBaseException extends ListErrorMessageBaseException {
        MockListErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum) {
            super(errorCodeEnum, new ArrayList<>());
        }

        MockListErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum, final List<ErrorMessage> errorMessageList) {
            super(errorCodeEnum, errorMessageList);
        }

        MockListErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum, final ErrorMessage errorMessage) {
            super(errorCodeEnum, errorMessage);
        }

        MockListErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum,
                                          final List<ErrorMessage> errorMessageList,
                                          final Throwable throwable) {
            super(errorCodeEnum, errorMessageList, throwable);
        }

        MockListErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum,
                                          final ErrorMessage errorMessage,
                                          final Throwable throwable) {
            super(errorCodeEnum, errorMessage, throwable);
        }

        MockListErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum,
                                          final String param,
                                          final String message) {
            super(errorCodeEnum, param, message);
        }
    }
}
