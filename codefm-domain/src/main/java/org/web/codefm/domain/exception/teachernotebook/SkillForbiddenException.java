package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class SkillForbiddenException extends ErrorMessageBaseException {
    public SkillForbiddenException(String error) {
        super(ErrorCodeEnum.RESOURCE_FORBIDDEN, error);
    }
}
