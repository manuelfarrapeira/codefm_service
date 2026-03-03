package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class GradeExportException extends ErrorMessageBaseException {

    public GradeExportException(String errorDescription) {
        super(ErrorCodeEnum.GENERIC_ERROR, errorDescription);
    }

    public GradeExportException(String errorDescription, Throwable throwable) {
        super(ErrorCodeEnum.GENERIC_ERROR, errorDescription, throwable);
    }
}

