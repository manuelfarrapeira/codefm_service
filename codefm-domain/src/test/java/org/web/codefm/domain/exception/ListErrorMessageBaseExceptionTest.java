package org.web.codefm.domain.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.exception.ErrorMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ListErrorMessageBaseExceptionTest {

    @Test
    void testConstructorWithErrorMessageListAndThrowable() {

        ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
        List<ErrorMessage> errorMessages = Arrays.asList(
                new ErrorMessage("field1", "Error message 1"),
                new ErrorMessage("field2", "Error message 2")
        );
        Throwable throwable = new Exception("Test exception");

        ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum, errorMessages, throwable);

        Assertions.assertEquals(2, exception.getErrors().size());
        Assertions.assertEquals("field1", exception.getErrors().get(0).getParam());
        Assertions.assertEquals("Error message 1", exception.getErrors().get(0).getMessage());
        Assertions.assertEquals("field2", exception.getErrors().get(1).getParam());
        Assertions.assertEquals("Error message 2", exception.getErrors().get(1).getMessage());
        Assertions.assertEquals("Test exception", exception.getCause().getMessage());
    }

    @Test
    void testConstructorWithSingleErrorMessageAndThrowable() {

        ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
        ErrorMessage errorMessage = new ErrorMessage("field1", "Single error message");
        Throwable throwable = new Exception("Test exception");

        ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum, errorMessage, throwable);

        Assertions.assertEquals(1, exception.getErrors().size());
        Assertions.assertEquals("field1", exception.getErrors().get(0).getParam());
        Assertions.assertEquals("Single error message", exception.getErrors().get(0).getMessage());
        Assertions.assertEquals("Test exception", exception.getCause().getMessage());
    }

    @Test
    void testConstructorWithParamAndMessage() {

        ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
        String param = "field1";
        String message = "Error message for field1";

        ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum, param, message);

        Assertions.assertEquals(1, exception.getErrors().size());
        Assertions.assertEquals("field1", exception.getErrors().get(0).getParam());
        Assertions.assertEquals("Error message for field1", exception.getErrors().get(0).getMessage());
    }

    @Test
    void testConstructorWithErrorMessageList() {

        ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
        List<ErrorMessage> errorMessages = Arrays.asList(
                new ErrorMessage("field1", "Error message 1"),
                new ErrorMessage("field2", "Error message 2")
        );

        ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum, errorMessages);

        Assertions.assertEquals(2, exception.getErrors().size());
        Assertions.assertEquals("field1", exception.getErrors().get(0).getParam());
        Assertions.assertEquals("Error message 1", exception.getErrors().get(0).getMessage());
        Assertions.assertEquals("field2", exception.getErrors().get(1).getParam());
        Assertions.assertEquals("Error message 2", exception.getErrors().get(1).getMessage());
    }

    @Test
    void testConstructorWithSingleErrorMessage() {

        ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
        ErrorMessage errorMessage = new ErrorMessage("field1", "Single error message");

        ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum, errorMessage);

        Assertions.assertEquals(1, exception.getErrors().size());
        Assertions.assertEquals("field1", exception.getErrors().get(0).getParam());
        Assertions.assertEquals("Single error message", exception.getErrors().get(0).getMessage());
    }

    @Test
    void testAddErrorWithErrorMessage() {

        ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
        ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum);

        exception.addError(new ErrorMessage("field1", "New error message"));

        Assertions.assertEquals(1, exception.getErrors().size());
        Assertions.assertEquals("field1", exception.getErrors().get(0).getParam());
        Assertions.assertEquals("New error message", exception.getErrors().get(0).getMessage());
    }

    @Test
    void testAddErrorWithList() {

        ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
        ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum);
        List<ErrorMessage> errorMessages = Arrays.asList(
                new ErrorMessage("field2", "Error message 2"),
                new ErrorMessage("field3", "Error message 3")
        );

        exception.addError(errorMessages);

        Assertions.assertEquals(2, exception.getErrors().size());
        Assertions.assertEquals("field2", exception.getErrors().get(0).getParam());
        Assertions.assertEquals("Error message 2", exception.getErrors().get(0).getMessage());
        Assertions.assertEquals("field3", exception.getErrors().get(1).getParam());
        Assertions.assertEquals("Error message 3", exception.getErrors().get(1).getMessage());
    }

    @Test
    void testGetMessage() {

        ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.GENERIC_ERROR;
        List<ErrorMessage> errorMessages = Arrays.asList(
                new ErrorMessage("field1", "First error"),
                new ErrorMessage("field2", "Second error")
        );
        ListErrorMessageBaseException exception = new MockListErrorMessageBaseException(errorCodeEnum, errorMessages);

        String message = exception.getMessage();

        Assertions.assertEquals("[Code: 1000, CodeDescription: GENERIC_ERROR, Errors: (ErrorMessage(param=field1, message=First error) | ErrorMessage(param=field2, message=Second error))]", message);
    }

    // Clase mock para pruebas
    private static class MockListErrorMessageBaseException extends ListErrorMessageBaseException {
        public MockListErrorMessageBaseException(ErrorCodeEnum errorCodeEnum) {
            super(errorCodeEnum, new ArrayList<>());
        }

        public MockListErrorMessageBaseException(ErrorCodeEnum errorCodeEnum, List<ErrorMessage> errorMessageList) {
            super(errorCodeEnum, errorMessageList);
        }

        public MockListErrorMessageBaseException(ErrorCodeEnum errorCodeEnum, ErrorMessage errorMessage) {
            super(errorCodeEnum, errorMessage);
        }

        public MockListErrorMessageBaseException(ErrorCodeEnum errorCodeEnum, List<ErrorMessage> errorMessageList, Throwable throwable) {
            super(errorCodeEnum, errorMessageList, throwable);
        }

        public MockListErrorMessageBaseException(ErrorCodeEnum errorCodeEnum, ErrorMessage errorMessage, Throwable throwable) {
            super(errorCodeEnum, errorMessage, throwable);
        }

        public MockListErrorMessageBaseException(ErrorCodeEnum errorCodeEnum, String param, String message) {
            super(errorCodeEnum, param, message);
        }
    }
}