package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class StudentNotFoundException extends ErrorMessageBaseException {

    public StudentNotFoundException(String errorDescription) {
        super(ErrorCodeEnum.RESOURCE_NOT_FOUND, errorDescription);
    }
}

