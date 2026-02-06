package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class ScheduleNotFoundException extends ErrorMessageBaseException {
    public ScheduleNotFoundException(String message) {
        super(ErrorCodeEnum.RESOURCE_NOT_FOUND, message);
    }
}
