package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class ClassForbiddenException extends ErrorMessageBaseException {
    public ClassForbiddenException(String error) {
        super(ErrorCodeEnum.RESOURCE_FORBIDDEN, error);
    }
}

