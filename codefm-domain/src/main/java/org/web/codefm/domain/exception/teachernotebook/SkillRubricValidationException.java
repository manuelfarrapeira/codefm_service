package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ListErrorMessageBaseException;

import java.util.List;

public class SkillRubricValidationException extends ListErrorMessageBaseException {

    public SkillRubricValidationException(List<ErrorMessage> errors) {
        super(ErrorCodeEnum.VALIDATION_ERROR, errors);
    }
}

