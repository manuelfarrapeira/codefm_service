package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class ExerciseDocumentUploadException extends ErrorMessageBaseException {

    public ExerciseDocumentUploadException(String errorDescription) {
        super(ErrorCodeEnum.GENERIC_ERROR, errorDescription);
    }

    public ExerciseDocumentUploadException(String errorDescription, Throwable throwable) {
        super(ErrorCodeEnum.GENERIC_ERROR, errorDescription, throwable);
    }
}

