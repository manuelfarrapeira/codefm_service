package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class ClassNotFoundException extends ErrorMessageBaseException {
    public ClassNotFoundException(String error) {
        super(ErrorCodeEnum.RESOURCE_NOT_FOUND, error);
    }
}
