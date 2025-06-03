package org.web.codefm.domain.exception;

import lombok.Getter;

public abstract class BaseException extends RuntimeException {

    @Getter
    public final ErrorCodeEnum errorCodeEnum;

    protected BaseException(final ErrorCodeEnum errorCodeEnum) {
        super();
        this.errorCodeEnum = errorCodeEnum;
    }

    protected BaseException(final ErrorCodeEnum errorCodeEnum, final Throwable throwable) {
        super(throwable);
        this.errorCodeEnum = errorCodeEnum;
    }
}
