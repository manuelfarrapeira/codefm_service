package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class SkillRubricNotFoundException extends ErrorMessageBaseException {
    public SkillRubricNotFoundException(String error) {
        super(ErrorCodeEnum.RESOURCE_NOT_FOUND, error);
    }
}

