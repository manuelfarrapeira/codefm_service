package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class ExerciseNotFoundException extends ErrorMessageBaseException {
    public ExerciseNotFoundException(String message) {
        super(ErrorCodeEnum.RESOURCE_NOT_FOUND, message);
    }
}

