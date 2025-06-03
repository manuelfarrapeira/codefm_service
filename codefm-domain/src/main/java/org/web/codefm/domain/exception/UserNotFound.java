package org.web.codefm.domain.exception;

public class UserNotFound extends ErrorMessageBaseException{

    public UserNotFound(ErrorCodeEnum errorCodeEnum, String errorDescription) {
        super(errorCodeEnum, errorDescription);
    }
}
