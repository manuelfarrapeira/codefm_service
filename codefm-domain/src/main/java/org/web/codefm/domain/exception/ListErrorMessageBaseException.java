package org.web.codefm.domain.exception;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.web.codefm.domain.entity.exception.ErrorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ListErrorMessageBaseException extends BaseException {

    private static final String TO_STRING_PATTERN = "[Code: %s, CodeDescription: %s, Errors: (%s)]";

    @Getter
    public final List<ErrorMessage> errors = new ArrayList<>();

    protected ListErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum, final List<ErrorMessage> errorMessageList) {
        super(errorCodeEnum);
        this.addError(errorMessageList);
    }

    protected ListErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum, final ErrorMessage errorMessage) {
        super(errorCodeEnum);
        this.addError(errorMessage);
    }

    protected ListErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum, final List<ErrorMessage> errorMessageList,
                                            final Throwable throwable) {
        super(errorCodeEnum, throwable);
        this.addError(errorMessageList);
    }

    protected ListErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum, final ErrorMessage errorMessage, final Throwable throwable) {
        super(errorCodeEnum, throwable);
        this.addError(errorMessage);
    }

    protected ListErrorMessageBaseException(final ErrorCodeEnum errorCodeEnum, final String param, final String message) {
        super(errorCodeEnum);
        this.addError(param, message);
    }

    public void addError(final ErrorMessage error) {
        if (error != null) {
            this.errors.add(error);
        }
    }

    public void addError(final List<ErrorMessage> errorList) {
        this.errors.addAll(errorList);
    }

    public void addError(final String param, final String message) {
        if (StringUtils.isNotBlank(param)) {
            final ErrorMessage errorMessage = new ErrorMessage(param, message);
            this.errors.add(errorMessage);
        }
    }

    @Override
    public String getMessage() {
        final String errorsAsString = this.errors.stream()
                .map(ErrorMessage::toString)
                .collect(Collectors.joining(" | "));
        return String.format(TO_STRING_PATTERN, this.errorCodeEnum.getCode(), this.errorCodeEnum.getDescription(), errorsAsString);
    }

}

