package org.web.codefm.domain.exception;

import lombok.Getter;

public abstract class ErrorMessageBaseException extends BaseException {

    private static final String TO_STRING_PATTERN = "[Code: %s, CodeDescription: %s, ErrorDescription: %s]";

    @Getter
    public final String errorDescription;

    protected ErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum, final String errorDescription) {
        super(errorCodeEnum);
        this.errorDescription = errorDescription;
    }

    protected ErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum, final String errorDescription,
                                        final Throwable throwable) {
        super(errorCodeEnum, throwable);
        this.errorDescription = errorDescription;
    }

    @Override
    public String getMessage() {
        return String.format(TO_STRING_PATTERN, this.errorCodeEnum.getCode(), this.errorCodeEnum.getDescription(), this.errorDescription);
    }
}
