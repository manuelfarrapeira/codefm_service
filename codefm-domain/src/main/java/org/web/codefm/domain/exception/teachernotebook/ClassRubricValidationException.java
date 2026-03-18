package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ListErrorMessageBaseException;

import java.util.List;

public class ClassRubricValidationException extends ListErrorMessageBaseException {
    public ClassRubricValidationException(List<ErrorMessage> errors) {
        super(ErrorCodeEnum.VALIDATION_ERROR, errors);
    }
}

