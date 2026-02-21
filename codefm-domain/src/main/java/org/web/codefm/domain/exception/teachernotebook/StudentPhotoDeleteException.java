package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class StudentPhotoDeleteException extends ErrorMessageBaseException {

    public StudentPhotoDeleteException(String errorDescription) {
        super(ErrorCodeEnum.GENERIC_ERROR, errorDescription);
    }

    public StudentPhotoDeleteException(String errorDescription, Throwable throwable) {
        super(ErrorCodeEnum.GENERIC_ERROR, errorDescription, throwable);
    }
}

