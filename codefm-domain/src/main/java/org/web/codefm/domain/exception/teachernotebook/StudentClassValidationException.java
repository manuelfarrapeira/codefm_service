package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class StudentClassValidationException extends ErrorMessageBaseException {
    public StudentClassValidationException(String message) {
        super(ErrorCodeEnum.VALIDATION_ERROR, message);
    }
}

