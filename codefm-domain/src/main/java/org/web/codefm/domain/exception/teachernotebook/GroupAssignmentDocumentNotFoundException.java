package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class GroupAssignmentDocumentNotFoundException extends ErrorMessageBaseException {

    public GroupAssignmentDocumentNotFoundException(String errorDescription) {
        super(ErrorCodeEnum.RESOURCE_NOT_FOUND, errorDescription);
    }
}

