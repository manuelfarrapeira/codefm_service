package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class SubjectNotFoundException extends ErrorMessageBaseException {
    public SubjectNotFoundException(String error) {
        super(ErrorCodeEnum.RESOURCE_NOT_FOUND, error);
    }
}
