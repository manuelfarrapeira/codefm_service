package org.web.codefm.domain.exception.teachernotebook;

import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.ErrorMessageBaseException;

public class StudentAbsenceNotFoundException extends ErrorMessageBaseException {
	public StudentAbsenceNotFoundException(String error) {
		super(ErrorCodeEnum.RESOURCE_NOT_FOUND, error);
	}
}
