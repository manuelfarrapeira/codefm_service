package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class StudentPhotoNotFoundException extends ErrorMessageBaseException {

    public StudentPhotoNotFoundException(String errorDescription) {
        super(ErrorCodeEnum.RESOURCE_NOT_FOUND, errorDescription);
    }
}

