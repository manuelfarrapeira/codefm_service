package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class SchoolForbiddenException extends ErrorMessageBaseException {
    public SchoolForbiddenException(String error) {
        super(ErrorCodeEnum.RESOURCE_FORBIDDEN, error);
    }
}
