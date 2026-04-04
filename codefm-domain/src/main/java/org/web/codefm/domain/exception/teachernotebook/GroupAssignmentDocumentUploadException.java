package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class GroupAssignmentDocumentUploadException extends ErrorMessageBaseException {

    public GroupAssignmentDocumentUploadException(String errorDescription) {
        super(ErrorCodeEnum.GENERIC_ERROR, errorDescription);
    }

    public GroupAssignmentDocumentUploadException(String errorDescription, Throwable throwable) {
        super(ErrorCodeEnum.GENERIC_ERROR, errorDescription, throwable);
    }
}

