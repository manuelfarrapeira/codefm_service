package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class ClassRubricNotFoundException extends ErrorMessageBaseException {
    public ClassRubricNotFoundException(String error) {
        super(ErrorCodeEnum.RESOURCE_NOT_FOUND, error);
    }
}

