package org.web.codefm.api.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.exception.teachernotebook.StudentAbsenceValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.session.SessionUser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class StudentAbsenceRequestMapper {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private SessionUser sessionUser;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public LocalDate parseDate(String dateString) {
		if (dateString == null || dateString.trim().isEmpty()) {
			final List<ErrorMessage> errors = new ArrayList<>();
			final String message = this.messageSource.getMessage(MessageKeys.ABSENCE_VALIDATION_DATE_REQUIRED, null,
                    this.sessionUser.getLocale());
			errors.add(new ErrorMessage("date", message));
			throw new StudentAbsenceValidationException(errors);
		}

		try {
			return LocalDate.parse(dateString, DATE_FORMATTER);
		} catch (final DateTimeParseException e) {
			final List<ErrorMessage> errors = new ArrayList<>();
			final String message = this.messageSource.getMessage(MessageKeys.ABSENCE_VALIDATION_DATE_INVALID, null,
                    this.sessionUser.getLocale());
			errors.add(new ErrorMessage("date", message));
			throw new StudentAbsenceValidationException(errors);
		}
	}
}
