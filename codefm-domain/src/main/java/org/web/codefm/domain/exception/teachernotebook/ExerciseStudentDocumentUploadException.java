package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class ExerciseStudentDocumentUploadException extends ErrorMessageBaseException {

    public ExerciseStudentDocumentUploadException(String message) {
        super(ErrorCodeEnum.GENERIC_ERROR, message);
    }

    public ExerciseStudentDocumentUploadException(String message, Throwable cause) {
        super(ErrorCodeEnum.GENERIC_ERROR, message, cause);
    }
}

