package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class StudentSearchValidationException extends ErrorMessageBaseException {

    public StudentSearchValidationException(String errorDescription) {
        super(ErrorCodeEnum.VALIDATION_ERROR, errorDescription);
    }
}

