package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class StudentClassRubricCriteriaNotFoundException extends ErrorMessageBaseException {
    public StudentClassRubricCriteriaNotFoundException(String error) {
        super(ErrorCodeEnum.RESOURCE_NOT_FOUND, error);
    }
}

