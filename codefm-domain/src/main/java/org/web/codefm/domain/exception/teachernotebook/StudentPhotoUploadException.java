package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class StudentPhotoUploadException extends ErrorMessageBaseException {

    public StudentPhotoUploadException(String errorDescription) {
        super(ErrorCodeEnum.GENERIC_ERROR, errorDescription);
    }

    public StudentPhotoUploadException(String errorDescription, Throwable throwable) {
        super(ErrorCodeEnum.GENERIC_ERROR, errorDescription, throwable);
    }
}

